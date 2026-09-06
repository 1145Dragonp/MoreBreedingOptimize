package BreedMovement.MD.SM;

import BreedMovement.BAct.BreedMovementAction;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LustEffect extends MobEffect {

    private static final String COOLDOWN_TAG = "lust_cooldown";
    private static final String OCCUPIED_TAG = "lust_occupied";
    private static final int COOLDOWN_TICKS = 120;      // 6秒冷却
    private static final double SEARCH_RADIUS = 16.0;   // 搜索半径
    private static final double BREED_DISTANCE = 2.5;   // 触发繁殖的最大距离

    public LustEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFC0CB);
    }

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

        CompoundTag tag = entity.getPersistentData();

        // --- 冷却 & 占用递减 ---
        int cooldown = tag.getInt(COOLDOWN_TAG);
        if (cooldown > 0) {
            tag.putInt(COOLDOWN_TAG, cooldown - 1);
            int occ = tag.getInt(OCCUPIED_TAG);
            if (occ > 0) tag.putInt(OCCUPIED_TAG, occ - 1);
            return true;
        }

        // --- 严格成年检测 ---
        if (entity instanceof Animal a && a.isBaby()) return true;
        if (entity instanceof Villager v && v.isBaby()) return true;

        // --- 自己被占用时不参与 ---
        if (tag.getInt(OCCUPIED_TAG) > 0) return true;

        // --- 搜索同类成年个体 ---
        AABB searchBox = entity.getBoundingBox().inflate(SEARCH_RADIUS);
        List<? extends LivingEntity> candidates = entity.level()
                .getEntitiesOfClass(entity.getClass(), searchBox);

        LivingEntity nearestAdult = null;
        double minDist = Double.MAX_VALUE;

        for (LivingEntity c : candidates) {
            if (c == entity) continue;
            if (c instanceof Animal a && a.isBaby()) continue;
            if (c instanceof Villager v && v.isBaby()) continue;
            if (c.getPersistentData().getInt(OCCUPIED_TAG) > 0) continue;

            double d = entity.distanceTo(c);
            if (d < minDist) {
                minDist = d;
                nearestAdult = c;
            }
        }

        if (nearestAdult == null) return true;

        // --- 未靠近：仅导航，并【强制停止原版AI】防止抽搐 ---
        if (minDist > BREED_DISTANCE) {
            if (entity instanceof Animal a) {
                a.getNavigation().stop(); // 先清除原版AI的残留路径
                a.getNavigation().moveTo(nearestAdult, 1.2);
            } else if (entity instanceof Villager v) {
                v.getNavigation().stop();
                v.getNavigation().moveTo(nearestAdult, 1.0);
            }
            return true;
        }

        // --- 已靠近：判断对方是否有效参与双方发情 ---
        Holder<MobEffect> selfHolder = getSelfHolder();
        boolean targetHasLust = nearestAdult.hasEffect(selfHolder);
        CompoundTag targetTag = nearestAdult.getPersistentData();

        if (targetHasLust) {
            if (targetTag.getInt(COOLDOWN_TAG) > 0 || targetTag.getInt(OCCUPIED_TAG) > 0) {
                targetHasLust = false;
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

        // 生成幼崽
        baby.setBaby(true);
        baby.moveTo(entity.getX(), entity.getY(), entity.getZ(), 0.0F, 0.0F);
        serverLevel.addFreshEntity(baby);

        // ⭐ 粒子效果大增强：生成一波密集的爱心粒子！
        serverLevel.sendParticles(ParticleTypes.HEART,
                entity.getX(), entity.getY() + 1.5, entity.getZ(),
                20, 0.8, 0.8, 0.8, 0.2); // 数量20，扩散范围加大

        // --- 动画调用 ---
        if (targetHasLust) {
            BreedMovementAction.applyThrust(entity, nearestAdult);
            BreedMovementAction.applyThrust(nearestAdult, entity);
            tag.putInt(COOLDOWN_TAG, COOLDOWN_TICKS);
            targetTag.putInt(COOLDOWN_TAG, COOLDOWN_TICKS);
        } else {
            BreedMovementAction.applyThrust(entity, nearestAdult);
            tag.putInt(COOLDOWN_TAG, COOLDOWN_TICKS);
            targetTag.putInt(OCCUPIED_TAG, COOLDOWN_TICKS);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}