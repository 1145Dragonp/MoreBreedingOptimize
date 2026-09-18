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
import MoreBreedingOptimize.MD.MDCT;
import MoreBreedingOptimize.MD.ChemistrySynthesis.ChemistryItem;



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

    // ✅ 子弹复制器方块：属性完全内联，不依赖任何外部类
   // public static final DeferredBlock<Block> BULLET_REPLICATOR = BLOCKS.registerSimpleBlock(
    //        "bullet_replicator",
        //    BlockBehaviour.Properties.of()
              //      .mapColor(MapColor.METAL)
                //    .strength(3.5F)
               //     .sound(SoundType.METAL)
                  //  .requiresCorrectToolForDrops()
    //);

    // ✅ 子弹复制器方块物品：直接绑定上方块
    //public static final DeferredItem<BlockItem> BULLET_REPLICATOR_ITEM =
           // ITEMS.registerSimpleBlockItem(BULLET_REPLICATOR);



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

        //if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
         //   LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
       // }

       // LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        //Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            // ✅ 将子弹复制器加入建筑方块创造模式标签页
            //event.accept(BULLET_REPLICATOR_ITEM);
            //event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }




    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}