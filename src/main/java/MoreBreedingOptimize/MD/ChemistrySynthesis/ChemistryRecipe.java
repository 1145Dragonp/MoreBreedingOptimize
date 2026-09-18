package MoreBreedingOptimize.MD.ChemistrySynthesis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;

import java.util.ArrayList;
import java.util.List;

public class ChemistryRecipe {
    /**
     * 检查试管内容是否匹配配方
     *
     * @param json     配方 JSON
     * @param solids   试管中的固体列表（List<ContainedItem>）
     * @param solution 试管中的溶液 ItemStack（可能为 null）
     * @return 是否匹配
     */
    public static boolean matchesRecipe(JsonObject json, List<ContainedItem> solids, ItemStack solution) {
        // 1. 检查溶液需求
        if (json.has("needsolution")) {
            boolean needsSol = json.get("needsolution").getAsBoolean();
            if (needsSol) {
                if (solution == null || solution.isEmpty()) return false;
            } else {
                if (solution != null && !solution.isEmpty()) return false;
            }
        }
        // 2. 检查固体需求
        if (json.has("neededitem")) {
            JsonArray neededArr = json.getAsJsonArray("neededitem");
            for (JsonElement elem : neededArr) {
                String neededId = elem.getAsString();
                boolean found = false;
                for (ContainedItem contained : solids) {
                    // 通过 ContainedItem.stack() 获取 ItemStack，再查注册表ID
                    ItemStack stack = contained.stack();
                    ResourceLocation solidId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (solidId != null && solidId.toString().equals(neededId)) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
            return true;
        }
        return false; // 如果没有 neededitem 字段，视为不匹配（或根据需求返回 true）
    }

    /**
     * 执行配方：清除物品并添加产物
     *
     * @param json      配方 JSON
     * @param tubeStack 试管 ItemStack
     */
    public static void executeRecipe(JsonObject json, ItemStack tubeStack) {
        boolean clearAll = json.has("clearall") && json.get("clearall").getAsBoolean();
        // 1. 清除试管内容
        if (clearAll) {
            // 修复点：SOLUTION -> HAS_SOLUTION
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

            // 修复点：getValue -> getOptional
            Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(itemId)).orElse(null);

            if (item != null) {
                ItemStack output = new ItemStack(item, count);
                // 产物是桶/药水类 → 放入溶液槽
                boolean isSolution = output.getItem() instanceof net.minecraft.world.item.BucketItem || output.has(DataComponents.POTION_CONTENTS);
                if (isSolution) {
                    // @AI 修复：HAS_SOLUTION 是 Boolean 组件，不能塞 ItemStack。
                    // 溶液槽只存"有无"标记（取出时统一给药水），这里只置 true
                    tubeStack.set(ChemistryDataComponents.HAS_SOLUTION.get(), true);
                } else {
                    // 产物放入固体槽，包装成 ContainedItem
                    List<ContainedItem> solids = tubeStack.get(ChemistryDataComponents.SOLIDS.get());
                    List<ContainedItem> mutableSolids = (solids != null) ? new ArrayList<>(solids) : new ArrayList<>();
                    mutableSolids.add(new ContainedItem(output));
                    tubeStack.set(ChemistryDataComponents.SOLIDS.get(), mutableSolids);
                }
            }
        }
    }
}