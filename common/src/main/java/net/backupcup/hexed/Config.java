package net.backupcup.hexed;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.DeserializationException;
import blue.endless.jankson.api.SyntaxError;
import net.backupcup.hexed.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Server config, saved as {@code config/hexed/config.json5} and synced to clients on join and on {@code /reload}.
 * Field names are part of the file format, so they keep their original spelling.
 */
@SuppressWarnings("unused")
public class Config {
    private static final String SKIP = "\n\n";

    @Nullable
    public static Component lastError = null;

    private static final Jankson JANKSON = Jankson.builder()
        .registerSerializer(ResourceLocation.class, (id, marshaller) -> marshaller.serialize(id.toString()))
        .registerDeserializer(String.class, ResourceLocation.class, (str, marshaller) -> ResourceLocation.tryParse(str))
        .build();

    private static File getConfigFile() {
        return Services.PLATFORM.getConfigDir().resolve(Hexed.MOD_ID).resolve("config.json5").toFile();
    }

    public static Config load() {
        Config defaults = new Config();
        try {
            File file = getConfigFile();
            if (file.exists()) {
                lastError = null;
                JsonObject json = JANKSON.load(file);
                boolean migrated = migrate(json);
                Config config = JANKSON.fromJsonCarefully(json, Config.class);
                if (migrated) config.save();
                return config;
            }
            defaults.save();
            lastError = null;
            return defaults;
        } catch (SyntaxError e) {
            Hexed.LOGGER.error("Config syntax error. {}.", e.getLineMessage());
            Hexed.LOGGER.error(e.getMessage());
        } catch (DeserializationException e) {
            Hexed.LOGGER.error("Hexed config deserialization error.");
            Hexed.LOGGER.error("{}", e.getMessage());
            if (e.getCause() != null) {
                Hexed.LOGGER.error("Cause: {}", e.getCause().getMessage());
            }
        } catch (IOException e) {
            Hexed.LOGGER.error("IO exception occurred while reading config. Using defaults.");
            Hexed.LOGGER.error(e.getMessage());
        }
        Hexed.LOGGER.warn("Using default configuration.");
        lastError = Component.translatable("message.hexed.error.config.general");
        return defaults;
    }

    /** Renames keys from older versions of the file. Returns whether anything changed. */
    private static boolean migrate(JsonObject json) {
        boolean changed = false;
        // Aflame used to put Ablaze on the target and fire on the wielder; both now get Aflame.
        if (json.get("aflameHex") instanceof JsonObject aflame) {
            changed |= rename(aflame, "AblazeDuration", "targetDuration");
            changed |= rename(aflame, "AblazeAmplifier", "targetAmplifier");
            changed |= rename(aflame, "AflameDuration", "userDuration");
            changed |= rename(aflame, "AflameAmplifier", "userAmplifier");
        }
        if (changed) Hexed.LOGGER.info("Updated old Hexed config keys");
        return changed;
    }

    private static boolean rename(JsonObject object, String from, String to) {
        if (!object.containsKey(from)) return false;
        JsonElement value = object.remove(from);
        if (!object.containsKey(to)) object.put(to, value);
        return true;
    }

    @Nullable
    public static Config fromJson(String json) {
        try {
            return JANKSON.fromJsonCarefully(json, Config.class);
        } catch (SyntaxError | DeserializationException e) {
            Hexed.LOGGER.error("Error while retrieving config from server", e);
            return null;
        }
    }

    public void save() {
        File file = getConfigFile();
        file.getParentFile().mkdirs();
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(JANKSON.toJson(this).toJson(true, true).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Hexed.LOGGER.error("IO exception while saving config: {}", e.getMessage());
        }
    }

    public String toJson() {
        return JANKSON.toJson(this).toJson();
    }

    public enum ListType {
        ALLOW,
        DENY
    }

    @Comment("List type\n" +
        "ALLOW - whitelist, only the items in this list WILL have the ability to be hexed\n" +
        "DENY - blacklist, all the items in this list WONT have the ability to be hexed")
    private ListType listType = ListType.DENY;

