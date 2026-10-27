Port to Minecraft 1.21.1, for Fabric and NeoForge:
- Hexes are now data-driven enchantments (`data/hexed/enchantment`), all part of the `#hexed:hex_enchantments` tag
- The existing config file keeps working; it is still synced from the server
- Fabric Language Kotlin is no longer required
- Requires one flame mod, either [Prometheus](https://modrinth.com/mod/prometheus-api) or [Dyed Flames](https://modrinth.com/mod/dyed-flames); Prometheus is used if both are installed

Aflame rework:
- Aflame is now its own burn with crimson flames and its own damage type, instead of normal fire on the wielder and Ablaze on the target
- It burns both the target and the wielder, for separate durations (`targetDuration`/`userDuration` in the config; old configs are updated automatically)
- It gets through effect immunity (bosses included); only the full Calamitous set protects from it
- Blazing Magma sets things on fire with its own pale gold flames

Bug Fixes:
- Disabling a hex with `shouldRegister: false` now only removes that hex from the altar (previously every hex except Aflame was hidden)
- The altar checks the item, candles and crystal on the server before hexing
- Brimstone Candle and plushie tooltips show up again
- Amplify mines the correct blocks at negative coordinates
- Phased's damage uses `initialDamage` when the config is loaded
- Overclock overheat is saved and loaded correctly, and the Fuller Auto advancement can be earned
- Celebration fireworks get a different explosion for each burst
- Death Mark (Baleful Carnage) is registered as a status effect
- Enchancement compat keeps hexes instead of removing them
