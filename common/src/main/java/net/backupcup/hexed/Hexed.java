package net.backupcup.hexed;

import net.backupcup.hexed.flame.HexedFlames;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.backupcup.hexed.network.HexUiUpdatePayload;
import net.backupcup.hexed.network.HexedNetwork;
import net.backupcup.hexed.network.SyncConfigPayload;
import net.backupcup.hexed.register.*;
import net.backupcup.hexed.util.HexRandom;
import net.backupcup.hexed.util.TimedEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Hexed {
    public static final String MOD_ID = "hexed";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Nullable
    private static Config config;

    private Hexed() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Nullable
    public static Config getConfig() {
        return config;
    }

    /** The current config, or the defaults when none has been loaded or synced yet. */
    public static Config config() {
        Config current = config;
        return current != null ? current : DEFAULTS;
    }

    private static final Config DEFAULTS = new Config();

    public static void setConfig(Config newConfig) {
        config = newConfig;
    }

    /** Called from each loader's mod initializer. */
    public static void init() {
        config = Config.load();

        RegisterDataComponents.init();
        RegisterSounds.init();
        RegisterStatusEffects.init();
        RegisterBlocks.init();
        RegisterItems.init();
        RegisterBlockEntities.init();
        RegisterEntities.init();
        RegisterMenus.init();
        RegisterStats.init();
        RegisterCreativeTabs.init();

        HexedFlames.init();
    }

    /** Reloads the config whenever the server reloads its data packs. */
    public static final ResourceManagerReloadListener CONFIG_RELOADER = new ResourceManagerReloadListener() {
        @Override
        public void onResourceManagerReload(ResourceManager manager) {
            config = Config.load();
        }

        @Override
        public String getName() {
            return id("config").toString();
        }
    };

    public static void onServerTick() {
        TimedEvents.tick();
    }

    public static void onServerStopped() {
        TimedEvents.clear();
    }

    /** Called when data packs are sent to a player, both on join and after {@code /reload}. */
    public static void onDatapackSync(ServerPlayer player, boolean joined) {
        HexedNetwork.sendToPlayer(player, new SyncConfigPayload(config().toJson()));
        if (Config.lastError != null) {
            player.sendSystemMessage(Component.literal("[" + MOD_ID.toUpperCase() + "]: ")
                .append(Config.lastError).withStyle(ChatFormatting.RED));
        }

        if (joined) {
            for (HexUiUpdatePayload.Ui ui : HexUiUpdatePayload.Ui.values()) {
                HexedNetwork.sendToPlayer(player, new HexUiUpdatePayload(ui, false, 0));
            }
        }
    }

    /** Runs after a player breaks a block with a tool. */
    public static void onPostMine(ItemStack tool, Level level, BlockState state, BlockPos pos, Player player) {
        if (level.isClientSide() || !tool.isCorrectToolForDrops(state)) return;

        amplifyMine(tool, level, pos, state, player);
        overburdenedStack(tool, player);
        ruinousExplode(tool, level, pos, player);
    }

    private static void ruinousExplode(ItemStack tool, Level level, BlockPos pos, Player miner) {
        if (!HexHelper.has(tool, Hexes.RUINOUS)) return;

        Entity source = HexHelper.hasFullRobes(miner) ? miner : null;
        level.explode(source,
            pos.getX() + HexRandom.nextDouble(-0.25, 1.25),
            pos.getY() + HexRandom.nextDouble(-0.25, 1.25),
            pos.getZ() + HexRandom.nextDouble(-0.25, 1.25),
            config().ruinousHex.explosionPower,
            Level.ExplosionInteraction.BLOCK);
    }

    private static void overburdenedStack(ItemStack tool, LivingEntity miner) {
        if (!HexHelper.has(tool, Hexes.OVERBURDEN)) return;

        var current = miner.getEffect(RegisterStatusEffects.OVERBURDEN.holder());
        if ((current != null ? current.getAmplifier() : 0) < 256) {
            HexHelper.multiplyingEffect(miner, RegisterStatusEffects.OVERBURDEN.holder(),
                config().overburdenHex.buffDuration, HexHelper.hasFullRobes(miner) ? 0 : 1);
        }
    }

    private static void amplifyMine(ItemStack tool, Level level, BlockPos pos, BlockState state, Player player) {
        if (player.isShiftKeyDown() || !HexHelper.has(tool, Hexes.AMPLIFY)) return;

        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 look = player.getLookAngle();
        double dx = Math.abs((pos.getX() + 0.5 - eye.x) * look.x);
        double dy = Math.abs((pos.getY() + 0.5 - eye.y) * look.y);
        double dz = Math.abs((pos.getZ() + 0.5 - eye.z) * look.z);
        double max = Math.max(dx, Math.max(dy, dz));

        // Break the 3x3 plane facing the player.
        int rx = 1, ry = 1, rz = 1;
        if (max == dx) rx = 0;
        else if (max == dy) ry = 0;
        else rz = 0;

        for (int x = pos.getX() - rx; x <= pos.getX() + rx; x++) {
            for (int y = pos.getY() - ry; y <= pos.getY() + ry; y++) {
                for (int z = pos.getZ() - rz; z <= pos.getZ() + rz; z++) {
                    BlockPos target = new BlockPos(x, y, z);
                    if (target.equals(pos)) continue;
                    breakExtraBlock(target, level, player, tool, state);
                    if (tool.isEmpty()) return;
                }
            }
        }
    }

    private static void breakExtraBlock(BlockPos pos, Level level, Player player, ItemStack tool, BlockState origin) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !tool.isCorrectToolForDrops(state)
            || state.getBlock().defaultDestroyTime() > origin.getBlock().defaultDestroyTime() + 2) return;

        boolean shouldDrop = HexRandom.nextDouble() >= config().amplifyHex.dropChance || HexHelper.hasFullRobes(player);

        player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
        level.destroyBlock(pos, shouldDrop, player);
        player.awardStat(Stats.ITEM_USED.get(tool.getItem()));
        tool.hurtAndBreak(config().amplifyHex.toolDamage, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        player.causeFoodExhaustion((float) config().amplifyHex.exhaustionAmount);
    }
}
