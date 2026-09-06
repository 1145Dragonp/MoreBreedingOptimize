package BreedMovement.MD.SM;

import BreedMovement.MainClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 物品注册类
 * 统一管理模组内所有自定义物品的注册
 */
public class SMItem {
    // 创建物品延迟注册器，绑定到你的模组ID
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);

    // --- 物品定义 ---

    /**
     * 春药 (Aphrodisiac)
     * 稀有度：稀有 (UNCOMMON)，显示为黄色
     */
    public static final Supplier<Item> SM = ITEMS.register("sm", () ->
            new Item(new Item.Properties().rarity(Rarity.UNCOMMON))
    );

    public static final Supplier<Item> SMT = ITEMS.register("smt", () -> new LustItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(64)) );

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MainClass.MODID);
    public static final DeferredHolder<MobEffect, MobEffect> LUST = MOB_EFFECTS.register("lust", LustEffect::new);

    // --- 注册方法 ---

    /**
     * 将物品注册器添加到模组事件总线
     * 需要在主类的构造函数中调用
     *
     * @param modEventBus 模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
    }
}
