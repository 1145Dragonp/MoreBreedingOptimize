package MoreBreedingOptimize;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import MoreBreedingOptimize.MD.SM.SMItem;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = MainClass.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = MainClass.MODID, value = Dist.CLIENT)
public class ModClient {
    public ModClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        MainClass.LOGGER.info("HELLO FROM CLIENT SETUP");
        MainClass.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

        // @AI(BAPI.ItemProperties, register) 物品属性注册线程不安全，必须放到并行线程池里串行执行
        event.enqueueWork(ModClient::registerItemProperties);
    }

    /**
     * 给自定义弓注册 pulling / pull 物品属性。
     * 原版只给 Items.BOW 注册了这两个属性，自定义弓不注册的话，
     * lovebow.json 里的 overrides 谓词永远拿不到值，拉弓动画就不会切换模型。
     */
    private static void registerItemProperties() {
        Item loveBow = SMItem.LB.get();

        // "pulling"：正在使用（拉弓）时为 1
        ItemProperties.register(loveBow, ResourceLocation.withDefaultNamespace("pulling"),
                (stack, level, entity, seed) ->
                        entity != null
                                && entity.isUsingItem()
                                && entity.getUseItem() == stack ? 1.0F : 0.0F);

        // "pull"：拉弓进度 0.0 ~ 1.0，与原版弓的进度计算完全一致
        ItemProperties.register(loveBow, ResourceLocation.withDefaultNamespace("pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null || !entity.isUsingItem() || entity.getUseItem() != stack) {
                        return 0.0F;
                    }
                    int useDuration = stack.getUseDuration(entity);
                    int usedTicks = useDuration - entity.getUseItemRemainingTicks();
                    float f = usedTicks / 20.0F;
                    // 原版公式：让前几秒拉慢、满蓄力前加速
                    f = (f * f + f * 2.0F) / 3.0F;
                    return Math.min(f, 1.0F);
                });
    }
}
