package net.gameoverse.mana.mixin.client;

import dev.muon.dynamic_resource_bars.compat.ManaProviderManager;
import dev.muon.dynamic_resource_bars.provider.ManaProvider;
import net.gameoverse.mana.client.ManaAttributesBarProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dynamic Resource Bars 0.9.6 only fills its mana bar from Combat Attributes (its one {@code ManaBarBehavior} besides
 * OFF; registered providers are never selected). With Combat Attributes absent, Mana Attributes' mana takes that slot,
 * so {@code manaBarBehavior: COMBAT_ATTRIBUTES} shows our mana. {@code @Pseudo}: skipped without Dynamic Resource Bars.
 */
@Pseudo
@Mixin(value = ManaProviderManager.class, remap = false)
public abstract class ManaProviderManagerMixin {
    @Shadow
    private static ManaProvider combatAttributesProvider;

    @Shadow
    public static void updateActiveProvider() {
    }

    @Inject(method = "initialize", at = @At("TAIL"), require = 0)
    private static void gameoverse$useManaAttributes(CallbackInfo ci) {
        if (combatAttributesProvider == null) {
            combatAttributesProvider = new ManaAttributesBarProvider();
            updateActiveProvider();
        }
    }
}
