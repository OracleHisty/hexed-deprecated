package net.backupcup.hexed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.entity.BlazingSkullEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// Made with Blockbench 4.9.4
public class BlazingSkullModel extends EntityModel<BlazingSkullEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Hexed.id("blazing_skull"), "skull");

    private final ModelPart skull;

    public BlazingSkullModel(ModelPart root) {
        this.skull = root.getChild("skull");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("skull",
            CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0f, -15.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.0f))
                .texOffs(7, 16).addBox(-3.0f, -7.0f, -3.5f, 6.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 27.0f, 0.0f));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(BlazingSkullEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        skull.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