    @Comment("An item list. What the list does depends on the \"listType\"")
    private String[] itemList = new String[0];

    public boolean isListed(Item item) {
        boolean contained = Arrays.asList(itemList).contains(BuiltInRegistries.ITEM.getKey(item).toString());
        return listType == ListType.DENY ? contained : !contained;
    }

    @Comment("Whether the \"Calamitous\" Full armor effect should apply.")
    private boolean fullArmorEffectApply = true;

    public boolean shouldArmorApply() {
        return fullArmorEffectApply;
    }

    //AFLAME
    public static class AflameHexParams {
        public boolean shouldRegister = true;

        public int targetDuration = 80;
        public int targetAmplifier = 1;

        public int userDuration = 40;
        public int userAmplifier = 0;
    }

    @Comment("\"Aflame\" Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "targetDuration/Amplifier - The duration (in ticks) & the amplifier of the Aflame burn on whoever is hit\n" +
        "userDuration/Amplifier - The duration (in ticks) & the amplifier of the Aflame burn on the wielder\n" +
        "Aflame deals 1 + amplifier damage per second and ignores effect immunity, except on someone wearing the full Calamitous set")
    public AflameHexParams aflameHex = new AflameHexParams();

    //AMPLIFY
    public static class AmplifyHexParams {
        public boolean shouldRegister = true;

        public double dropChance = 0.5;
        public double exhaustionAmount = 0.01;
        public int toolDamage = 1;
    }

    @Comment("Amplify Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "dropChance - chance for the additional mined blocks to drop\n" +
        "exhaustionAmount - how much exhaustion is applied for each additional mined block\n" +
        "toolDamage - how much durability is lost per additional mined block")
    public AmplifyHexParams amplifyHex = new AmplifyHexParams();

    //AQUATIQUE
    public static class AquatiqueHexParams {
        public boolean shouldRegister = true;

        public float speedMultiplier = 2.0f;
    }

