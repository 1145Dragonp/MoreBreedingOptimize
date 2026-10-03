package mrmd.morebreedingoptimize.worldgen;

import mrmd.morebreedingoptimize.MainClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ModWorldGen {
    public static final ResourceKey<PlacedFeature> SD_PLACED = ResourceKey.create(
        Registries.PLACED_FEATURE,
        ResourceLocation.fromNamespaceAndPath(MainClass.MODID, "sd")
    );
}