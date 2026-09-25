package MoreBreedingOptimize.MD.JNHC;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BZQHEffect extends MobEffect {

    public BZQHEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x9B59B6);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            Level level = entity.level();
            BlockPos center = player.blockPosition();

            // 1. 在玩家周围 5x5x5 范围内清理所有旧光源（防止残留）
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        if (state.is(Blocks.LIGHT)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }

            // 2. 在玩家头顶上方一格放置新光源（亮度15）
            BlockPos lightPos = center.above();
            BlockState lightState = Blocks.LIGHT.defaultBlockState()
                    .setValue(LightBlock.LEVEL, 15);
            level.setBlock(lightPos, lightState, 3);
        }
        return true;
    }

    public void onEffectRemoved(LivingEntity entity, MobEffectInstance instance) {
        if (entity instanceof Player player) {
            Level level = entity.level();
            BlockPos center = player.blockPosition();

            // 效果结束时，清理周围所有光源
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        if (state.is(Blocks.LIGHT)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}