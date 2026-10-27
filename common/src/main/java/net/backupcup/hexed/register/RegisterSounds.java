package net.backupcup.hexed.register;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.registry.Reg;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class RegisterSounds {
    public static final RegObject<SoundEvent> ACCURSED_ALTAR_HEX = sound("accursed_altar_hex");
    public static final RegObject<SoundEvent> ACCURSED_ALTAR_TAINT = sound("accursed_altar_taint");
    public static final RegObject<SoundEvent> ACCURSED_ALTAR_ACTIVATE = sound("accursed_altar_activate");

    public static final RegObject<SoundEvent> BEEP_ALT = sound("beep_alt");

    public static final RegObject<SoundEvent> PROVISION_FAIL = sound("provision_fail");
    public static final RegObject<SoundEvent> PROVISION_CHARGE = sound("provision_charge");
    public static final RegObject<SoundEvent> PROVISION_IN_RANGE = sound("provision_in_range");

    public static final RegObject<SoundEvent> OVERCLOCK_TIER = sound("overclock_tier");
    public static final RegObject<SoundEvent> AGGRAVATE_TIER = sound("aggravate_tier");

    public static final RegObject<SoundEvent> PHASED_SHOT = sound("phased_shot");
    public static final RegObject<SoundEvent> PHASED_TIER = sound("phased_tier");

    public static final RegObject<SoundEvent> ELECTRIC_CHARGE = sound("electric_charge");
    public static final RegObject<SoundEvent> ELECTRIC_DISCHARGE = sound("electric_discharge");

    public static final RegObject<SoundEvent> TECH_LAUGH = sound("tech_laugh");

    public static final RegObject<SoundEvent> YIPPEE = sound("yippee");

    private RegisterSounds() {}

    private static RegObject<SoundEvent> sound(String name) {
        return Reg.register(BuiltInRegistries.SOUND_EVENT, name, () -> SoundEvent.createVariableRangeEvent(Hexed.id(name)));
    }

    public static void init() {}
}
