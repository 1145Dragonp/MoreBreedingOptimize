package mrmd.morebreedingoptimize.boosfight.dor;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DorEntity extends Mob {
    private boolean open = false;
    private boolean fleeing = false;
    private int fleeTicks = 0;
    private double startY;
    private double fleeY;

    public DorEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D) //测试为60，正式200
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    public boolean isOpen() { return open; }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide) return InteractionResult.SUCCESS;
        if (fleeing) return InteractionResult.PASS;
        this.open = !this.open;
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isPushable() { return !open; }

    @Override
    public boolean isPickable() { return !open || fleeing; }

    @Override
    public boolean canBeCollidedWith() { return !open || fleeing; }

    @Override
    public void tick() {
        super.tick();

        AABB box = this.getBoundingBox();
        double cx = (box.minX + box.maxX) / 2;
        double cz = (box.minZ + box.maxZ) / 2;
        double yMin = box.minY;
        double yMax = box.maxY;

        float yaw = this.getYRot() % 360;
        if (yaw < 0) yaw += 360;
        double halfLong = 1.5;
        double halfThin = 0.3;

        if (yaw < 45 || yaw >= 315 || (yaw >= 135 && yaw < 225)) {
            this.setBoundingBox(new AABB(cx - halfLong, yMin, cz - halfThin, cx + halfLong, yMax, cz + halfThin));
        } else {
            this.setBoundingBox(new AABB(cx - halfThin, yMin, cz - halfLong, cx + halfThin, yMax, cz + halfLong));
        }

        if (!fleeing && this.getHealth() <= 10.0F && this.getHealth() > 0) {
            fleeing = true;
            fleeTicks = 0;
            startY = this.getY();
            fleeY = 0;
        }

        if (fleeing) {
            fleeTicks++;
            double radians = Math.toRadians(this.getYRot());

            if (fleeTicks <= 20) {
                this.setYRot(this.getYRot() + 9.0F);
            } else if (fleeTicks <= 60) {
                // 沿转身后面向的反方向跑（远离玩家）
                double moveX = Math.sin(radians) * 0.4;
                double moveZ = -Math.cos(radians) * 0.4;
                // 跳跃：fleeY 是相对 startY 的高度，重力自然下落
                fleeY -= 0.1;
                if (fleeY < 0) fleeY = 0;
                if (fleeY == 0 && this.random.nextInt(3) == 0) {
                    fleeY = 0.6;
                }
                this.setPos(this.getX() + moveX, startY + fleeY, this.getZ() + moveZ);
            } else if (fleeTicks <= 120) {
                if (fleeTicks == 105) {
                    this.level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
                }
                this.level().addParticle(ParticleTypes.PORTAL,
                        this.getX() + (this.random.nextDouble()-0.5), this.getY() + this.random.nextDouble()*2, this.getZ() + (this.random.nextDouble()-0.5),
                        0, 0.1, 0);
            } else {
                this.level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
                this.discard();
                return;
            }
        }

        if (this.level().isClientSide || open || fleeing) return;

        AABB doorBox = this.getBoundingBox();
        for (Player player : this.level().getEntitiesOfClass(Player.class, doorBox.inflate(0.5))) {
            if (player.isSpectator() || player.isCreative()) continue;
            Vec3 doorCenter = doorBox.getCenter();
            double dx = player.getX() - doorCenter.x;
            double dz = player.getZ() - doorCenter.z;
            double distX = Math.abs(dx);
            double distZ = Math.abs(dz);
            double halfX = doorBox.getXsize() / 2.0;
            double halfZ = doorBox.getZsize() / 2.0;
            if (distX > distZ) {
                double targetX = doorCenter.x + Math.signum(dx) * (halfX + player.getBbWidth() / 2.0 + 0.05);
                player.setPos(targetX, player.getY(), player.getZ());
            } else {
                double targetZ = doorCenter.z + Math.signum(dz) * (halfZ + player.getBbWidth() / 2.0 + 0.05);
                player.setPos(player.getX(), player.getY(), targetZ);
            }
            player.setDeltaMovement(Vec3.ZERO);
        }

        for (Projectile proj : this.level().getEntitiesOfClass(Projectile.class, doorBox)) {
            proj.discard();
        }
    }
}