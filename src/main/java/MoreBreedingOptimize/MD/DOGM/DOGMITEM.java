package MoreBreedingOptimize.MD.DOGM;

import MoreBreedingOptimize.MainClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static net.neoforged.neoforge.client.gui.VanillaGuiLayers.EFFECTS;

public class DOGMITEM {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);

    // --- 物品定义 ---
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, MainClass.MODID);

    /**
     * Mrmagicdargon C
     * 粤西特供野生狗奶
     * 稀有度：稀有 (UNCOMMON)，显示为黄色
     */
    public static final Supplier<Item> DOGM = ITEMS.register("dogm", () ->
            new WildDogMilkItem()
    );

    public static final Supplier<MobEffect> ETERNAL = EFFECTS.register("eternal", EternalEffect::new);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        EFFECTS.register(modEventBus);
    }

}
