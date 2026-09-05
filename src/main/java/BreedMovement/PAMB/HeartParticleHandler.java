package BreedMovement.PAMB;

import BreedMovement.MainClass;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 爱心粒子处理器
 *
 * 当检测到末地烛物品实体生成时（对应 BreedMovementAction 中的"操死"掉落物），
 * 在附近 3 格内的动物位置发射爱心粒子，增强视觉效果。
 *
 * 设计原则：
 * - 不修改 BreedMovementAction 和 LoveModeEventHandler
 * - 通过监听 EntityJoinLevelEvent 检测末地烛生成事件
 * - 自动在附近动物位置发射粒子
 * - 使用 WeakHashMap 防止同一物品重复触发
 */
@EventBusSubscriber(modid = MainClass.MODID)
public class HeartParticleHandler {

    private static final Logger log = LoggerFactory.getLogger(HeartParticleHandler.class);

    /**
     * 已处理过的物品实体集合（防止重复触发）
     * WeakHashMap 保证物品消失后自动 GC
     */
    private static final Set<ItemEntity> processedItems = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * 检测半径：末地烛周围多少格内的动物会被发射爱心粒子
     */
    private static final double DETECT_RADIUS = 3.0;

    /**
     * 每次发射的粒子数量
     */
    private static final int PARTICLE_COUNT = 8;

    /**
     * 粒子 XYZ 扩散范围（让粒子散开而不是聚成一点）
     */
    private static final double PARTICLE_SPREAD = 0.3;

    /**
     * 监听实体加入世界事件（仅服务端）。
     * 当检测到末地烛物品实体生成时，在附近动物位置发射爱心粒子。
     *
     * @param event 实体加入事件
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // 仅服务端处理
        if (event.getLevel().isClientSide) return;

        // 强制转换为 ServerLevel（服务端一定可以转换）
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        // 只关心物品实体
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

        // 只关心末地烛（对应 BreedMovementAction 中的 DIE_CHANCE 掉落物）
        if (!itemEntity.getItem().is(Items.END_ROD)) return;

        // 防止重复处理（同一物品可能触发多次事件）
        if (processedItems.contains(itemEntity)) return;
        processedItems.add(itemEntity);

        // 搜索附近动物
        AABB searchBox = itemEntity.getBoundingBox().inflate(DETECT_RADIUS);
        List<Animal> nearbyAnimals = serverLevel.getEntitiesOfClass(
                Animal.class,
                searchBox,
                e -> true
        );

        if (nearbyAnimals.isEmpty()) return;

        // 在每个动物位置发射爱心粒子
        for (Animal animal : nearbyAnimals) {
            // @AI(BAPI.ServerLevel,sendParticles)
            // 使用 ServerLevel 内置方法，自动处理粒子广播，无需手动发包
            serverLevel.sendParticles(
                    ParticleTypes.HEART,
                    animal.getX(),
                    animal.getY() + animal.getBbHeight() * 0.5,
                    animal.getZ(),
                    PARTICLE_COUNT,
                    PARTICLE_SPREAD,
                    PARTICLE_SPREAD,
                    PARTICLE_SPREAD,
                    0.0
            );
        }

        log.info("[BreedMovement] Heart particles emitted near {} animals at {}",
                nearbyAnimals.size(), itemEntity.blockPosition());
    }
}
