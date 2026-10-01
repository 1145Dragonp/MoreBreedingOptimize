package mrmd.morebreedingoptimize.boosfight.dor;

import mrmd.morebreedingoptimize.boosfight.BoosFightItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class DorDoorItem extends Item {
    public DorDoorItem(Properties props) { super(props); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        DorEntity entity = BoosFightItem.DOR_ENTITY.get().create(level);
        if (entity == null) return InteractionResult.FAIL;
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        float yaw = Math.round(context.getPlayer().getYRot() / 90.0F) * 90.0F;
        entity.setYRot(yaw);
        level.addFreshEntity(entity);
        if (!context.getPlayer().isCreative()) context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}