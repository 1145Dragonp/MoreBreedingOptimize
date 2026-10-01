package mrmd.morebreedingoptimize.boosfight.sgc;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class SgcEntity extends Mob {
    public SgcEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide) {
            this.spawnAtLocation(new net.minecraft.world.item.ItemStack(BoosFightItem.sgc.get()));
        }
        super.die(cause);
    }
}