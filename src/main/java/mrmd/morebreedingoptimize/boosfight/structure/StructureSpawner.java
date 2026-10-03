package mrmd.morebreedingoptimize.boosfight.structure;

import mrmd.morebreedingoptimize.MainClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = MainClass.MODID)
public class StructureSpawner {

    private static final List<BlockPos> queue = new ArrayList<>();
    private static boolean started = false;

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (started) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        started = true;
        ServerLevel level = player.serverLevel();

        int[] bases = {334, 3340, 1225};
        for (int base : bases) {
            queue.add(new BlockPos(base, 0, base));
            queue.add(new BlockPos(base, 0, -base));
            queue.add(new BlockPos(-base, 0, base));
            queue.add(new BlockPos(-base, 0, -base));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (queue.isEmpty()) return;
        
        BlockPos target = queue.remove(0);
        ServerLevel level = event.getServer().overworld();
        placeTemple(level, target);
    }

    private static void placeTemple(ServerLevel level, BlockPos target) {
        try {
            ResourceLocation resourceLoc = ResourceLocation.fromNamespaceAndPath(MainClass.MODID, "structures/sd.nbt");
            Optional<Resource> resOpt = level.getServer().getResourceManager().getResource(resourceLoc);
            if (resOpt.isEmpty()) return;

            StructureTemplate template = new StructureTemplate();
            try (InputStream is = resOpt.get().open()) {
                CompoundTag nbt = NbtIo.readCompressed(is, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                HolderGetter<Block> blockGetter = level.holderLookup(Registries.BLOCK);
                template.load(blockGetter, nbt);
            }

            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, target.getX(), target.getZ());
            BlockPos placePos = new BlockPos(target.getX(), y, target.getZ());

            StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.NONE)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false);

            template.placeInWorld(level, placePos, placePos, settings, level.random, 2);
            System.out.println("[morebo] sd temple placed at " + placePos);
        } catch (Exception e) {
            System.err.println("[morebo] place failed: " + e.getMessage());
        }
    }
}