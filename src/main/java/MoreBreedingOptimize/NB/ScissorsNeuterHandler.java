package MoreBreedingOptimize.NB;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 剪刀绝育处理器
 *
 * 玩家手持剪刀对任意动物右键时：
 * 1. 消耗剪刀耐久
 * 2. 掉落 2 个鸡蛋
 * 3. 给动物打上 NBT 标记 "morebo:neutered" = true
 * 4. 被标记的动物无法再繁殖（由 NeuterBlockHandler 拦截）
 * 5. 使用鸡蛋对动物右键可恢复繁殖能力（由 EggRestoreHandler 处理）
 *
 * 不修改任何已有类，完全独立的新功能模块。
 */
@EventBusSubscriber(modid = "morebo")
public class ScissorsNeuterHandler {

    private static final Logger log = LoggerFactory.getLogger(ScissorsNeuterHandler.class);

    /**
     * NBT 标记键名
     */
    public static final String NEUTERED_TAG = "morebo:neutered";

    /**
     * 绝育后掉落的鸡蛋数量
     */
    private static final int EGG_DROP_COUNT = 2;

    /**
     * 监听玩家右键实体事件。
     * 玩家需潜行（Shift）且手持剪刀右键动物时，执行绝育操作。
     * 潜行是为了防止与剪刀剪羊毛的默认交互冲突。
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

        // 必须潜行（防止与剪刀剪羊毛冲突）
        if (!player.isCrouching()) return;

        // 检查玩家手持物品是否为剪刀
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.is(Items.SHEARS)) return;

        // 检查动物是否已被绝育
        CompoundTag tag = animal.getPersistentData();
        if (tag.getBoolean(NEUTERED_TAG)) {
            // 已绝育，提示玩家
            player.sendSystemMessage(Component.literal("该动物已被绝育"));
            event.setCanceled(true);
            return;
        }

        // 强制转换为 ServerLevel
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        // ===== 执行绝育逻辑 =====

        // 1. 设置 NBT 标记
        tag.putBoolean(NEUTERED_TAG, true);

        // 2. 掉落 2 个鸡蛋
        for (int i = 0; i < EGG_DROP_COUNT; i++) {
            ItemEntity egg = new ItemEntity(
                    serverLevel,
                    animal.getX(),
                    animal.getY() + 0.5,
                    animal.getZ(),
                    new ItemStack(Items.EGG)
            );
            egg.setDeltaMovement(
                    (animal.getRandom().nextDouble() - 0.5) * 0.2,
                    0.2,
                    (animal.getRandom().nextDouble() - 0.5) * 0.2
            );
            serverLevel.addFreshEntity(egg);
        }

        // 3. 消耗剪刀耐久（创造模式不消耗）
        if (!player.isCreative()) {
            heldItem.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                    ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        }

        // 4. 播放剪刀音效
        serverLevel.playSound(
                null,
                animal.getX(),
                animal.getY(),
                animal.getZ(),
                SoundEvents.SHEEP_SHEAR,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F
        );

        // 5. 提示玩家
        player.sendSystemMessage(Component.literal("已对 " + animal.getName().getString() + " 进行绝育"));

        // 6. 扣动物 0.5 血（绝育的代价）
        animal.setHealth(animal.getHealth() - 0.5F);

        log.info("[MoreBreedingOptimize] Scissors neuter! Player {} used shears on {} at {}",
                player.getName().getString(),
                animal.getType(),
                animal.blockPosition());

        // 取消默认交互行为
        event.setCanceled(true);
    }

    /**
     * 检查动物是否已被绝育。
     * 供其他 Handler（如 NeuterBlockHandler）调用。
     *
     * @param animal 要检查的动物
     * @return true 表示已绝育，false 表示未绝育
     */
    public static boolean isNeutered(Animal animal) {
        return animal.getPersistentData().getBoolean(NEUTERED_TAG);
    }
}
