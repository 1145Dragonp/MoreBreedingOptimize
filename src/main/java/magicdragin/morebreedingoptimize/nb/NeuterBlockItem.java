package magicdragin.morebreedingoptimize.nb;

import magicdragin.morebreedingoptimize.MD.DOGM.WildDogMilkItem;
import magicdragin.morebreedingoptimize.MainClass;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeuterBlockItem {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);

    public static final Supplier<Item> LHQ = ITEMS.register("lhq", () -> new Item(new Item.Properties().rarity(Rarity.COMMON))
    );

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
