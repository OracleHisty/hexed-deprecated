Port to Minecraft 1.21.1, for Fabric and NeoForge:
- Hexes are now data-driven enchantments (`data/hexed/enchantment`), all part of the `#hexed:hex_enchantments` tag
- The existing config file keeps working; it is still synced from the server
- Fabric Language Kotlin is no longer required

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
