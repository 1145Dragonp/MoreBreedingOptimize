package mrmd.morebreedingoptimize.MD.chemistrysynthesis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChemistryRecipe {

    /**
     * 检查试管内容是否匹配配方（新格式：neededitem 为对象数组）。
     *
     * @param json     配方 JSON
     * @param solids   试管中的固体列表（List<ContainedItem>）
     * @param solution 试管中的溶液 ItemStack（可能为 null/empty）
     * @param ops      RegistryOps（解析组件用），通常来自 level.registryAccess()
     */
    public static boolean matchesRecipe(JsonObject json, List<ContainedItem> solids, ItemStack solution, RegistryOps<JsonElement> ops) {
        // 1. 检查溶液需求
        if (json.has("needsolution")) {
            boolean needsSol = json.get("needsolution").getAsBoolean();
            if (needsSol) {
                if (solution == null || solution.isEmpty()) return false;
            } else {
                if (solution != null && !solution.isEmpty()) return false;
            }
        }
        // 2. 检查固体需求（新格式：每项 {id, component?, count?}）
        if (json.has("neededitem")) {
            JsonArray neededArr = json.getAsJsonArray("neededitem");
            for (JsonElement elem : neededArr) {
                JsonObject need = elem.getAsJsonObject();
                String neededId = need.get("id").getAsString();
                int requiredCount = need.has("count") ? need.get("count").getAsInt() : 1;

                ItemStack template = buildTemplateStack(neededId, need.has("component") ? need.getAsJsonObject("component") : null, ops);
                if (template.isEmpty()) return false;

                // 在试管固体里数出多少个与模板同物品同组件
                int matched = 0;
                for (ContainedItem contained : solids) {
                    if (ItemStack.isSameItemSameComponents(template, contained.stack())) {
                        matched++;
                    }
                }
                if (matched < requiredCount) return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 执行配方：按 clearall/clear 消耗材料，写入产物（含可选组件）。
     */
    public static void executeRecipe(JsonObject json, ItemStack tubeStack, RegistryOps<JsonElement> ops) {
        boolean clearAll = json.has("clearall") && json.get("clearall").getAsBoolean();
        // 1. 清除试管内容
        if (clearAll) {
            tubeStack.remove(ChemistryDataComponents.HAS_SOLUTION.get());
            tubeStack.remove(ChemistryDataComponents.SOLIDS.get());
        } else if (json.has("clear")) {
            List<ContainedItem> solids = tubeStack.get(ChemistryDataComponents.SOLIDS.get());
            if (solids != null) {
                JsonArray clearArr = json.getAsJsonArray("clear");
                List<ContainedItem> newSolids = new ArrayList<>();
                for (ContainedItem contained : solids) {
                    boolean shouldClear = false;
                    ItemStack stack = contained.stack();
                    ResourceLocation solidId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    for (JsonElement elem : clearArr) {
                        if (solidId != null && solidId.toString().equals(elem.getAsString())) {
                            shouldClear = true;
                            break;
                        }
                    }
                    if (!shouldClear) newSolids.add(contained);
                }
                if (newSolids.isEmpty()) {
                    tubeStack.remove(ChemistryDataComponents.SOLIDS.get());
                } else {
                    tubeStack.set(ChemistryDataComponents.SOLIDS.get(), newSolids);
                }
            }
        }
        // 2. 添加产物
        if (json.has("output")) {
            JsonObject outObj = json.getAsJsonObject("output");
            String itemId = outObj.get("id").getAsString();
            int count = outObj.has("count") ? outObj.get("count").getAsInt() : 1;

            Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(itemId)).orElse(null);
            if (item == null) return;

            ItemStack output = new ItemStack(item, count);
            if (outObj.has("component")) {
                DataComponentPatch patch = DataComponentPatch.CODEC.decode(ops, outObj.getAsJsonObject("component")).getOrThrow().getFirst();
                output.applyComponents(patch);
            }

            // 产物是桶/药水类 → 放入溶液槽
            boolean isSolution = output.getItem() instanceof net.minecraft.world.item.BucketItem || output.has(DataComponents.POTION_CONTENTS);
            if (isSolution) {
                tubeStack.set(ChemistryDataComponents.HAS_SOLUTION.get(), true);
            } else {
                List<ContainedItem> solids = tubeStack.get(ChemistryDataComponents.SOLIDS.get());
                List<ContainedItem> mutableSolids = (solids != null) ? new ArrayList<>(solids) : new ArrayList<>();
                mutableSolids.add(new ContainedItem(output));
                tubeStack.set(ChemistryDataComponents.SOLIDS.get(), mutableSolids);
            }
        }
    }

    /**
     * 由配方里的 {id, component} 构造一个模板 ItemStack（用于组件严格比对）。
     */
    private static ItemStack buildTemplateStack(String id, JsonObject componentJson, RegistryOps<JsonElement> ops) {
        Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id)).orElse(null);
        if (item == null) return ItemStack.EMPTY;
        ItemStack template = new ItemStack(item);
        if (componentJson != null) {
            DataComponentPatch patch = DataComponentPatch.CODEC.decode(ops, componentJson).getOrThrow().getFirst();
            template.applyComponents(patch);
        }
        return template;
    }

    /** 便捷：从 RegistryAccess 构造 ops */
    public static RegistryOps<JsonElement> ops(RegistryAccess access) {
        return RegistryOps.create(JsonOps.INSTANCE, access);
    }
}