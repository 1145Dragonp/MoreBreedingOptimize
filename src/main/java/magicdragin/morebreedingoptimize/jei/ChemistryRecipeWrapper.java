package magicdragin.morebreedingoptimize.jei;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 化学合成配方的 JEI 包装器。
 * 把 data/morebo/chemistry_recipes/*.json 里的一个配方转成 JEI 可渲染的数据：
 * 输入（试管固体 neededitem）→ 输出（output），needsolution 标记是否需要溶液。
 */
public class ChemistryRecipeWrapper {

    private final String id;
    private final List<ItemStack> inputs;
    private final boolean needsSolution;
    private final ItemStack output;

    private ChemistryRecipeWrapper(String id, List<ItemStack> inputs, boolean needsSolution, ItemStack output) {
        this.id = id;
        this.inputs = inputs;
        this.needsSolution = needsSolution;
        this.output = output;
    }

    /**
     * 从配方 JSON 创建包装器；材料或产物缺失时返回 empty。
     */
    public static Optional<ChemistryRecipeWrapper> tryCreate(String id, JsonObject json) {
        List<ItemStack> inputs = new ArrayList<>();
        if (json.has("neededitem")) {
            JsonArray arr = json.getAsJsonArray("neededitem");
            for (JsonElement elem : arr) {
                ItemStack stack = parseItem(elem.getAsString());
                if (!stack.isEmpty()) inputs.add(stack);
            }
        }
        boolean needsSolution = json.has("needsolution") && json.get("needsolution").getAsBoolean();
        ItemStack output = ItemStack.EMPTY;
        if (json.has("output")) {
            JsonObject out = json.getAsJsonObject("output");
            output = parseItem(out.get("id").getAsString());
            if (out.has("count")) output.setCount(out.get("count").getAsInt());
        }
        if (inputs.isEmpty() || output.isEmpty()) return Optional.empty();
        return Optional.of(new ChemistryRecipeWrapper(id, inputs, needsSolution, output));
    }

    private static ItemStack parseItem(String id) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id))
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

    public String getId() {
        return id;
    }

    public List<ItemStack> getInputs() {
        return inputs;
    }

    public boolean needsSolution() {
        return needsSolution;
    }

    public ItemStack getOutput() {
        return output;
    }
}
