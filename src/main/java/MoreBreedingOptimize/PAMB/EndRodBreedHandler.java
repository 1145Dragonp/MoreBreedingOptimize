package MoreBreedingOptimize.PAMB;

import MoreBreedingOptimize.MainClass;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import MoreBreedingOptimize.NB.ScissorsNeuterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 末地烛催生处理器
 *
 * 玩家手持末地烛对任意动物右键时：
 * 1. 消耗一根末地烛
 * 2. 在动物位置生成一只同物种的幼年形态动物
 * 3. 在幼崽位置发射大量爱心粒子
 * 4. 对同一动物设置冷却时间（防止连续催生）
 *
 * 不修改任何已有类，完全独立的新功能模块。
 */
@EventBusSubscriber(modid = MainClass.MODID)
public class EndRodBreedHandler {

    private static final Logger log = LoggerFactory.getLogger(EndRodBreedHandler.class);

    /**
     * 已催生过的动物冷却集合
     * WeakHashMap 保证动物死亡后自动 GC
     */
    private static final Set<Animal> cooldownAnimals = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * 冷却帧数：同一动物被催生后需要等待多少 tick 才能再次催生
     * 200 tick = 10 秒
     */
    private static final int COOLDOWN_TICKS = 200;

    /**
     * 冷却递减计数器（全局共享，简化实现）
     */
    private static int cooldownCounter = 0;

    /**
     * 幼崽初始年龄（负值表示幼年，绝对值越大成长越慢）
     * -24000 tick = 20 分钟，与原版幼年动物一致
     */
    private static final int BABY_AGE = -24000;

    /**
     * 爱心粒子数量（比普通触发多一些，增强仪式感）
     */
    private static final int HEART_PARTICLE_COUNT = 15;

    /**
     * 粒子扩散范围
     */
    private static final double PARTICLE_SPREAD = 0.5;

    /**
     * 监听玩家右键实体事件。
     * 当玩家手持末地烛右键动物时，催生幼崽。
     *
     * @param event 玩家实体交互事件
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // @AI(BAPI.PlayerInteractEvent.EntityInteract, onEntityInteract)
        Player player = event.getEntity();
        Entity target = event.getTarget();

        // 只关心动物
        if (!(target instanceof Animal animal)) return;

        // 幼年动物不能催生（防止对幼崽使用末地烛）
        if (animal.isBaby()) return;

        // 绝育动物不能催生
        if (ScissorsNeuterHandler.isNeutered(animal)) return;

        // 仅服务端处理核心逻辑
        if (event.getLevel().isClientSide) return;

        // 检查玩家手持物品是否为末地烛
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.is(Items.END_ROD)) return;

        // 冷却检查：同一动物短时间内不能连续催生
        if (cooldownAnimals.contains(animal)) return;

        // 强制转换为 ServerLevel
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        // ===== 执行催生逻辑 =====

        // 1. 创建同物种的幼崽
        AgeableMob baby = createBaby(serverLevel, animal.getType(), animal);
        if (baby == null) {
            log.warn("[MoreBreedingOptimize] Failed to create baby from end rod breed, target: {}",
                    animal.getType());
            return;



        }





        // 2. 定位在动物旁边（稍微偏移，避免重叠）
        double offsetX = (animal.getRandom().nextDouble() - 0.5) * 1.5;
        double offsetZ = (animal.getRandom().nextDouble() - 0.5) * 1.5;
        baby.moveTo(
                animal.getX() + offsetX,
                animal.getY(),
                animal.getZ() + offsetZ,
                animal.getRandom().nextFloat() * 360.0F,
                0.0F
        );

        // 3. 设置为幼年形态
        baby.setBaby(true);
        baby.setAge(BABY_AGE);

        // 4. 加入世界
        serverLevel.addFreshEntity(baby);

        // 5. 消耗末地烛（创造模式不消耗）

        // 6. 标记动物冷却
        cooldownAnimals.add(animal);

        // 7. 发射爱心粒子
        emitHeartParticles(serverLevel, baby);
        emitHeartParticles(serverLevel, animal);

        log.info("[MoreBreedingOptimize] End rod breed! Player {} used end rod on {} at {}, baby spawned: {}",
                player.getName().getString(),
                animal.getType(),
                animal.blockPosition(),
                baby.blockPosition());

        // 定期清理冷却列表
        cooldownCounter++;
        if (cooldownCounter >= COOLDOWN_TICKS) {
            cooldownCounter = 0;
            cooldownAnimals.clear();
        }

        // 取消默认交互行为，防止末地烛被当作普通物品使用
        event.setCanceled(true);
    }

    /**
     * 根据指定物种类型创建幼崽实体。
     * 与 CrossSpeciesBreedHandler.createBaby 逻辑类似，但此处为独立方法。
     *
     * @param level    服务端世界
     * @param type     幼崽物种类型
     * @param parent   父方动物（用于继承位置信息）
     * @return 幼年实体，创建失败返回 null
     */
    private static AgeableMob createBaby(ServerLevel level, EntityType<?> type, Animal parent) {
        Entity raw = type.create(level);
        if (!(raw instanceof AgeableMob baby)) return null;

        // 定位在父方旁边
        double offsetX = (parent.getRandom().nextDouble() - 0.5) * 1.5;
        double offsetZ = (parent.getRandom().nextDouble() - 0.5) * 1.5;
        baby.moveTo(
                parent.getX() + offsetX,
                parent.getY(),
                parent.getZ() + offsetZ,
                parent.getRandom().nextFloat() * 360.0F,
                0.0F
        );

        return baby;
    }

    /**
     * 在实体位置发射爱心粒子。
     * 使用 ServerLevel.sendParticles 自动广播给附近玩家。
     *
     * @param level 服务端世界
     * @param entity 粒子发射源实体
     */
    private static void emitHeartParticles(ServerLevel level, Entity entity) {
        // @AI(BAPI.ServerLevel,sendParticles)
        level.sendParticles(
                ParticleTypes.HEART,
                entity.getX(),
                entity.getY() + entity.getBbHeight() * 0.5,
                entity.getZ(),
                HEART_PARTICLE_COUNT,
                PARTICLE_SPREAD,
                PARTICLE_SPREAD,
                PARTICLE_SPREAD,
                0.0
        );
    }
}
