package MoreBreedingOptimize.MD.IVF;

import MoreBreedingOptimize.MainClass;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(MainClass.MODID);

    /** 存储生物实体ID的组件，值为 String 类型（如 "minecraft:cow"） */
    public static final Supplier<DataComponentType<String>> SOURCE_ENTITY =
            COMPONENTS.registerComponentType("source_entity", builder ->
                    builder.persistent(Codec.STRING)
            );

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }
}