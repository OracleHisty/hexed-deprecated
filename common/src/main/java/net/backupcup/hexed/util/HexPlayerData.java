package net.backupcup.hexed.util;

/** Per-player hex state, mixed into {@code ServerPlayer}. */
public interface HexPlayerData {
    float hexed$getPullPredicate();

    void hexed$setPullPredicate(float value);

    ProvisionData hexed$getProvisionData();

    void hexed$setProvisionData(ProvisionData data);

    AggravateData hexed$getAggravateData();

    void hexed$setAggravateData(AggravateData data);

    OverclockData hexed$getOverclockData();

    void hexed$setOverclockData(OverclockData data);

    int hexed$getPhasedCharge();

    void hexed$setPhasedCharge(int value);
}
