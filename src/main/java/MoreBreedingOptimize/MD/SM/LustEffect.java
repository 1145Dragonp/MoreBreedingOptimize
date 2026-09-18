package MoreBreedingOptimize.MD.SM;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 发情效果
 *
 * 效果逻辑：
 * - 双方都有发情效果：紧贴在一起，每 6 秒繁殖一次
 * - 仅单方有发情效果：主动寻找伴侣，每 12 秒繁殖一次
 * - 冷却期间双方保持靠近，不会分开
 *
 * 防分离机制：
 * - 每 tick 先 stop() 清除原版 AI 的路径，再 moveTo() 强制导航向伴侣
 * - 同时强制设置速度向量指向伴侣，覆盖原版物理
 * - 冷却期间如果距离仍然过大（>3格），直接传送到伴侣身边
 *
 * 防双触发：双方都有发情时，仅 UUID 较小的一方执行繁殖逻辑。
 */
public class LustEffect extends MobEffect {

    private static final String COOLDOWN_TAG = "lust_cooldown";
    private static final double SEARCH_RADIUS = 16.0;       // 搜索半径
    private static final double BREED_DISTANCE = 2.5;       // 触发繁殖的最大距离
    private static final int COOLDOWN_MUTUAL = 120;         // 双方发情冷却：6秒 (120 tick)
    private static final int COOLDOWN_SINGLE = 240;         // 单方发情冷却：12秒 (240 tick)
    private static final double STICK_DISTANCE = 1.2;       // 冷却期间保持的贴近距离
    private static final double TELEPORT_THRESHOLD = 3.0;   // 超过此距离直接传送
    private static final double STICK_SPEED = 0.10;         // 强制靠近的速度

