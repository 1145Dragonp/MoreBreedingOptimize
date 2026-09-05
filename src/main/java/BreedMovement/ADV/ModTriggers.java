package BreedMovement.ADV;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 触发器注册表
 *
 * 注册所有自定义成就触发器到 Registries.TRIGGER_TYPE。
 * 需要在 MainClass 构造函数中调用 ModTriggers.TRIGGER_TYPES.register(modEventBus)。
 */
public class ModTriggers {

    /**
     * 触发器 DeferredRegister
     */
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES =
            DeferredRegister.create(Registries.TRIGGER_TYPE, "breedmovement");

    /**
     * 触发器
     */
    public static final DeferredHolder<CriterionTrigger<?>, BreedTrigger> BREED_TRIGGER =
            TRIGGER_TYPES.register("breed_with_end_rod", BreedTrigger::new);
}
