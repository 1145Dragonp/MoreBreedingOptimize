package mrmd.morebreedingoptimize.dc;

import mrmd.morebreedingoptimize.MainClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DCItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MainClass.MODID);

    public static final ResourceKey<JukeboxSong> FFC_SONG_KEY = ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(MainClass.MODID, "ffc"));

    public static final Supplier<SoundEvent> FFC_MUSIC = SOUND_EVENTS.register("ffc", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MainClass.MODID, "ffc")));

    public static final Supplier<Item> ffc_record = ITEMS.register("ffc_record", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
        .jukeboxPlayable(FFC_SONG_KEY)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
    }
}
