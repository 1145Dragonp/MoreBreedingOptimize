package MoreBreedingOptimize;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import MoreBreedingOptimize.MD.SM.SMItem;
import MoreBreedingOptimize.MD.DOGM.DOGMITEM;
import MoreBreedingOptimize.MD.JNHC.JNHCITEM;
import MoreBreedingOptimize.MD.ChemistrySynthesis.ChemistryItem;
import MoreBreedingOptimize.MD.growthhormone.GrowthHormoneItems;
import MoreBreedingOptimize.MD.IVF.IVFITEM;
import MoreBreedingOptimize.MD.SM.ModDataAttachments;


//import static com.tacz.guns.GunMod.container;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MainClass.MODID)
public class MainClass {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "morebo";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);



    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public MainClass(IEventBus modEventBus, ModContainer modContainer) {
        // ✅ 注册方块与物品（必须在构造函数中完成）
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);

        SMItem.register(modEventBus);
        DOGMITEM.register(modEventBus);
        JNHCITEM.register(modEventBus);
        ChemistryItem.register(modEventBus);
        GrowthHormoneItems.register(modEventBus);
        IVFITEM.register(modEventBus);
        // 注册模组手册物品（morebo:sbook）
        MoreBreedingOptimize.MD.book.BookItem.register(modEventBus);
        // 注册化学书手册物品（morebo:hbook）
        MoreBreedingOptimize.MD.book.ChemistryBookItem.register(modEventBus);
        // 仅客户端：注册 Markdown Manual 的手册与文档提供者（markdown_manual 的注册表只在客户端存在，
        // 且须在其构造完成之后注册——neoforge.mods.toml 已声明 ordering="AFTER"）
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            MoreBreedingOptimize.MD.book.BookManualClient.register();
        }
        // 注册 IVF 数据组件（source_entity 等），漏注册会导致 DeferredHolder 未绑定、悬停 tooltip 时 NPE 崩溃
        MoreBreedingOptimize.MD.IVF.ModDataComponents.register(modEventBus);
        ModDataAttachments.ATTACHMENT_TYPES.register(modEventBus);




        //ModItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MDCT.CREATIVE_TABS.register(modEventBus);

        // 注册成就触发器
        MoreBreedingOptimize.ADV.ModTriggers.TRIGGER_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
       // modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        //container.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "morebo-common.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "morebo-common.toml");


    }

    private void commonSetup(FMLCommonSetupEvent event) {


        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");


    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {

        }
    }




    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}