package net.gameoverse.mana;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ManaRulesTest {
    private final Map<String, Float> table = Map.of("wizards:arcane_bolt", 20f, "wizards:arcane_beam", 60f);
    private final List<Float> tiers = List.of(20f, 30f, 40f, 60f, 80f);

    @Test
    void tableWinsOverTier() {
        assertEquals(20f, ManaRules.baseCost("wizards:arcane_bolt", 3, table, tiers));
    }

    @Test
    void tierFallbackIsClamped() {
        assertEquals(40f, ManaRules.baseCost("mod:unknown", 2, table, tiers));
        assertEquals(80f, ManaRules.baseCost("mod:unknown", 9, table, tiers));
        assertEquals(20f, ManaRules.baseCost("mod:unknown", -1, table, tiers));
    }

    @Test
    void channelSpreadsCostOverPulses() {
        // arcane beam: 5 s, a pulse every 25 ticks = 4 pulses
        assertEquals(15f, ManaRules.perPayment(60f, true, 5f, 25), 1e-4);
        assertEquals(60f, ManaRules.perPayment(60f, false, 5f, 25));
        assertEquals(60f, ManaRules.perPayment(60f, true, 0f, 25));
    }

    @Test
    void onlyRunesAreMana() {
        assertTrue(ManaRules.isRuneCost("runes:arcane_stone"));
        assertFalse(ManaRules.isRuneCost("minecraft:arrow"));
        assertFalse(ManaRules.isRuneCost(null));
    }

    @Test
    void spellPowerBonus() {
        assertEquals(40.0, ManaRules.spellPowerBonus(20, 2));
        assertEquals(0.0, ManaRules.spellPowerBonus(-3, 2));
    }
}
