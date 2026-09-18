package MoreBreedingOptimize.NB;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 鸡蛋恢复处理器
 *
 * 玩家手持鸡蛋对已绝育的动物右键时：
 * 1. 消耗 1 个鸡蛋
 * 2. 移除动物的 NBT 绝育标记 "morebo:neutered"
 * 3. 动物恢复繁殖能力
 *
 * 不修改任何已有类，完全独立的新功能模块。
 */
@EventBusSubscriber(modid = "morebo")
public class EggRestoreHandler {

    private static final Logger log = LoggerFactory.getLogger(EggRestoreHandler.class);

    /**
     * 监听玩家右键实体事件。
     * 当玩家手持鸡蛋右键已绝育的动物时，恢复其繁殖能力。
     *
     * @param event 玩家实体交互事件
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();

        // 只关心动物
        if (!(target instanceof Animal animal)) return;

        // 仅服务端处理核心逻辑
        if (event.getLevel().isClientSide) return;

        // 检查玩家手持物品是否为鸡蛋
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.is(Items.EGG)) return;

        // 检查动物是否已被绝育
        CompoundTag tag = animal.getPersistentData();
        if (!tag.getBoolean(ScissorsNeuterHandler.NEUTERED_TAG)) {
            // 未绝育，提示玩家
            player.sendSystemMessage(Component.literal("该动物未被绝育，无需恢复"));
            event.setCanceled(true);
            return;
        }

        // 强制转换为 ServerLevel
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        // ===== 执行恢复逻辑 =====

        // 1. 移除 NBT 标记
        tag.remove(ScissorsNeuterHandler.NEUTERED_TAG);

        // 2. 消耗鸡蛋（创造模式不消耗）
        if (!player.isCreative()) {
            heldItem.shrink(1);
        }

        // 3. 播放音效（使用村民交易音效，表示恢复）
        serverLevel.playSound(
                null,
                animal.getX(),
                animal.getY(),
                animal.getZ(),
                SoundEvents.VILLAGER_YES,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F
        );

        // 4. 提示玩家
        player.sendSystemMessage(Component.literal("已恢复 " + animal.getName().getString() + " 的繁殖能力"));

        log.info("[MoreBreedingOptimize] Egg restore! Player {} used egg on {} at {}",
                player.getName().getString(),
                animal.getType(),
                animal.blockPosition());

        // 取消默认交互行为
        event.setCanceled(true);
    }
}
