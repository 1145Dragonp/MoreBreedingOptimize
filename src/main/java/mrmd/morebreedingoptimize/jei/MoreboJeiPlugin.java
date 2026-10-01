package mrmd.morebreedingoptimize.jei;

import com.google.gson.JsonObject;
import mrmd.morebreedingoptimize.MD.chemistrysynthesis.ChemistryRecipeManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MoreBo 的 JEI 插件入口。
 * 把自定义的化学合成配方（chemistry_recipes/*.json）注册给 JEI，
 * 让 ghw / sm / rex 等只能通过试管合成的物品在 JEI 里显示"化学合成"配方。
 */
@JeiPlugin
public class MoreboJeiPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath("morebo", "jei_plugin");

    public static final RecipeType<ChemistryRecipeWrapper> CHEMISTRY =
            RecipeType.create("morebo", "chemistry", ChemistryRecipeWrapper.class);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ChemistryRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ChemistryRecipeWrapper> wrappers = new ArrayList<>();
        for (Map.Entry<String, JsonObject> entry : ChemistryRecipeManager.INSTANCE.getAllRecipes().entrySet()) {
            ChemistryRecipeWrapper.tryCreate(entry.getKey(), entry.getValue()).ifPresent(wrappers::add);
        }
        registration.addRecipes(CHEMISTRY, wrappers);
    }
}
