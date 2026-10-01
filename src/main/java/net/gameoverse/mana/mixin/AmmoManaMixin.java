package net.gameoverse.mana.mixin;

import java.util.List;

import net.gameoverse.mana.GameoverseMana;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.cost.Ammo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A rune cost is satisfied by mana when the caster has enough: nothing to consume, marked {@link GameoverseMana#MANA}
 * so {@code SpellCostManaMixin} takes the mana when the cast pays. Without enough mana Spell Engine looks for the rune
 * as usual. Runs on both sides (the client uses it to decide whether a cast can start), mana is synced to the client.
 */
@Mixin(Ammo.class)
public abstract class AmmoManaMixin {
    @Inject(method = "ammoForSpell", at = @At("HEAD"), cancellable = true)
    private static void gameoverse$payWithMana(Player player, Spell spell, ItemStack casterStack,
                                              CallbackInfoReturnable<Ammo.Result> cir) {
        if (player.getAbilities().instabuild) return;
        if (GameoverseMana.canPay(player, spell)) {
            cir.setReturnValue(new Ammo.Result(true, GameoverseMana.MANA, 0, List.of()));
        }
    }
}
