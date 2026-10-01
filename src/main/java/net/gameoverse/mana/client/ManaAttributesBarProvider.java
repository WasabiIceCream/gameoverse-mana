package net.gameoverse.mana.client;

import com.github.theredbrain.manaattributes.entity.ManaUsingEntity;
import dev.muon.dynamic_resource_bars.provider.ManaProvider;
import net.minecraft.client.Minecraft;

/** Dynamic Resource Bars' mana bar, fed from Mana Attributes' mana (synced to the client as an attachment). */
public final class ManaAttributesBarProvider implements ManaProvider {
    private static ManaUsingEntity player() {
        var p = Minecraft.getInstance().player;
        return p == null ? null : (ManaUsingEntity) p;
    }

    @Override
    public double getCurrentMana() {
        ManaUsingEntity p = player();
        return p == null ? 0 : p.manaattributes$getMana();
    }

    @Override
    public float getMaxMana() {
        ManaUsingEntity p = player();
        return p == null ? 0 : p.manaattributes$getMaxMana();
    }

    @Override
    public float getReservedMana() {
        ManaUsingEntity p = player();
        return p == null ? 0 : p.manaattributes$getReservedMana();
    }

    @Override
    public long getGameTime() {
        var level = Minecraft.getInstance().level;
        return level == null ? 0 : level.getGameTime();
    }
}
