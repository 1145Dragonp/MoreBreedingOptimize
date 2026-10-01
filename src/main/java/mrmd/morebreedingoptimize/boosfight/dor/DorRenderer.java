package mrmd.morebreedingoptimize.boosfight.dor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mrmd.morebreedingoptimize.boosfight.BoosFightItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DorRenderer extends EntityRenderer<DorEntity> {
    private final ItemRenderer itemRenderer;

    public DorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(DorEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.translate(0, 1.5F, 0);   // X=左右, Y=上下(负下沉), Z=前后
// 加这行调大小：
        poseStack.scale(3.0F, 3.0F, 3.0F);
        ItemStack stack = new ItemStack(BoosFightItem.dor.get());
        this.itemRenderer.renderStatic(
                stack, ItemDisplayContext.FIXED,
                packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DorEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}