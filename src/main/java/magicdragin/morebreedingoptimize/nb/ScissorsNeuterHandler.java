package magicdragin.morebreedingoptimize.nb;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
 * 剪刀/老虎钳绝育处理器
 *
 * 玩家手持剪刀潜行右键，或手持老虎钳（lhq）直接右键，对任意动物执行绝育：
 * 1. 消耗工具耐久
 * 2. 掉落物：剪刀掉 2 个鸡蛋；老虎钳掉 1 个末地烛
 * 3. 给动物打上 NBT 标记 "morebo:neutered" = true（两者标记完全相同）
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

        // 适用于所有生物（动物/村民/怪物等），玩家除外
        if (target instanceof Player) return;
        if (!(target instanceof LivingEntity living)) return;

        // 仅服务端处理核心逻辑
        if (event.getLevel().isClientSide) return;

        // 手持物品：剪刀 或 老虎钳（lhq）
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        boolean isShears = heldItem.is(Items.SHEARS);
        boolean isPliers = heldItem.is(NeuterBlockItem.LHQ.get());
        if (!isShears && !isPliers) return;

        // 剪刀必须潜行（防止与剪刀剪羊毛冲突）；老虎钳直接右键即可
        if (isShears && !player.isCrouching()) return;

        // 检查动物是否已被绝育
        CompoundTag tag = living.getPersistentData();
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

        // 2. 掉落物：剪刀掉 2 个鸡蛋，老虎钳掉 1 个末地烛
        ItemStack dropStack = isPliers ? new ItemStack(Items.END_ROD) : new ItemStack(Items.EGG);
        int dropCount = isPliers ? 1 : EGG_DROP_COUNT;
        for (int i = 0; i < dropCount; i++) {
            ItemEntity drop = new ItemEntity(
                    serverLevel,
                    living.getX(),
                    living.getY() + 0.5,
                    living.getZ(),
                    dropStack.copy()
            );
            drop.setDeltaMovement(
                    (living.getRandom().nextDouble() - 0.5) * 0.2,
                    0.2,
                    (living.getRandom().nextDouble() - 0.5) * 0.2
            );
            serverLevel.addFreshEntity(drop);
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
                living.getX(),
                living.getY(),
                living.getZ(),
                SoundEvents.SHEEP_SHEAR,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F
        );

        // 5. 提示玩家
        player.sendSystemMessage(Component.literal("已对 " + living.getName().getString() + " 进行绝育"));

        // 6. 扣动物 0.5 血（绝育的代价）
        living.setHealth(living.getHealth() - 0.5F);

        log.info("[mrmagicdragin.morebreedingoptimize] Scissors neuter! Player {} used shears on {} at {}",
                player.getName().getString(),
                living.getType(),
                living.blockPosition());

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
    public static boolean isNeutered(LivingEntity living) {
        return living.getPersistentData().getBoolean(NEUTERED_TAG);
    }
}
