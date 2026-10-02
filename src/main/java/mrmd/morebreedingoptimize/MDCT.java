package mrmd.morebreedingoptimize;

// 1. 导入所有必须的基础类
import mrmd.morebreedingoptimize.MD.chemistrysynthesis.ChemistryItem;
import mrmd.morebreedingoptimize.MD.SM.SMItem;
import mrmd.morebreedingoptimize.nb.NeuterBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import mrmd.morebreedingoptimize.MD.growthhormone.GrowthHormoneItems;
import mrmd.morebreedingoptimize.MD.IVF.IVFITEM;
import mrmd.morebreedingoptimize.MD.DOGM.DOGMITEM;
import mrmd.morebreedingoptimize.MD.JNHC.JNHCITEM;
import mrmd.morebreedingoptimize.MD.book.BookItem;
import mrmd.morebreedingoptimize.MD.book.ChemistryBookItem;
import mrmd.morebreedingoptimize.boosfight.BoosFightItem;

public class MDCT {

    // 2. 在这里正式声明 CREATIVE_TABS 变量！
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "morebo");

    // 3. 注册具体的创造标签页（此时 CREATIVE_TABS 已经存在，不会报错了）
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MY_TAB =
            CREATIVE_TABS.register("mdct", () -> CreativeModeTab.builder()
                    // 设置标签页的显示名称（支持多语言）
                    .title(Component.translatable("itemGroup.morebo.my_creative_tab"))
                    // 设置标签页的图标
                    .icon(() -> new ItemStack(BookItem.SBOOK.get()))
                    // 添加物品到标签页
                    .displayItems((parameters, output) -> {
                        output.accept(BookItem.SBOOK.get());
                        output.accept(ChemistryBookItem.HBOOK.get());
                        output.accept(SMItem.SM.get());
                        output.accept(GrowthHormoneItems.GHW.get());
                        output.accept(SMItem.syringe.get());
                        output.accept(SMItem.SMT.get());
                        output.accept(GrowthHormoneItems.GH.get());
                        output.accept(ChemistryItem.TEST_TUBE.get());
                        output.accept(IVFITEM.BTS.get());
                        output.accept(SMItem.LB.get());
                        output.accept(DOGMITEM.DOGM.get());
                        output.accept(JNHCITEM.JNHC.get());
                        output.accept(ChemistryItem.RE_X.get());
                        output.accept(NeuterBlockItem.LHQ.get());
                        output.accept(BoosFightItem.KP.get());
                        output.accept(BoosFightItem.KYE.get());
                        output.accept(BoosFightItem.dor.get());
                        output.accept(BoosFightItem.sgc.get());
                        output.accept(BoosFightItem.bw.get());

                    })
                    .build());
}