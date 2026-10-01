package mrmd.morebreedingoptimize.MD.chemistrysynthesis;

import com.google.gson.JsonElement;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestTubeItem extends Item {
    private static final int MODEL_EMPTY = 0;
    private static final int MODEL_SOLUTION = 1;
    private static final int MODEL_SOLID = 2;

    /** 试管内最多可塞几个固体 */
    private final int maxSolids;

    public TestTubeItem(Properties properties, int maxSolids) {
        super(properties.stacksTo(1));
        this.maxSolids = maxSolids;
    }

    public int getMaxSolids() {
        return maxSolids;
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
        Integer react = stack.get(ChemistryDataComponents.REACT_TICKS.get());
        if (react != null && react > 0) {
            tooltip.add(Component.translatable("tooltip.morebo.test_tube.reacting", react / 20 + 1));
        }
        if (!hasSolution && (solids == null || solids.isEmpty()) && (react == null || react <= 0)) {
            tooltip.add(Component.translatable("tooltip.morebo.chemistry.empty"));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        boolean hasSolution = stack.getOrDefault(ChemistryDataComponents.HAS_SOLUTION.get(), false);
        List<ContainedItem> solids = stack.get(ChemistryDataComponents.SOLIDS.get());
        Integer react = stack.get(ChemistryDataComponents.REACT_TICKS.get());
        return hasSolution || (solids != null && !solids.isEmpty()) || (react != null && react > 0);
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

        // --- 2. 反应中的试管不能再塞东西 ---
        Integer react = stack.get(ChemistryDataComponents.REACT_TICKS.get());
        if (react != null && react > 0) {
            return InteractionResultHolder.pass(stack);
        }

        // --- 3. 正常右键：塞入物品 ---
        if (!heldItem.isEmpty()) {
            boolean isSolutionItem = heldItem.getItem() instanceof net.minecraft.world.item.BucketItem || heldItem.has(DataComponents.POTION_CONTENTS);
            if (isSolutionItem) {
                stack.set(ChemistryDataComponents.HAS_SOLUTION.get(), true);
                if (!player.isCreative()) heldItem.shrink(1);
            } else {
                List<ContainedItem> currentSolids = stack.get(ChemistryDataComponents.SOLIDS.get());
                List<ContainedItem> mutableSolids = (currentSolids != null) ? new ArrayList<>(currentSolids) : new ArrayList<>();
                if (mutableSolids.size() >= maxSolids) {
                    player.displayClientMessage(Component.translatable("tooltip.morebo.test_tube.full"), true);
                    return InteractionResultHolder.pass(stack);
                }
                mutableSolids.add(new ContainedItem(heldItem));
                stack.set(ChemistryDataComponents.SOLIDS.get(), mutableSolids);
                if (!player.isCreative()) heldItem.shrink(1);
            }

            // --- 4. 检查并执行配方 ---
            List<ContainedItem> finalSolids = stack.get(ChemistryDataComponents.SOLIDS.get());
            boolean hasSolution = stack.getOrDefault(ChemistryDataComponents.HAS_SOLUTION.get(), false);
            ItemStack solutionStack = hasSolution ? new ItemStack(Items.POTION) : ItemStack.EMPTY;

            RegistryOps<JsonElement> ops = ChemistryRecipe.ops(level.registryAccess());
            Optional<com.google.gson.JsonObject> recipeOpt = ChemistryRecipeManager.INSTANCE.findMatchingRecipe(
                    finalSolids != null ? finalSolids : List.of(),
                    solutionStack,
                    ops
            );

            if (recipeOpt.isPresent()) {
                com.google.gson.JsonObject recipe = recipeOpt.get();
                int tiem = recipe.has("tiem") ? recipe.get("tiem").getAsInt() : 0;
                if (tiem > 0) {
                    // 延迟反应：启动倒计时，期间由 ticker 持续/到点处理
                    stack.set(ChemistryDataComponents.REACT_TICKS.get(), tiem);
                    playRecipeSound(level, player, recipe, false);
                } else {
                    ChemistryRecipe.executeRecipe(recipe, stack, ops);
                    playRecipeSound(level, player, recipe, true);
                }
                player.displayClientMessage(Component.translatable("tooltip.morebo.chemistry.craft_success"), true);
                updateAppearance(stack);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }

            updateAppearance(stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    /** 播放配方声音；soundid 缺省用默认提示音。done=true 表示反应完成。 */
    public static void playRecipeSound(Level level, Player player, com.google.gson.JsonObject recipe, boolean done) {
        String soundId = "minecraft:entity.experience_orb.pickup";
        if (recipe.has("sound") && recipe.getAsJsonObject("sound").has("soundid")) {
            soundId = recipe.getAsJsonObject("sound").get("soundid").getAsString();
        }
        ResourceLocation rl = ResourceLocation.tryParse(soundId);
        if (rl == null) return;
        float pitch = done ? 1.2f : 0.8f;
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvent.createVariableRangeEvent(rl), SoundSource.PLAYERS, 0.6f, pitch);
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