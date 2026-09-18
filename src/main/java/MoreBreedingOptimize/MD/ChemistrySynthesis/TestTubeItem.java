package MoreBreedingOptimize.MD.ChemistrySynthesis;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestTubeItem extends Item {
    private static final int MODEL_EMPTY = 0;
    private static final int MODEL_SOLUTION = 1;
    private static final int MODEL_SOLID = 2;

    public TestTubeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        boolean hasSolution = stack.getOrDefault(ChemistryDataComponents.HAS_SOLUTION.get(), false);
        List<ContainedItem> solids = stack.get(ChemistryDataComponents.SOLIDS.get());

        if (hasSolution) {
            tooltip.add(Component.translatable("tooltip.morebo.test_tube.solution"));
        }
        if (solids != null && !solids.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.morebo.test_tube.solids"));
            for (ContainedItem item : solids) {
                tooltip.add(Component.translatable("tooltip.morebo.test_tube.solid_item", item.stack().getHoverName()));
            }
        }
        if (!hasSolution && (solids == null || solids.isEmpty())) {
            tooltip.add(Component.translatable("tooltip.morebo.chemistry.empty"));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        boolean hasSolution = stack.getOrDefault(ChemistryDataComponents.HAS_SOLUTION.get(), false);
        List<ContainedItem> solids = stack.get(ChemistryDataComponents.SOLIDS.get());
        return hasSolution || (solids != null && !solids.isEmpty());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionHand otherHand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack heldItem = player.getItemInHand(otherHand);

        // --- 1. 潜行 (蹲下) + 右键：取出物品 ---
        if (player.isShiftKeyDown()) {
            boolean hasSolution = stack.getOrDefault(ChemistryDataComponents.HAS_SOLUTION.get(), false);
            List<ContainedItem> solids = stack.get(ChemistryDataComponents.SOLIDS.get());

            if (hasSolution) {
                ItemStack solutionOutput = new ItemStack(Items.POTION);
                if (!player.getInventory().add(solutionOutput)) {
                    player.drop(solutionOutput, false);
                }
                stack.remove(ChemistryDataComponents.HAS_SOLUTION.get());
                player.displayClientMessage(Component.translatable("tooltip.morebo.chemistry.poured_solution"), true);
            } else if (solids != null && !solids.isEmpty()) {
                // 创建可变副本，避免操作不可变列表
                List<ContainedItem> mutableSolids = new ArrayList<>(solids);
                ContainedItem removed = mutableSolids.remove(mutableSolids.size() - 1);
                if (!player.getInventory().add(removed.stack())) {
                    player.drop(removed.stack(), false);
                }
                if (mutableSolids.isEmpty()) {
                    stack.remove(ChemistryDataComponents.SOLIDS.get());
                } else {
                    stack.set(ChemistryDataComponents.SOLIDS.get(), mutableSolids);
                }
            } else {
                player.displayClientMessage(Component.translatable("tooltip.morebo.chemistry.empty"), true);
                return InteractionResultHolder.pass(stack);
            }
            updateAppearance(stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        // --- 2. 正常右键：塞入物品 ---
        if (!heldItem.isEmpty()) {
            boolean isSolutionItem = heldItem.getItem() instanceof net.minecraft.world.item.BucketItem || heldItem.has(DataComponents.POTION_CONTENTS);
            if (isSolutionItem) {
                stack.set(ChemistryDataComponents.HAS_SOLUTION.get(), true);
                if (!player.isCreative()) heldItem.shrink(1);
            } else {
                // 获取当前固体列表，创建可变副本
                List<ContainedItem> currentSolids = stack.get(ChemistryDataComponents.SOLIDS.get());
                List<ContainedItem> mutableSolids = (currentSolids != null) ? new ArrayList<>(currentSolids) : new ArrayList<>();
                mutableSolids.add(new ContainedItem(heldItem));
                stack.set(ChemistryDataComponents.SOLIDS.get(), mutableSolids);
                if (!player.isCreative()) heldItem.shrink(1);
            }

            // --- 3. 检查并执行配方 ---
            List<ContainedItem> finalSolids = stack.get(ChemistryDataComponents.SOLIDS.get());
            boolean hasSolution = stack.getOrDefault(ChemistryDataComponents.HAS_SOLUTION.get(), false);
            ItemStack solutionStack = hasSolution ? new ItemStack(Items.POTION) : ItemStack.EMPTY;

            // 使用配方管理器查找匹配配方
            Optional<com.google.gson.JsonObject> recipeOpt = ChemistryRecipeManager.INSTANCE.findMatchingRecipe(
                    finalSolids != null ? finalSolids : List.of(),
                    solutionStack
            );

            if (recipeOpt.isPresent()) {
                ChemistryRecipe.executeRecipe(recipeOpt.get(), stack);
                player.displayClientMessage(Component.translatable("tooltip.morebo.chemistry.craft_success"), true);
                updateAppearance(stack);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }

            updateAppearance(stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    private void updateAppearance(ItemStack stack) {
        boolean hasSolution = stack.getOrDefault(ChemistryDataComponents.HAS_SOLUTION.get(), false);
        List<ContainedItem> solids = stack.get(ChemistryDataComponents.SOLIDS.get());
        int modelId = MODEL_EMPTY;

        if (hasSolution) {
            modelId = MODEL_SOLUTION;
        } else if (solids != null && !solids.isEmpty()) {
            modelId = MODEL_SOLID;
        }

        if (modelId == MODEL_EMPTY) {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(modelId));
        }
    }
}
