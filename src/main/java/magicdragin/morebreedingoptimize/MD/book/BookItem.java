package magicdragin.morebreedingoptimize.MD.book;

import magicdragin.morebreedingoptimize.MainClass;
import li.cil.manual.api.ManualModel;
import li.cil.manual.api.ManualScreenStyle;
import li.cil.manual.api.prefab.item.AbstractManualItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

/**
 * sbook —— 更多繁殖优化（morebo）模组手册。
 * <p>
 * 基于 Markdown Manual 库（modid: markdown_manual）的手册物品：
 * 右键即可在客户端打开该库自带的 Markdown 手册界面。
 * 手册模型与文档提供者仅在客户端注册（见 {@link BookManualClient}），
 * 手册正文位于 assets/morebo/doc/&lt;语言&gt;/index.MD（Markdown 语法）。
 */
public class BookItem extends AbstractManualItem {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MainClass.MODID);

    /** 手册物品，注册 id：morebo:sbook */
    public static final Supplier<Item> SBOOK = ITEMS.register("sbook",
            () -> new BookItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public BookItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    protected ManualModel getManualModel() {
        // 手册模型由 BookManualClient 在客户端注册（morebo:manual）
        return BookManualClient.MANUAL.get();
    }

    @Override
    protected ManualScreenStyle getScreenStyle() {
        // 自定义手册底图：assets/morebo/textures/gui/manual.png（256x256，与默认窗口矩形一致）
        return new ManualScreenStyle() {
            @Override
            public ResourceLocation getWindowBackground() {
                return ResourceLocation.fromNamespaceAndPath(MainClass.MODID, "textures/gui/manual.png");
            }
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.morebo.sbook.hint").withStyle(ChatFormatting.GRAY));
    }
}
