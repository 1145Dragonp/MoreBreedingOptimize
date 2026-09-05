package BreedMovement.BAct;

import BreedMovement.Config;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BreedMovementAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(BreedMovementAction.class);

    // 推力力度 (0.5 ~ 1.0 之间效果较好，可根据需求调整)
    public  static double STRENGTH = Config.STRENGTH.get();

    //public static double DROP = 0.1;

    //public static double DIE = 0.005;

    /**
     * 【兼容接口】
     * 专门为 LoveModeEventHandler 提供的方法。
     * 繁殖瞬间调用，给 animal 一个远离 partner 的瞬时速度。
     */
    public static void applyThrust(LivingEntity animal, LivingEntity partner) {
        if (animal == null || partner == null || animal.level().isClientSide) {
            return;
        }

        // 1. 计算从 partner 指向 animal 的方向向量
        Vec3 partnerPos = partner.position();
        Vec3 animalPos = animal.position();
        Vec3 direction = animalPos.subtract(partnerPos).normalize();

        // 2. 如果距离太近导致方向为0，则随机给一个方向防止报错
        if (direction.lengthSqr() == 0) {
            direction = new Vec3(1, 0, 0);
        }

        // 3. 施加瞬时速度 (保留原有的 Y 轴运动，如跳跃或跌落)
        Vec3 currentMotion = animal.getDeltaMovement();
        Vec3 newMotion = new Vec3(
                direction.x * STRENGTH,
                currentMotion.y, // 保持垂直方向的原有力
                direction.z * STRENGTH
        );

        animal.setDeltaMovement(newMotion);

        // 4. 标记实体已修改过速度，防止被碰撞箱或摩擦力瞬间抵消
        animal.hurtMarked = true;
        /*
        LOGGER.info("[BreedMovement] 实体 {} 受到繁殖推力，方向: {}",
                animal.getType().builtInRegistryHolder().key().location(),
                direction);

         */
        //掉落阴水率
        if (animal.getRandom().nextDouble() < Config.DROP_CHANCE.get()) {
            ItemEntity waterBucket = new ItemEntity(
                    animal.level(),
                    animal.getX(),
                    animal.getY() + 0.5,
                    animal.getZ(),
                    new ItemStack(Items.WATER_BUCKET)
            );

            waterBucket.setDeltaMovement(0, 0.1, 0);
            animal.level().addFreshEntity(waterBucket);
        }
        //掉落精液率
        if (animal.getRandom().nextDouble() < Config.DROP_CHANCE.get()) {
            ItemEntity waterBucket = new ItemEntity(
                    animal.level(),
                    animal.getX(),
                    animal.getY() + 0.5,
                    animal.getZ(),
                    new ItemStack(Items.MILK_BUCKET)
            );

            waterBucket.setDeltaMovement(0, 0.1, 0);
            animal.level().addFreshEntity(waterBucket);
        }
        //操死率
        if (animal.getRandom().nextDouble() < Config.DIE_CHANCE.get()) {

            // 掉落末地烛
            ItemEntity endRod = new ItemEntity(
                    animal.level(),
                    animal.getX(),
                    animal.getY() + 0.5,
                    animal.getZ(),
                    new ItemStack(Items.END_ROD)
            );



            endRod.setDeltaMovement(0, 0.1, 0);
            animal.level().addFreshEntity(endRod);

            animal.kill();

        }

    }

}