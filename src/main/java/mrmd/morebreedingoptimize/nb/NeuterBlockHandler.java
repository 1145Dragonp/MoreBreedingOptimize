package mrmd.morebreedingoptimize.nb;

import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 绝育繁殖拦截器
 *
 * 监听幼崽出生事件，如果检测到父方或母方已被绝育（NBT 标记），
 * 则取消该次繁殖，幼崽不会生成。
 *
 * 这是实现"绝育后无法繁殖"的关键逻辑。
 */
@EventBusSubscriber(modid = "morebo")
public class NeuterBlockHandler {

    private static final Logger log = LoggerFactory.getLogger(NeuterBlockHandler.class);

    /**
     * 监听幼崽出生事件。
     * 如果父方或母方已被绝育，取消繁殖。
     *
     * @param event 幼崽出生事件
     */
    @SubscribeEvent
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        Mob parentA = event.getParentA();
        Mob parentB = event.getParentB();

        // 检查父方或母方是否已被绝育（适用于所有可繁殖生物，村民等非 Animal 也生效）
        if (ScissorsNeuterHandler.isNeutered(parentA) || ScissorsNeuterHandler.isNeutered(parentB)) {
            // 取消繁殖
            event.setCanceled(true);

            log.info("[mrmagicdragin.morebreedingoptimize] Neuter block! Breeding canceled between {} x {} at {} & {}",
                    parentA.getType(),
                    parentB.getType(),
                    parentA.blockPosition(),
                    parentB.blockPosition());
        }
    }
}
