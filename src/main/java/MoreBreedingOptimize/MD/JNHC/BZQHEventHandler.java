package MoreBreedingOptimize.MD.JNHC;

import MoreBreedingOptimize.MainClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = MainClass.MODID)
public class BZQHEventHandler {

    /**
     * 清理玩家周围 5x5x5 范围内的所有光源方块。
     * 抽成公共静态方法，供 Remove / Expired 两个事件监听复用，
     * 也供 BZQHEffect 内部清理逻辑复用，避免代码重复。
     */
    public static void clearNearbyLights(Player player) {
        Level level = player.level();
        BlockPos center = player.blockPosition();

        // 清理周围 5x5x5 范围内的所有光源
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(Blocks.LIGHT)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    /**
     * 判断事件的效果实例是否是宾洲强化效果。
     * 注意：MobEffectInstance.getEffect() 在 1.21.1 返回 Holder<MobEffect>，
     * 必须先 .value() 拿到 MobEffect 再 instanceof。
     */
    private static boolean isBZQHEffect(MobEffectEvent event) {
        return event.getEffectInstance().getEffect().value() instanceof BZQHEffect;
    }

    /**
     * 显式移除路径：喝牛奶、/effect clear 等主动清除效果时触发。
     */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        // 只处理玩家的 BZQHEffect 效果移除
        if (event.getEntity() instanceof Player player) {
            if (isBZQHEffect(event)) {
                clearNearbyLights(player);
            }
        }
    }

    /**
     * 自然到期路径：药水时间走完自动结束。
     * 1.21.1 中自然到期只发布 MobEffectEvent.Expired（不发 Remove），
     * 之前只监听 Remove 导致到期瞬间最后一个光源方块无法清理。
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        // 只处理玩家的 BZQHEffect 效果到期
        if (event.getEntity() instanceof Player player) {
            if (isBZQHEffect(event)) {
                clearNearbyLights(player);
            }
        }
    }
}