    public LustEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFC0CB);
    }

    /**
     * 获取自身的 Holder 引用（用于 hasEffect 检测）
     */
    private Holder<MobEffect> getSelfHolder() {
        try {
            ResourceLocation key = BuiltInRegistries.MOB_EFFECT.getKey(this);
            if (key != null) {
                return BuiltInRegistries.MOB_EFFECT.getHolder(key).orElseThrow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to get Holder for LustEffect!");
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return true;

        // --- 持续冒爱心粒子：每 5 tick 发射 2 个，制造浪漫氛围 ---
        if (entity.tickCount % 5 == 0) {
            ServerLevel serverLevel = (ServerLevel) entity.level();
            serverLevel.sendParticles(ParticleTypes.HEART,
                    entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 0.3,
                    entity.getY() + entity.getBbHeight() + 0.2,
                    entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 0.3,
                    2, 0.1, 0.1, 0.1, 0.02);
        }

        CompoundTag tag = entity.getPersistentData();

        // --- 冷却期间：强制保持靠近伴侣，不执行繁殖 ---
        int cooldown = tag.getInt(COOLDOWN_TAG);
        if (cooldown > 0) {
            tag.putInt(COOLDOWN_TAG, cooldown - 1);
            stickToPartner(entity);
            return true;
        }

        // --- 严格成年检测 ---
        if (entity instanceof Animal a && a.isBaby()) return true;
        if (entity instanceof Villager v && v.isBaby()) return true;

        // --- 搜索最近的成年同类 ---
        LivingEntity nearestAdult = findNearestAdultPartner(entity);
        if (nearestAdult == null) return true;

        double dist = entity.distanceTo(nearestAdult);

        // --- 未靠近：强制导航过去 ---
        if (dist > BREED_DISTANCE) {
            forceNavigateTo(entity, nearestAdult);
            return true;
        }

        // --- 已靠近：判断是否双方都有发情效果 ---
        Holder<MobEffect> selfHolder = getSelfHolder();
        boolean bothHaveLust = nearestAdult.hasEffect(selfHolder)
                && nearestAdult.getPersistentData().getInt(COOLDOWN_TAG) <= 0;

        // --- 防双触发：双方都有发情时，仅 UUID 较小的一方处理繁殖 ---
        if (bothHaveLust) {
            if (entity.getUUID().compareTo(nearestAdult.getUUID()) > 0) {
                return true; // 让对方处理
            }
        }

        // --- 执行繁殖 ---
        ServerLevel serverLevel = (ServerLevel) entity.level();
        AgeableMob baby = null;

        if (entity instanceof Animal animal) {
            baby = animal.getBreedOffspring(serverLevel, (Animal) nearestAdult);
        } else if (entity instanceof Villager villager) {
            baby = villager.getBreedOffspring(serverLevel, (Villager) nearestAdult);
        }

        if (baby == null) return true;

        // 生成幼崽（在双方中点）
        baby.setBaby(true);
        double midX = (entity.getX() + nearestAdult.getX()) / 2.0;
        double midZ = (entity.getZ() + nearestAdult.getZ()) / 2.0;
        baby.moveTo(midX, entity.getY(), midZ, 0.0F, 0.0F);
        serverLevel.addFreshEntity(baby);

        // 爱心粒子效果
        serverLevel.sendParticles(ParticleTypes.HEART,
                entity.getX(), entity.getY() + 1.5, entity.getZ(),
                20, 0.8, 0.8, 0.8, 0.2);

        // --- 设置冷却 + 繁殖后强制贴在一起 ---
        if (bothHaveLust) {
            // 双方发情：6秒冷却，双方都进入冷却
            tag.putInt(COOLDOWN_TAG, COOLDOWN_MUTUAL);
            nearestAdult.getPersistentData().putInt(COOLDOWN_TAG, COOLDOWN_MUTUAL);
            // 繁殖后双方速度清零，防止惯性漂开
            entity.setDeltaMovement(Vec3.ZERO);
            nearestAdult.setDeltaMovement(Vec3.ZERO);
        } else {
            // 单方发情：12秒冷却，仅自己进入冷却
            tag.putInt(COOLDOWN_TAG, COOLDOWN_SINGLE);
            entity.setDeltaMovement(Vec3.ZERO);
        }

        return true;
    }

    /**
     * 冷却期间强制保持靠近伴侣。
     * 每 tick：stop() 清除原版 AI 路径 → moveTo() 重新导航 → 强制速度向量。
     * 如果距离超过传送阈值，直接传送到伴侣身边。
     *
     * @param entity 当前实体
     */
    private void stickToPartner(LivingEntity entity) {
        LivingEntity partner = findNearestAdultPartner(entity);
        if (partner == null) return;

        double dist = entity.distanceTo(partner);

        // 距离太远：直接传送
        if (dist > TELEPORT_THRESHOLD) {
            entity.moveTo(partner.getX(), partner.getY(), partner.getZ(),
                    entity.getYRot(), entity.getXRot());
            entity.setDeltaMovement(Vec3.ZERO);
            return;
        }

        // 距离在合理范围内：强制导航 + 强制速度
        if (dist > STICK_DISTANCE) {
            forceNavigateTo(entity, partner);
        } else {
            // 已经贴在一起了，清除所有移动防止抖动
            if (entity instanceof Mob mob) {
                mob.getNavigation().stop();
            }
            entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
        }
    }

    /**
     * 搜索最近的成年同类个体（排除自己和幼崽）
     *
     * @param entity 当前实体
     * @return 最近的成年同类，找不到返回 null
     */
    private LivingEntity findNearestAdultPartner(LivingEntity entity) {
        AABB searchBox = entity.getBoundingBox().inflate(SEARCH_RADIUS);
        List<? extends LivingEntity> candidates = entity.level()
                .getEntitiesOfClass(entity.getClass(), searchBox);

        LivingEntity nearest = null;
        double minDist = Double.MAX_VALUE;

        for (LivingEntity c : candidates) {
            if (c == entity) continue;
            if (c instanceof Animal a && a.isBaby()) continue;
            if (c instanceof Villager v && v.isBaby()) continue;

            double d = entity.distanceTo(c);
            if (d < minDist) {
                minDist = d;
                nearest = c;
            }
        }

        return nearest;
    }

    /**
     * 强制导航实体向目标移动。
     * 先 stop() 清除原版 AI 的路径，再 moveTo() 设置新路径，
     * 同时强制设置速度向量指向目标，确保原版 AI 无法覆盖。
     *
     * @param entity 要导航的实体
     * @param target 目标实体
     */
    private void forceNavigateTo(LivingEntity entity, LivingEntity target) {
        // 1. 先 stop() 清除原版 AI 正在执行的路径
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.getNavigation().moveTo(target, 1.5);
        }

        // 2. 强制设置速度向量指向目标（覆盖原版物理和 AI 的移动）
        Vec3 dir = target.position().subtract(entity.position()).normalize();
        entity.setDeltaMovement(
                dir.x * STICK_SPEED,
                entity.getDeltaMovement().y,  // 保留 Y 轴（重力/跳跃）
                dir.z * STICK_SPEED
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
