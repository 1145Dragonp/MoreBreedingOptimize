package MoreBreedingOptimize.MD.ChemistrySynthesis;

import MoreBreedingOptimize.MainClass;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ChemistryDataComponents {
    // 注册 DeferredRegister
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MainClass.MODID);

    // 1. 溶液标记（Boolean）
    public static final Supplier<DataComponentType<Boolean>> HAS_SOLUTION = DATA_COMPONENTS.register("has_solution",
            () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    // 2. 固体列表（List<ContainedItem>）- 修改点：支持多个固体
    public static final Supplier<DataComponentType<List<ContainedItem>>> SOLIDS = DATA_COMPONENTS.register("solids",
            () -> DataComponentType.<List<ContainedItem>>builder()
                    // 使用 ContainedItem 的 Codec 列表进行持久化
                    .persistent(ContainedItem.CODEC.listOf())
                    // 网络同步
                    .networkSynchronized(ContainedItem.STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());

    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}