    @Comment("Aquatique Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "speedMultiplier - by how much the movement speed in water is multiplied (Anything below 1 will actually make the wearer slower)")
    public AquatiqueHexParams aquatiqueHex = new AquatiqueHexParams();

    //AVERTING
    public static class AvertingHexParams {
        public boolean shouldRegister = true;

        public double damageReduction = 0.125;
        public int avertingCooldown = 50;
    }

    @Comment("Averting Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "damageReduction - how much each armor piece with Averting adds to the chance of negating damage (Percentage value, 0.125 = 12.5%)\n" +
        "avertingCooldown - for how much time (in ticks) the invinsibility frames will be taken away after dodging damage")
    public AvertingHexParams avertingHex = new AvertingHexParams();

    //BLOODTHIRSTY
    public static class BloodthirstyHexParams {
        public boolean shouldRegister = true;

        public float minimumHealAmount = 3f;
        public float maximumHealAmount = 10f;
        public float healModifier = 0.125f;
    }

    @Comment("Bloodthirsty Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "minimumHealAmount - minimum amount of Health that will be regenerated\n" +
        "maximumHealAmount - maximum amount of Health that will be regenerated\n" +
        "healModifier - heal scaling amount (Percentage value, 0.125 = 12.5%)")
    public BloodthirstyHexParams bloodthirstyHex = new BloodthirstyHexParams();

    //CELEBRATION
    public static class CelebrationHexParams {
        public boolean shouldRegister = true;

        public float minimumDivergence = -5.0f;
        public float maximumDivergence = 5.0f;
        public double faulyChance = 0.333;
        public float faultySpeed = 0.0625f;
    }

    @Comment("Celebration Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "minimumDivergence - minimum variation of the firework's direction\n" +
        "maximumDivergence - maximum variation of the firework's direction\n" +
        "faulyChance - the chance that the launched firework will be faulty (Percentage value, 0.333 = 33.3%)\n" +
        "faultySpeed - speed of the faulty firework")
    public CelebrationHexParams celebrationHex = new CelebrationHexParams();

    //DISFIGUREMENT
    public static class DisfigurementHexParams {
        public boolean shouldRegister = true;

        public int boxRadius = 3;
        public int hungerDuration = 100;
        public int hungerAmplifier = 0;
        public int weaknessDuration = 100;
        public int weaknessAmplifier = 0;
    }

    @Comment("Disfigurement Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "boxRadius - radius of the effect\n" +
        "hungerDuration/Amplifier - The duration (in ticks) & the amplifier of the Hunger debuff\n" +
        "weaknessDuration/Amplifier - The duration (in ticks) & the amplifier of the Weakness debuff\n")
    public DisfigurementHexParams disfigurementHex = new DisfigurementHexParams();

    //DISPLACED
    public static class DisplacedHexParams {
        public boolean shouldRegister = true;
    }

    @Comment("Displaced Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable")
    public DisplacedHexParams displacedHex = new DisplacedHexParams();

    //DIVINE
    public static class DivineHexParams {
        public boolean shouldRegister = true;

        public int defaultDuration = 20;
        public int defaultAmplifier = 0;

        public int ironcladDuration = 40;
        public int ironcladAmplifier = 3;

        public int exhaustionDuration = 60;
        public int exhaustionAmplifier = 9;

        public int etherealDuration = 80;
        public int etherealAmplifier = 5;
    }

    @Comment("Divine Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "defaultDuration/Amplifier - The duration (in seconds) & the amplifier of the Ablaze & Traitorous debuffs\n" +
        "ironcladDuration/Amplifier - The duration (in seconds) & the amplifier of the Ironclad debuffs\n" +
        "exhaustionDuration/Amplifier - The duration (in seconds) & the amplifier of the Exhaustion debuffs\n" +
        "etherealDuration/Amplifier - The duration (in seconds) & the amplifier of the Ethereal debuffs\n")
    public DivineHexParams divineHex = new DivineHexParams();

    //DYNAMIQUE
    public static class DynamiqueHexParams {
        public boolean shouldRegister = true;

        public float jumpModifier = 0.15625f;
    }

    @Comment("Dynamique Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "jumpModifier - the additional amount of velocity the user will receive (0.15625 = 1 block)")
    public DynamiqueHexParams dynamiqueHex = new DynamiqueHexParams();

    //EPHEMERAL
    public static class EphemeralHexParams {
        public boolean shouldRegister = true;

        public float damageAddition = 4f;
        public int debuffDuration = 150;
        public int debuffDecayLength = 50;
    }

    @Comment("Ephemeral Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "damageAddition - how much more damage the weapon does at first\n" +
        "debuffDuration - the initial duration of the debuff (in ticks)\n" +
        "debuffDecayLength - how much each additional level of the debuff lasts\n")
    public EphemeralHexParams ephemeralHex = new EphemeralHexParams();

    //FAMISHMENT
    public static class FamishmentHexParams {
        public boolean shouldRegister = true;
    }

    @Comment("Famishment Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable")
    public FamishmentHexParams famishmentHex = new FamishmentHexParams();

    //FLARING
    public static class FlaringHexParams {
        public boolean shouldRegister = true;

        public boolean isSoulFire = false;
    }

    @Comment("Flaring Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "isSoulFire - whether the fire spawned should be Soul Fire")
    public FlaringHexParams flaringHex = new FlaringHexParams();

    //FRANTIC
    public static class FranticHexParams {
        public boolean shouldRegister = true;

        public int franticDuration = 100;
        public int franticDecayLength = 50;

        public float damageModifier = 2f;
    }

    @Comment("Frantic Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "franticDuration - the initial duration of the debuff (in ticks)\n" +
        "franticDecayLength - how much each additional level of the debuff lasts\n" +
        "damageModifier - the amplifier of the debuff is divided by this to get the actual damage modifier\n" +
        "Formula: damage = damage * (1 + (FranticDebuffLevel / damageModifier))")
    public FranticHexParams franticHex = new FranticHexParams();

    //IRONCLAD
    public static class IroncladHexParams {
        public boolean shouldRegister = true;

        public int debuffDuration = 200;
        public int debuffDecayLength = 10;
        public int robesDebuffModifier = 2;
        public float damageReductionAmount = 0.33f;
    }

    @Comment("Ironclad Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "debuffDuration - the initial duration of the debuff (in ticks)\n" +
        "debuffDecayLength - how much each additional level of the debuff lasts\n" +
        "robesDebuffModifier - by how much the initial duration of the debuff is divided\n" +
        "damageReductionAmount - the amount of damage reduction this hex provides\n" +
        "Formula: damage = damage * (1 - damageReductionAmount)")
    public IroncladHexParams ironcladHex = new IroncladHexParams();

    //LINGER
    public static class LingerHexParams {
        public boolean shouldRegister = true;

        public int lingerRadius = 3;
        public int debuffDuration = 25;
        public int debuffAmplifier = 0;
        public int tridentDamage = 1;
    }

    @Comment("Linger Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "lingerRadius - the radius from the trident in which the debuff is applied\n" +
        "debuffDuration/Amplifier - The duration (in seconds) & the amplifier of the Ablaze debuff\n" +
        "tridentDamage - the amount of durability damage the trident receives overtime (This is affected by the amount of creatures in the radius)")
    public LingerHexParams lingerHex = new LingerHexParams();

    //METAMORPHOSIS
    public static class MetamorphosisHexParams {
        public boolean shouldRegister = true;

        public float foodModifier = 1f;
        public float saturationAmount = 0f;
    }

    @Comment("Metamorphosis Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "foodModifier - modifies the amount of food recieved per half heart (anything below 1 will nullify the effect of the hex)\n" +
        "saturationAmount - how much saturation is applied")
    public MetamorphosisHexParams metamorphosisHex = new MetamorphosisHexParams();

    //OVERBURDEN
    public static class OverburdenHexParams {
        public boolean shouldRegister = true;

        public int buffDuration = 200;
        public float movementSpeedModifier = 0.025f;
    }

    @Comment("Overburden Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "buffDuration - the initial duration of the buff (in ticks)\n" +
        "movementSpeedModifier - how much speed is removed for each level of the buff")
    public OverburdenHexParams overburdenHex = new OverburdenHexParams();

    //PERSECUTED
    public static class PersecutedHexParams {
        public boolean shouldRegister = true;

        public float healthCap = 25f;
        public int debuffDurationModifier = 10;
    }

    @Comment("Persecuted Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "healthCap - the maximum amount of damage a weapon with this hex can deal\n" +
        "debuffDurationModifier - a modifier determining how much the debuff lasts for")
    public PersecutedHexParams persecutedHex = new PersecutedHexParams();

    //RUINOUS
    public static class RuinousHexParams {
        public boolean shouldRegister = true;

        public float explosionPower = 1.25f;
    }

    @Comment("Ruinous Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "explosionPower - the power of the explosion")
    public RuinousHexParams ruinousHex = new RuinousHexParams();

    //SEIZE
    public static class SeizeHexParams {
        public boolean shouldRegister = true;

        public int maxPullableHP = 50;
        public double minimumPullStrength = 1.0;
        public double maximumPullStrength = 4.0;
    }

    @Comment("Seize Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "maxPullableHP - determines whether the creature can be pulled based on it's max Health\n" +
        "minimumPullStrength - the minimum strength of the pull if the trident doesn't have Loyalty\n" +
        "maximumPullStrength - the maximum strength of the pull if the trident doesn't have Loyalty\n")
    public SeizeHexParams seizeHex = new SeizeHexParams();

    //SEPULTURE
    public static class SepultureHexParams {
        public boolean shouldRegister = true;

        public double angerChance = 0.333;
        public float explosionPower = 1.5f;
    }

    @Comment("Sepulture Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "angerChance - chance that a skull will target the user instead of the nearest entity\n" +
        "explosionPower - the power of the produced explosion\n")
    public SepultureHexParams sepultureHex = new SepultureHexParams();

    //TRAITOROUS
    public static class TraitorousHexParams {
        public boolean shouldRegister = true;

        public int debuffDuration = 80;
        public int cooldownDuration = 40;
    }

    @Comment("Traitorous Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "debuffDuration - The duration (in ticks) of the Traitorous debuff\n" +
        "cooldownDuration - The duration (in ticks) of the weapon cooldown\n")
    public TraitorousHexParams traitorousHex = new TraitorousHexParams();

    //VINDICTIVE
    public static class VindictiveHexParams {
        public boolean shouldRegister = true;

        public int maxAmplifier = 10;

        public int vindictiveDuration = 60;
        public int vindictiveDecayLength = 20;

        public int smoulderingDuration = 60;
        public int smoulderingDecayLength = 20;
    }

    @Comment("Vindictive Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "maxAmplifier - max amount of damage this hex can do when the debuff runs out\n" +
        "vindictiveDuration - the initial duration of the Vindictive debuff (in ticks)\n" +
        "vindictiveDecayLength - how much each additional level of the Vindictive debuff lasts\n" +
        "smoulderingDuration - the initial duration of the Smouldering debuff (in ticks)\n" +
        "smoulderingDecayLength - how much each additional level of the Smouldering debuff lasts")
    public VindictiveHexParams vindictiveHex = new VindictiveHexParams();

    //AGGRAVATE
    public static class AggravateHexParams {
        public boolean shouldRegister = true;

        public int chargeTicks = 10;
        public int arrowsPerCharge = 1;
        public float explosionPower = 2f;
    }

    @Comment("Aggravate Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "chargeTicks - How much ticks does it take to add a single charge\n" +
        "arrowsPerCharge - How much arrows is a single charge equal to\n" +
        "explosionPower - The power of the explosion on an overcharge")
    public AggravateHexParams aggravateHex = new AggravateHexParams();

    //VOLATILITY
    public static class VolatilityHexParams {
        public boolean shouldRegister = true;

        public float explosionPower = 1f;
    }

    @Comment("Volatility Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "explosionPower - the power of the explosion")
    public VolatilityHexParams volatilityHex = new VolatilityHexParams();

    //PHASED
    public static class PhasedHexParams {
        public boolean shouldRegister = true;

        public int hitscanDistance = 64;
        public float initialDamage = 5f;
        public float damageMultiplier = 0.4f;
        public float powerModifier = 1f;
        public int flameSeconds = 5;
    }

    @Comment("Phased Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "initialDamage - the initial damage the hitscan does\n" +
        "damageMultiplier - by how much the initial damage is multiplied for each pierced enemy\n" +
        "powerModifier - how much damage does a level of Power adds to the initial damage (Setting to 0 will make hitscan ignore Power)\n" +
        "flameSeconds - for how much seconds the enemy will be set on fire (Settings to 0 will make hitscan ignore Flame)")
    public PhasedHexParams phasedHex = new PhasedHexParams();

    //PROVISION
    public static class ProvisionHexParams {
        public boolean shouldRegister = true;

        public int maxReloadSpeed = 10;
        public int itemCooldown = 80;
    }

    @Comment("Provision Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "maxReloadSpeed - the max speed the reload indicator can have\n" +
        "itemCooldown - the cooldown penalty in ticks\n")
    public ProvisionHexParams provisionHex = new ProvisionHexParams();

    //OVERCLOCK
    public static class OverclockHexParams {
        public boolean shouldRegister = true;

        public int overheatLasting = 200;
    }

    @Comment("Overclock Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "overheatLasting - how much ticks will the game wait before removing overheat stacks\n")
    public OverclockHexParams overclockHex = new OverclockHexParams();

    //RESENTFUL
    public static class ResentfulHexParams {
        public boolean shouldRegister = true;

        public double increasePoint = 16.0;
        public double maxIncreaseRange = 80.0;
    }

    @Comment("Resentful Hex settings" + SKIP +
        "shouldRegister - whether the hex should be obtainable\n" +
        "increasePoint - At which distance the damage starts to increase\n" +
        "maxIncreaseRange - At which distance the damage stops increasing\n")
    public ResentfulHexParams resentfulHex = new ResentfulHexParams();
}
