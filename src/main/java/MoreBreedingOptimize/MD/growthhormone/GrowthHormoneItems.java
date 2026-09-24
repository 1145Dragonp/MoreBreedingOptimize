package MoreBreedingOptimize.MD.growthhormone;

import MoreBreedingOptimize.MainClass;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class GrowthHormoneItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);

    public static final Supplier<Item> GHW = ITEMS.register("ghw", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));


    //public static final Supplier<Item> GH = ITEMS.register("gh", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final Supplier<Item> GH = ITEMS.register("gh", () -> new GrowthHormoneItem());
    public static void register(IEventBus modEventBus) {
        // 注册所有物品
        ITEMS.register(modEventBus);

    }
}
