package mrmd.morebreedingoptimize.boosfight.dor;

import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

@EventBusSubscriber
public class DorTeleportHandler {
    @SubscribeEvent
    public static void onTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof ThrownEnderpearl)) return;
        var level = event.getEntity().level();
        for (DorEntity door : level.getEntitiesOfClass(DorEntity.class, new AABB(event.getTarget().x() - 1, event.getTarget().y() - 1, event.getTarget().z() - 1, event.getTarget().x() + 1, event.getTarget().y() + 3, event.getTarget().z() + 1))) {
            if (!door.isOpen()) {
                event.setCanceled(true);
            }
        }
    }
}