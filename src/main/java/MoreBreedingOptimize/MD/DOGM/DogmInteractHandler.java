package MoreBreedingOptimize.MD.DOGM;

import MoreBreedingOptimize.MainClass;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

// 注册为游戏事件总线，监听全局事件
@EventBusSubscriber(modid = MainClass.MODID, bus = EventBusSubscriber.Bus.GAME)
public class DogmInteractHandler {
    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        // 1. 判定目标是否为原版未驯服的狼（野生狗）
        if (event.getTarget() instanceof Wolf wolf && !wolf.isTame()) {

            // 2. 判定玩家是否手持水桶，且为右键主手
            if (event.getHand() == InteractionHand.MAIN_HAND &&
                    event.getItemStack().is(Items.BUCKET)) {

                // --- 在这里填写你“就可以...”之后的逻辑 ---

                // 举例：消耗水桶，给玩家一个野生狗奶（DOGM），并播放挤奶音效
                ItemStack currentStack = event.getItemStack();
                currentStack.shrink(1); // 消耗一个桶

                ItemStack dogmStack = new ItemStack(DOGMITEM.DOGM.get());
                // 如果玩家手里还有空位，自动塞入背包，否则掉落
                if (!event.getEntity().getInventory().add(dogmStack)) {
                    event.getEntity().drop(dogmStack, false);
                }

                // 播放音效并取消原版交互（防止触发其他动作）
                wolf.level().playSound(null, wolf.getX(), wolf.getY(), wolf.getZ(),
                        SoundEvents.COW_MILK, SoundSource.NEUTRAL, 1.0F, 1.0F);
                event.setCanceled(true);
            }
        }
    }
}
