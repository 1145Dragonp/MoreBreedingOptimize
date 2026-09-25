package MoreBreedingOptimize.MD.DOGM;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class EternalEffect extends MobEffect {

    public EternalEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700); // 金色
    }

    // @AI(BAPI.MobEffect, applyEffectTick) 1.21.1 签名返回 boolean，返回 true 表示效果实际生效了
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            // 修正：getEffect 需要 Holder<MobEffect>，用注册表把自己包装成 Holder
            Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this);
            MobEffectInstance effectInstance = player.getEffect(holder);
            if (effectInstance == null) return false;

            int remainingTicks = effectInstance.getDuration();
            int totalDuration = 3600; // 总时长 180 秒 = 3600 ticks

            // 前 20 秒（400 ticks）：锁血
            if (remainingTicks >= totalDuration - 400) {
                player.setHealth(player.getMaxHealth());
            }

            // 全程锁饱食度和饱和度
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(5.0F);

            return true;
        }
        return false;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true; // 每 tick 都执行
    }
}
