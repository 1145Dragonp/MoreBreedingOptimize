package BreedMovement.MD.JNHC;

import BreedMovement.MainClass;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


    public class JNHCITEM {
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);

        // --- 物品定义 ---

        /**
         * 春药 (Aphrodisiac)
         * 稀有度：稀有 (UNCOMMON)，显示为黄色
         */
        public static final Supplier<Item> JNHC= ITEMS.register("jnhc", () ->
                new Item(new Item.Properties().rarity(Rarity.EPIC))
        );

        public static void register(IEventBus modEventBus) {
            ITEMS.register(modEventBus);

        }
    }


