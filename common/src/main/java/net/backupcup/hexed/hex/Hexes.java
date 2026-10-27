package net.backupcup.hexed.hex;

import net.backupcup.hexed.Config;
import net.backupcup.hexed.Hexed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Keys for every hex. Since 1.21 the enchantments themselves are data-driven
 * ({@code data/hexed/enchantment/*.json}); their behavior lives in the mixins and {@link HexEffects}.
 */
public final class Hexes {
    private static final Map<ResourceKey<Enchantment>, Predicate<Config>> ENABLED = new LinkedHashMap<>();

    //WEAPON
    public static final ResourceKey<Enchantment> AFLAME = hex("aflame", c -> c.aflameHex.shouldRegister);
    public static final ResourceKey<Enchantment> PERSECUTED = hex("persecuted", c -> c.persecutedHex.shouldRegister);
    public static final ResourceKey<Enchantment> EPHEMERAL = hex("ephemeral", c -> c.ephemeralHex.shouldRegister);
    public static final ResourceKey<Enchantment> VINDICTIVE = hex("vindictive", c -> c.vindictiveHex.shouldRegister);
    public static final ResourceKey<Enchantment> TRAITOROUS = hex("traitorous", c -> c.traitorousHex.shouldRegister);

    //ARMOR
    public static final ResourceKey<Enchantment> DISPLACED = hex("displaced", c -> c.displacedHex.shouldRegister);
    public static final ResourceKey<Enchantment> AVERTING = hex("averting", c -> c.avertingHex.shouldRegister);
    public static final ResourceKey<Enchantment> AQUATIQUE = hex("aquatique", c -> c.aquatiqueHex.shouldRegister);
    public static final ResourceKey<Enchantment> DYNAMIQUE = hex("dynamique", c -> c.dynamiqueHex.shouldRegister);
    public static final ResourceKey<Enchantment> IRONCLAD = hex("ironclad", c -> c.ironcladHex.shouldRegister);
    public static final ResourceKey<Enchantment> FRANTIC = hex("frantic", c -> c.franticHex.shouldRegister);
    public static final ResourceKey<Enchantment> BLOODTHIRSTY = hex("bloodthirsty", c -> c.bloodthirstyHex.shouldRegister);
    public static final ResourceKey<Enchantment> DISFIGUREMENT = hex("disfigurement", c -> c.disfigurementHex.shouldRegister);
    public static final ResourceKey<Enchantment> METAMORPHOSIS = hex("metamorphosis", c -> c.metamorphosisHex.shouldRegister);
    public static final ResourceKey<Enchantment> DIVINE = hex("divine", c -> c.divineHex.shouldRegister);

    //BOW
    public static final ResourceKey<Enchantment> AGGRAVATE = hex("aggravate", c -> c.aggravateHex.shouldRegister);
    public static final ResourceKey<Enchantment> VOLATILITY = hex("volatility", c -> c.volatilityHex.shouldRegister);
    public static final ResourceKey<Enchantment> PHASED = hex("phased", c -> c.phasedHex.shouldRegister);
    public static final ResourceKey<Enchantment> RESENTFUL = hex("resentful", c -> c.resentfulHex.shouldRegister);

    //CROSSBOW
    public static final ResourceKey<Enchantment> CELEBRATION = hex("celebration", c -> c.celebrationHex.shouldRegister);
    public static final ResourceKey<Enchantment> PROVISION = hex("provision", c -> c.provisionHex.shouldRegister);
    public static final ResourceKey<Enchantment> OVERCLOCK = hex("overclock", c -> c.overclockHex.shouldRegister);

    //TRIDENT
    public static final ResourceKey<Enchantment> FLARING = hex("flaring", c -> c.flaringHex.shouldRegister);
    public static final ResourceKey<Enchantment> LINGER = hex("linger", c -> c.lingerHex.shouldRegister);
    public static final ResourceKey<Enchantment> SEIZE = hex("seize", c -> c.seizeHex.shouldRegister);
    public static final ResourceKey<Enchantment> SEPULTURE = hex("sepulture", c -> c.sepultureHex.shouldRegister);

    //DIGGER
    public static final ResourceKey<Enchantment> RUINOUS = hex("ruinous", c -> c.ruinousHex.shouldRegister);
    public static final ResourceKey<Enchantment> AMPLIFY = hex("amplify", c -> c.amplifyHex.shouldRegister);
    public static final ResourceKey<Enchantment> OVERBURDEN = hex("overburden", c -> c.overburdenHex.shouldRegister);
    public static final ResourceKey<Enchantment> FAMISHMENT = hex("famishment", c -> c.famishmentHex.shouldRegister);

    public static final List<ResourceKey<Enchantment>> ALL = Collections.unmodifiableList(new ArrayList<>(ENABLED.keySet()));

    private Hexes() {}

    private static ResourceKey<Enchantment> hex(String name, Predicate<Config> enabled) {
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, Hexed.id(name));
        ENABLED.put(key, enabled);
        return key;
    }

    public static boolean isHex(ResourceKey<Enchantment> key) {
        return ENABLED.containsKey(key);
    }

    /** Whether the config lets this hex be applied at the altar. */
    public static boolean isEnabled(ResourceKey<Enchantment> key) {
        Config config = Hexed.getConfig();
        return config == null || ENABLED.getOrDefault(key, c -> false).test(config);
    }

    public static ResourceLocation runeTexture(ResourceKey<Enchantment> key) {
        return Hexed.id("textures/gui/runes/" + key.location().getPath() + ".png");
    }

    public static String translationKey(ResourceLocation id) {
        return "enchantment." + id.getNamespace() + "." + id.getPath();
    }
}
