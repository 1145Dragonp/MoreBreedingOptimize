package mrmd.morebreedingoptimize.boosfight.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import mrmd.morebreedingoptimize.MainClass;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
        DeferredRegister.create(Registries.STRUCTURE_TYPE, MainClass.MODID);

    public static final DeferredRegister<Structure> STRUCTURES =
        DeferredRegister.create(Registries.STRUCTURE, MainClass.MODID);

    public static final ResourceKey<Structure> SD = ResourceKey.create(
        Registries.STRUCTURE,
        ResourceLocation.fromNamespaceAndPath(MainClass.MODID, "sd")
    );

    public static void register(IEventBus bus) {
        STRUCTURE_TYPES.register(bus);
        STRUCTURES.register(bus);
    }
}