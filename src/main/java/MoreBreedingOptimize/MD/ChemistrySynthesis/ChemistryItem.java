package MoreBreedingOptimize.MD.ChemistrySynthesis;

import MoreBreedingOptimize.MainClass;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 化学合成模块统一注册入口。
 */
public class ChemistryItem {
    // 物品注册器
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);

    // 1. 注册原有的 sg 物品
    //public static final Supplier<Item> SG = ITEMS.register("sg", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // 2. 注册新的试管物品
    // ✅ 修复：传入 new Item.Properties() 参数
    public static final Supplier<Item> TEST_TUBE = ITEMS.register("test_tube", () -> new TestTubeItem(new Item.Properties()));

    public static final Supplier<Item> RE_X = ITEMS.register("rex",() -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    public static void register(IEventBus modEventBus) {
        // 注册所有物品
        ITEMS.register(modEventBus);
        // 3. 注册数据组件（用于存储化学物质）
        ChemistryDataComponents.register(modEventBus);
    }
}