package MoreBreedingOptimize.MD.DOGM;

import MoreBreedingOptimize.MainClass;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DOGMITEM {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);

    // --- 物品定义 ---

    /**
     * Mrmagicdargon C
     * 粤西特供野生狗奶
     * 稀有度：稀有 (UNCOMMON)，显示为黄色
     */
    public static final Supplier<Item> DOGM = ITEMS.register("dogm", () ->
            new Item(new Item.Properties().rarity(Rarity.UNCOMMON))
    );

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);

    }

}
