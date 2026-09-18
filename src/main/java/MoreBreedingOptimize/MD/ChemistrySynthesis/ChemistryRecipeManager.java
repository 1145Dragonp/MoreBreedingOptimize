package MoreBreedingOptimize.MD.ChemistrySynthesis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.*;

/**
 * 化学合成配方管理器 —— 从 DataPack 动态加载配方。
 * <p>
 * 支持两种配方格式：
 * 1. 单配方文件：{ "neededitem": [...], "needsolution": true, ... }
 * 2. 多配方文件：{ "recipe_id": { "neededitem": [...], ... }, "recipe_id2": { ... } }
 */
public class ChemistryRecipeManager extends SimplePreparableReloadListener<Map<String, JsonObject>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PATH = "chemistry_recipes";

    public static final ChemistryRecipeManager INSTANCE = new ChemistryRecipeManager();

    private Map<String, JsonObject> recipes = new HashMap<>();

    private ChemistryRecipeManager() {}

    @Override
    protected Map<String, JsonObject> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, JsonObject> loaded = new HashMap<>();

        profiler.startTick();
        profiler.push("chemistry_recipes");

        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources(
                PATH, p -> p.getPath().endsWith(".json")).entrySet()) {

            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            try (Reader reader = resource.openAsReader()) {
                JsonElement jsonElement = GsonHelper.fromJson(GSON, reader, JsonElement.class);
                if (jsonElement != null && jsonElement.isJsonObject()) {
                    JsonObject json = jsonElement.getAsJsonObject();

                    // 判断是单配方还是多配方格式
                    if (isSingleRecipeFormat(json)) {
                        // 单配方格式：{ "neededitem": [...], "needsolution": true, ... }
                        String recipeId = location.getPath().replace(".json", "");
                        loaded.put(recipeId, json);
                        LOGGER.debug("Loaded chemistry recipe (single): {}", recipeId);
                    } else {
                        // 多配方格式：{ "recipe_id": { ... }, "recipe_id2": { ... } }
                        for (Map.Entry<String, JsonElement> recipeEntry : json.entrySet()) {
                            if (recipeEntry.getValue().isJsonObject()) {
                                String recipeId = recipeEntry.getKey();
                                JsonObject recipeJson = recipeEntry.getValue().getAsJsonObject();
                                loaded.put(recipeId, recipeJson);
                                LOGGER.debug("Loaded chemistry recipe (multi): {}", recipeId);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load chemistry recipe {}", location, e);
            }
        }

        profiler.pop();
        profiler.endTick();
        return loaded;
    }

    @Override
    protected void apply(Map<String, JsonObject> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.recipes = prepared;
        LOGGER.info("Loaded {} chemistry recipes", recipes.size());
    }

    /**
     * 判断是否为单配方格式（直接包含 neededitem/needsolution 等字段）
     */
    private boolean isSingleRecipeFormat(JsonObject json) {
        return json.has("neededitem") || json.has("needsolution") || 
               json.has("clearall") || json.has("clear") || json.has("output");
    }

    /**
     * 查找满足当前试管内容的配方。
     */
    public Optional<JsonObject> findMatchingRecipe(List<ContainedItem> solids, ItemStack solution) {
        for (Map.Entry<String, JsonObject> entry : recipes.entrySet()) {
            if (ChemistryRecipe.matchesRecipe(entry.getValue(), solids, solution)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    public Map<String, JsonObject> getAllRecipes() {
        return Collections.unmodifiableMap(recipes);
    }
}
