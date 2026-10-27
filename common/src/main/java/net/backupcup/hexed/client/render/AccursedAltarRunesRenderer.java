package net.backupcup.hexed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.backupcup.hexed.altar.AccursedAltarBlockEntity;
import net.backupcup.hexed.register.RegisterItems;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Six runes circling above an active altar. */
public class AccursedAltarRunesRenderer implements BlockEntityRenderer<AccursedAltarBlockEntity> {
    private static final List<RegObject<Item>> RUNES = List.of(
        RegisterItems.RUNE_TRAITOROUS,
        RegisterItems.RUNE_DISPLACED,
        RegisterItems.RUNE_METAMORPHOSIS,
        RegisterItems.RUNE_AFLAME,
        RegisterItems.RUNE_AVERTING,
        RegisterItems.RUNE_DISFIGUREMENT
    );

    public AccursedAltarRunesRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(AccursedAltarBlockEntity altar, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        Level level = altar.getLevel();
        if (level == null || !altar.isActive()) return;

        int lightAbove = LevelRenderer.getLightColor(level, altar.getBlockPos().above());
        float time = level.getGameTime() + partialTick;
        Vec3 offset = new Vec3(0.0, 0.0, -1.0);

        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 2));

        for (int i = 0; i < RUNES.size(); i++) {
            poseStack.pushPose();
            int phase = i * 60;
            double bob = Math.sin((time + phase) / 8.0) / 4.0;
            poseStack.translate(offset.x, offset.y + bob, offset.z);
            poseStack.mulPose(Axis.YP.rotationDegrees((time + phase) * 0.25f));
            Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(RUNES.get(i).get()), ItemDisplayContext.GROUND,
                lightAbove, overlay, poseStack, buffers, level, 0);
            offset = offset.yRot(1.0472f);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
