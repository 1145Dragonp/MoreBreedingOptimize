package MoreBreedingOptimize.MD.book;

import MoreBreedingOptimize.MainClass;
import li.cil.manual.api.ManualModel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

/**
 * hbook —— 化学书。
 * <p>
 * 第二本 Markdown Manual 手册物品：右键打开化学合成模块的手册，
 * 起始页为 doc/&lt;语言&gt;/chemistry/index.md（见 {@link ChemistryManual}）。
 * 物品模型 / 贴图已存在于 assets/morebo/models/item/hbook.json 与
 * assets/morebo/textures/item/hbook.png。
 */
public class ChemistryBookItem extends BookItem {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);

    /** 化学书物品，注册 id：morebo:hbook */
    public static final Supplier<Item> HBOOK = ITEMS.register("hbook",
            () -> new ChemistryBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public ChemistryBookItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    protected ManualModel getManualModel() {
        // 化学书手册模型由 BookManualClient 在客户端注册（morebo:manual_chemistry）
        return BookManualClient.MANUAL_CHEMISTRY.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.morebo.hbook.hint").withStyle(ChatFormatting.GRAY));
    }
}
