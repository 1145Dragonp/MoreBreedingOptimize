package mrmd.morebreedingoptimize.boosfight.sgc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

@EventBusSubscriber
public class SgcBreakHandler {
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof SgcEntity sgc)) return;
        if (sgc.level().isClientSide) return;
        sgc.spawnAtLocation(new ItemStack(BoosFightItem.sgc.get()));
        sgc.discard();
        event.setCanceled(true);
    }
}