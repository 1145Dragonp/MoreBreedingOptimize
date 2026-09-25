package MoreBreedingOptimize.MD.SM;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class LovelowBowItem extends BowItem {
    public LovelowBowItem() {
        super(new Item.Properties().durability(384).stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        super.releaseUsing(stack, level, entityLiving, timeLeft);

        // 在射箭逻辑执行后，获取刚刚射出的箭实体并打上标记
        // 这里的逻辑是：当弓释放时，最后生成的箭实体就是我们需要的
        level.getEntitiesOfClass(AbstractArrow.class, entityLiving.getBoundingBox().inflate(10.0D),
                        arrow -> arrow.getOwner() == entityLiving && !arrow.getData(ModDataAttachments.IS_LOVE_ARROW.get()))
                .stream().findFirst()
                .ifPresent(arrow -> arrow.setData(ModDataAttachments.IS_LOVE_ARROW.get(), true));
    }
}