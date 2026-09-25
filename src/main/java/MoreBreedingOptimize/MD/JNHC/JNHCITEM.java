package MoreBreedingOptimize.MD.JNHC;

import MoreBreedingOptimize.MD.DOGM.EternalEffect;
import MoreBreedingOptimize.MainClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


    public class JNHCITEM {
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);

        // --- 物品定义 ---
        public static final DeferredRegister<MobEffect> EFFECTS =
                DeferredRegister.create(Registries.MOB_EFFECT, MainClass.MODID);

        /**
         日本进口生可乐物品类
         * 稀有度：稀有 (UNCOMMON)，显示为黄色
         */
        public static final Supplier<Item> JNHC= ITEMS.register("jnhc", () ->
                new JNHCItemUse()
        );

        public static final Supplier<MobEffect> BZSTRONGER = EFFECTS.register("bzstronger", BZQHEffect::new);

        public static void register(IEventBus modEventBus) {
            ITEMS.register(modEventBus);
            EFFECTS.register(modEventBus);

        }
    }


