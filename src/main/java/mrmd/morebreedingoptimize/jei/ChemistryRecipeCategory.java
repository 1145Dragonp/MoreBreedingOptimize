package mrmd.morebreedingoptimize.jei;

import mrmd.morebreedingoptimize.MD.chemistrysynthesis.ChemistryItem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * JEI 配方类别："化学合成"。
 * 输入槽为试管里的固体材料（neededitem），输出槽为产物；需要溶液的配方会标注文字。
 */
public class ChemistryRecipeCategory implements IRecipeCategory<ChemistryRecipeWrapper> {

    private final IDrawable background;
    private final IDrawable icon;

    public ChemistryRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(120, 60);
        this.icon = helper.createDrawableItemStack(new ItemStack(ChemistryItem.TEST_TUBE.get()));
    }

    @Override
    public RecipeType<ChemistryRecipeWrapper> getRecipeType() {
        return MoreboJeiPlugin.CHEMISTRY;
    }

    @Override
    public Component getTitle() {
        return Component.literal("化学合成");
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 60;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ChemistryRecipeWrapper recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = recipe.getInputs();
        for (int i = 0; i < inputs.size(); i++) {
            int x = 1 + (i % 4) * 18;
            int y = 1 + (i / 4) * 18;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStack(inputs.get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 22).addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(ChemistryRecipeWrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe.needsSolution()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "需要溶液", 6, 52, 0xFF404040, false);
        }
    }
}
