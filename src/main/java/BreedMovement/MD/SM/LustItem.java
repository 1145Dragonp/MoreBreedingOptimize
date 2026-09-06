package BreedMovement.MD.SM;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * 自定义物品逻辑类
 * 专门处理 smt 物品的右键交互
 */
public class LustItem extends Item {

    public LustItem(Properties properties) {
        super(properties);
    }

    /**
     * 当玩家右键点击生物时触发
     */
    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        Level level = player.level();

        // 只在服务器端执行，防止客户端也触发导致逻辑混乱
        if (!level.isClientSide) {
            // 1. 给目标生物添加“发情”效果，持续6000刻（5分钟）
            MobEffectInstance effectInstance = new MobEffectInstance(SMItem.LUST, 6000, 0);
            target.addEffect(effectInstance);

            // 2. 如果不是创造模式，就消耗掉手里的物品
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        // ⭐ 关键一步：返回 SUCCESS
        // 这行代码会告诉游戏：“这个右键交互我已经处理完了，你别管了！”
        // 这样一来，右键村民时就不会弹出交易界面了。
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}