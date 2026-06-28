package net.darkhax.botanypots.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.darkhax.botanypots.BotanyPotHelper;
import net.darkhax.botanypots.data.displaystate.DisplayState;
import net.darkhax.botanypots.data.displaystate.render.DisplayStateRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BotanyPotRenderer implements BlockEntityRenderer<BlockEntityBotanyPot> {

    public BotanyPotRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(BlockEntityBotanyPot pot, float tickDelta, PoseStack pose, MultiBufferSource bufferSource, int light, int overlay) {

        if (pot.getSoil() != null) {

            final Level level = pot.getLevel();
            final BlockPos pos = pot.getBlockPos();

            final int maxGrowth = pot.getInventory().getRequiredGrowthTime();
            final float partialOffset = pot.getGrowthTime() < maxGrowth ? tickDelta : 0f;
            final float growthProgress = Math.max((pot.getGrowthTime() + partialOffset) / maxGrowth, 0f);

            // ---- 绘制土壤（保留原样） ----
            pose.pushPose();
            pose.scale(0.625f, 0.375f, 0.625f);
            pose.translate(0.3, 0.0625, 0.3);
            DisplayStateRenderer.renderState(pot.getSoil().getDisplayState(level, pos, pot), pose, level, pos, bufferSource, light, overlay, growthProgress);
            pose.popPose();

            // ---- 绘制作物（修改：固定成熟形态，无动画） ----
            if (pot.getCrop() != null && BotanyPotHelper.canCropGrow(level, pos, pot, pot.getSoil(), pot.getCrop())) {

                pose.pushPose();

                pose.translate(0.5, 0.40625, 0.5);

                // 固定缩放为最大（成熟大小）
                float matureScale = 0.625f;
                pose.scale(matureScale, matureScale, matureScale);

                pose.translate(-0.5, 0, -0.5);

                int layerIndex = 0;
                // 遍历所有显示层，保持堆叠，但固定生长进度为 1.0
                for (DisplayState state : pot.getCrop().getDisplayState(level, pos, pot)) {

                    if (layerIndex > 0) {
                        pose.translate(0, 1, 0);
                    }

                    pose.pushPose();
                    // 固定生长进度为 1.0（完全成熟），取消动画
                    DisplayStateRenderer.renderState(state, pose, level, pos, bufferSource, light, overlay, 1.0f);
                    pose.popPose();

                    layerIndex++;
                }

                pose.popPose();
            }
        }
    }
}
