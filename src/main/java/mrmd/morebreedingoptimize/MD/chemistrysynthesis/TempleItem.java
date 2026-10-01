package mrmd.morebreedingoptimize.MD.chemistrysynthesis;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * 陵寝蓝图：右键地面，在点击处生成一座陵寝神殿。
 */
public class TempleItem extends Item {
    public TempleItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos().relative(context.getClickedFace());

        if (!level.isClientSide) {
            TempleBuilder.build(level, origin);
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.translatable("message.morebo.temple_built"), true);
                if (!context.getPlayer().isCreative()) {
                    context.getItemInHand().shrink(1);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}