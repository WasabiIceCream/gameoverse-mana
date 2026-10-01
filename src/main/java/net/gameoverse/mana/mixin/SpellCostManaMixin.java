package net.gameoverse.mana.mixin;

import net.gameoverse.mana.GameoverseMana;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.cost.Ammo;
import net.spell_engine.internals.cost.SpellCost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Takes the mana for a cast that {@code AmmoManaMixin} let through with mana (server side, once per payment). */
@Mixin(SpellCost.class)
public abstract class SpellCostManaMixin {
    @Inject(method = "consume", at = @At("HEAD"))
    private static void gameoverse$takeMana(Player player, float progress, SpellContainerSource.SourcedContainer container,
                                            Identifier spellId, Holder<Spell> spell, ItemStack stack, Ammo.Result ammo,
                                            boolean flag, CallbackInfo ci) {
        if (ammo != null && ammo.item() == GameoverseMana.MANA && !player.level().isClientSide()) {
            GameoverseMana.pay(player, spell.value());
        }
    }
}
