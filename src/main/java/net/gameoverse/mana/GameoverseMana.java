package net.gameoverse.mana;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.theredbrain.manaattributes.ManaAttributes;
import com.github.theredbrain.manaattributes.entity.ManaUsingEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.cost.Ammo;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mana for Spell Engine spells, on top of Mana Attributes' mana pool (RPGMana's idea, for 26.1): a spell whose cost is a
 * rune is paid with mana when the caster has enough, and with the rune as before when they don't. Max mana also grows
 * with the caster's highest magic spell power. Config: {@code config/gameoverse_mana.json}.
 */
public final class GameoverseMana implements ModInitializer {
    public static final String ID = "gameoverse_mana";
    static final Logger LOG = LoggerFactory.getLogger(ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Identifier SPELL_POWER_MODIFIER = Identifier.fromNamespaceAndPath(ID, "spell_power");

    /**
     * Marks an {@link Ammo.Result} as "paid with mana": {@code ammoForSpell} returns it satisfied with nothing to consume,
     * and {@code SpellCost.consume} takes the mana when it sees it.
     */
    public static final Ammo.Searched MANA = new Ammo.Searched(null, Items.AIR);

    static Map<String, Float> costTable = new HashMap<>();
    static List<Float> tierCosts = List.of(20f, 30f, 40f, 60f, 80f);
    static double manaPerSpellPower = 2.0;

    @Override
    public void onInitialize() {
        loadConfig();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                applySpellPowerBonus(player);
            }
        });
    }

    // ---- paying with mana ----

    static ManaUsingEntity mana(Player player) {
        return (ManaUsingEntity) player;
    }

    /** Mana for one payment of this spell, or 0 when the spell's cost isn't a rune. */
    public static float costOf(Player player, Spell spell) {
        if (spell == null || spell.cost == null || spell.cost.item == null || !ManaRules.isRuneCost(spell.cost.item.id)) {
            return 0;
        }
        var key = SpellRegistry.from(player.level()).getKey(spell);
        String id = key == null ? "" : key.toString();
        float base = ManaRules.baseCost(id, spell.tier, costTable, tierCosts);
        boolean channelled = spell.active != null && spell.active.cast != null && spell.active.cast.channel != null;
        float duration = spell.active != null && spell.active.cast != null ? spell.active.cast.duration : 0;
        int ticks = channelled ? spell.active.cast.channelTicks() : 0;
        return ManaRules.perPayment(base, channelled, duration, ticks);
    }

    /** True when the player can pay this rune-cost spell with mana right now. */
    public static boolean canPay(Player player, Spell spell) {
        float cost = costOf(player, spell);
        return cost > 0 && mana(player).manaattributes$getMana() >= cost;
    }

    public static void pay(Player player, Spell spell) {
        float cost = costOf(player, spell);
        if (cost > 0) {
            mana(player).manaattributes$addMana(-cost);
        }
    }

    // ---- spell power -> max mana ----

    static void applySpellPowerBonus(ServerPlayer player) {
        AttributeInstance max = player.getAttribute(ManaAttributes.MAX_MANA);
        if (max == null) return;
        double highest = 0;
        for (SpellSchool school : SpellSchools.all()) {
            // spell_power:generic is a percentage multiplier on every school (base 100), not a school's own power
            if (school.isMagicArchetype() && school != SpellSchools.GENERIC) {
                highest = Math.max(highest, SpellPower.getSpellPower(school, player).baseValue());
            }
        }
        double bonus = ManaRules.spellPowerBonus(highest, manaPerSpellPower);
        AttributeModifier current = max.getModifier(SPELL_POWER_MODIFIER);
        if (current != null && Math.abs(current.amount() - bonus) < 0.01) return;
        max.removeModifier(SPELL_POWER_MODIFIER);
        if (bonus > 0) {
            max.addTransientModifier(new AttributeModifier(SPELL_POWER_MODIFIER, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    // ---- config ----

    static void loadConfig() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(ID + ".json");
        JsonObject json;
        try (var in = GameoverseMana.class.getResourceAsStream("/gameoverse_mana_defaults.json")) {
            json = GSON.fromJson(new String(in.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("missing gameoverse_mana_defaults.json", e);
        }
        try {
            if (Files.exists(path)) {
                JsonObject user = GSON.fromJson(Files.readString(path), JsonObject.class);
                if (user != null) user.entrySet().forEach(e -> json.add(e.getKey(), e.getValue()));
            }
            Files.writeString(path, GSON.toJson(json));
        } catch (IOException | RuntimeException e) {
            LOG.error("Could not read {}, using defaults", path, e);
        }
        Map<String, Float> table = new HashMap<>();
        json.getAsJsonObject("spellCosts").entrySet().forEach(e -> table.put(e.getKey(), e.getValue().getAsFloat()));
        costTable = table;
        List<Float> tiers = new ArrayList<>();
        for (JsonElement e : json.getAsJsonArray("costByTier")) tiers.add(e.getAsFloat());
        tierCosts = tiers;
        manaPerSpellPower = json.get("maxManaPerSpellPower").getAsDouble();
    }
}
