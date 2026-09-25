package MoreBreedingOptimize.MD.IVF;

import MoreBreedingOptimize.MD.DOGM.EternalEffect;
import MoreBreedingOptimize.MainClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class IVFITEM {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);

    // --- 物品定义 ---
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, MainClass.MODID);


    public static final Supplier<Item> BTS= ITEMS.register("bts", () ->
            new Item(new Item.Properties().rarity(Rarity.EPIC))
    );

    public static final Supplier<MobEffect> BZSTRONGER = EFFECTS.register("bzstronger", EternalEffect::new);



    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);

    }
}
