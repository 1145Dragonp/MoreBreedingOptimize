package MoreBreedingOptimize.MD.SM;

import MoreBreedingOptimize.MainClass;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@EventBusSubscriber(modid = MainClass.MODID)
public class LustBowHandler {

    @SubscribeEvent
    public static void onArrowHit(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof Arrow arrow) {
            // 直接读取数据附件，判断是否是爱情箭
            if (arrow.getData(ModDataAttachments.IS_LOVE_ARROW.get())) {
                if (event.getRayTraceResult() instanceof EntityHitResult hitResult) {
                    if (hitResult.getEntity() instanceof LivingEntity target) {
                        target.addEffect(new MobEffectInstance(
                                SMItem.LUST,
                                6000,
                                0
                        ));
                    }
                }
            }
        }
    }
}