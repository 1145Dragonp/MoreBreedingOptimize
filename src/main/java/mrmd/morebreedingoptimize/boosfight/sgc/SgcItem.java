package mrmd.morebreedingoptimize.boosfight.sgc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SgcItem extends Item {
    public SgcItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        SgcEntity entity = BoosFightItem.SGC_ENTITY.get().create(level);
        if (entity == null) return InteractionResult.FAIL;

        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        float yaw = Math.round(context.getPlayer().getYRot() / 90.0F) * 90.0F;
        entity.setYRot(yaw);
        level.addFreshEntity(entity);

        if (!context.getPlayer().isCreative()) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}