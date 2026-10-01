package net.gameoverse.mana;

import java.util.List;
import java.util.Map;

/**
 * Pure mana rules (unit-tested, no Minecraft types). A spell's mana cost is its entry in the cost table, or a cost by
 * spell tier; a channelled spell pays it spread over its pulses, so a beam costs its listed amount over the whole cast
 * rather than per pulse.
 */
public final class ManaRules {
    private ManaRules() {
    }

    /** Full mana cost of one cast. */
    public static float baseCost(String spellId, int tier, Map<String, Float> table, List<Float> tierCosts) {
        Float listed = table.get(spellId);
        if (listed != null) return listed;
        if (tierCosts.isEmpty()) return 0;
        return tierCosts.get(Math.max(0, Math.min(tier, tierCosts.size() - 1)));
    }

    /**
     * Mana for one payment: channelled spells pay once per pulse ({@code durationSeconds * 20 / channelTicks} pulses),
     * everything else pays the whole cost at once.
     */
    public static float perPayment(float baseCost, boolean channelled, float durationSeconds, int channelTicks) {
        if (!channelled || channelTicks <= 0 || durationSeconds <= 0) return baseCost;
        float pulses = Math.max(1f, durationSeconds * 20f / channelTicks);
        return baseCost / pulses;
    }

    /** Spell Engine's rune costs are items from the Runes mod; arrows and other ammo stay item costs. */
    public static boolean isRuneCost(String itemId) {
        return itemId != null && itemId.startsWith("runes:");
    }

    /** Max mana bonus from spell power: the highest magic school's power times the per-point bonus. */
    public static double spellPowerBonus(double highestSchoolPower, double perPoint) {
        return Math.max(0, highestSchoolPower) * perPoint;
    }
}
