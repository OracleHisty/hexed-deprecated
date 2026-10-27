package net.backupcup.hexed.util;

import net.minecraft.world.InteractionHand;

public class AggravateData {
    public InteractionHand bowHand;
    public int chargeAmount;
    public long chargedTicks;

    public AggravateData(InteractionHand bowHand, int chargeAmount, long chargedTicks) {
        this.bowHand = bowHand;
        this.chargeAmount = chargeAmount;
        this.chargedTicks = chargedTicks;
    }
}
