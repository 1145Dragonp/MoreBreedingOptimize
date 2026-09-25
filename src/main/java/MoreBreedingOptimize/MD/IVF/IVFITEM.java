package MoreBreedingOptimize.MD.IVF;

import MoreBreedingOptimize.MainClass;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class IVFITEM {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);

    // --- 物品定义 ---


    public static final Supplier<Item> BTS= ITEMS.register("bts", () ->
            new Item(new Item.Properties().rarity(Rarity.EPIC))
    );



    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);

    }
}
