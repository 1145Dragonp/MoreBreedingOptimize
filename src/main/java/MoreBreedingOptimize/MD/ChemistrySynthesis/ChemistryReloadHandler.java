package MoreBreedingOptimize.MD.ChemistrySynthesis;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * 将化学合成配方管理器注册到服务器资源重载监听器
 */
@EventBusSubscriber(modid = MoreBreedingOptimize.MainClass.MODID)
public class ChemistryReloadHandler {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(ChemistryRecipeManager.INSTANCE);
    }
}
