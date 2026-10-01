# gameoverse-mana

Fabric mod (26.1.2), original, MIT, both sides. Not published (not asked). RPGMana's idea (cleannrooster/rpgmana, MIT,
stuck on 1.21.1) rebuilt on Mana Attributes 4.0.0 (TheRedBrain, MIT, needs Resource Bar API 4.0.0 and fzzy_config).

- **Spells cost mana.** A Spell Engine spell whose cost item is a rune (`runes:*`) is paid with mana when the caster has
  enough, and with the rune as before when they don't (the user's choice: runes stay as a backup). `AmmoManaMixin`
  returns a satisfied `Ammo.Result` with nothing to consume, marked `GameoverseMana.MANA`; `SpellCostManaMixin` takes
  the mana at the head of `SpellCost.consume` (server side). Arrow and other item costs are untouched. Creative casts are
  untouched.
- **Costs:** `config/gameoverse_mana.json` (defaults in the jar): RPGMana 1.4.5's per-spell table for Wizards and
  Paladins spells, otherwise by spell tier 20/30/40/60/80. Channelled spells spread the cost over their pulses
  (Arcane Beam: 60 over 4 pulses).
- **Pool:** Mana Attributes' server config (`config/manaattributes/server.toml`): `natural_max_mana = 100`,
  `natural_mana_regeneration = 5` (per second; its tick threshold is 20). Max mana also gets +5 per point of the
  caster's highest magic school spell power (`maxManaPerSpellPower`), refreshed every second.
- **Mana bar:** Dynamic Resource Bars 0.9.6 only reads mana from Combat Attributes (`ManaBarBehavior.COMBAT_ATTRIBUTES`);
  `ManaProviderManagerMixin` (client, `@Pseudo`) puts a Mana Attributes provider in that slot. Pack configs:
  `dynamic_resource_bars-client.json` `manaBarVisibility: SMART_FADE` (force-synced), Mana Attributes' own bar off
  (`config/manaattributes/client.toml` `mana_bar_display = "NONE"`, source of truth in `gameoverse-client-perf`).

Re-check the Spell Engine hooks (`Ammo.ammoForSpell`, `SpellCost.consume` signatures) and Dynamic Resource Bars'
`ManaProviderManager` when either updates.
