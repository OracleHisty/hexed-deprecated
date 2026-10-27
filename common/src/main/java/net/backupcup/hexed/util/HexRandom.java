package net.backupcup.hexed.util;

import net.minecraft.util.RandomSource;

/** Thread-safe random helpers. The integer versions round up, matching the original behavior. */
public final class HexRandom {
    private static final RandomSource RANDOM = RandomSource.createThreadSafe();

    private HexRandom() {}

    public static boolean nextBoolean() {
        return RANDOM.nextBoolean();
    }

    public static int nextInt(int max) {
        return (int) Math.ceil(max * RANDOM.nextFloat());
    }

    public static int nextInt(int min, int max) {
        return (int) Math.ceil(min + (max - min) * RANDOM.nextFloat());
    }

    public static double nextDouble() {
        return RANDOM.nextDouble();
    }

    public static double nextDouble(double min, double max) {
        return min + (max - min) * RANDOM.nextDouble();
    }

    public static float nextFloat() {
        return RANDOM.nextFloat();
    }

    public static float nextFloat(float min, float max) {
        return min + (max - min) * RANDOM.nextFloat();
    }
}
