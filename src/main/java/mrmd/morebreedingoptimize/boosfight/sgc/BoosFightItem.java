package mrmd.morebreedingoptimize.boosfight.sgc;

import mrmd.morebreedingoptimize.MainClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@EventBusSubscriber(modid = MainClass.MODID, bus = EventBusSubscriber.Bus.MOD)
public class BoosFightItem {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MainClass.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MainClass.MODID);

    public static final Supplier<Item> sgc = ITEMS.register("sgc", () -> new SgcItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Supplier<Block> bw = BLOCKS.register("bw", () -> new BwBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 6.0F).requiresCorrectToolForDrops().noOcclusion()));
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.BlockItem> bw_item = ITEMS.registerSimpleBlockItem("bw", bw);

    public static final Supplier<Block> ml = BLOCKS.register("ml", () -> new MuralBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.0F, 4.0F).requiresCorrectToolForDrops().noOcclusion()));
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.BlockItem> ml_item = ITEMS.registerSimpleBlockItem("ml", ml);

    public static final Supplier<EntityType<SgcEntity>> SGC_ENTITY = ENTITIES.register("sgc",
            () -> EntityType.Builder.of(SgcEntity::new, MobCategory.MISC)
                    .sized(1.0F, 0.5F)
                    .clientTrackingRange(10)
                    .build("sgc"));

    @SubscribeEvent
    public static void addAttributes(EntityAttributeCreationEvent event) {
        event.put(SGC_ENTITY.get(), SgcEntity.createAttributes().build());
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        BLOCKS.register(modEventBus);
    }
}
