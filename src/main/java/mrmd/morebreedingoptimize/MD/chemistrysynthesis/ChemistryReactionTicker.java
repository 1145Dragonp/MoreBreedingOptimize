package mrmd.morebreedingoptimize.MD.chemistrysynthesis;

import com.google.gson.JsonElement;
import mrmd.morebreedingoptimize.MainClass;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Optional;

/**
 * 化学试管延迟反应倒计时器。
 * 每个 server tick 扫描玩家背包里 REACT_TICKS>0 的试管：
 *  - keepsringing=true 期间每 5 tick 播一次配方声音；
 *  - 倒计时归零时重新匹配配方并产出。
 */
@EventBusSubscriber(modid = MainClass.MODID)
public class ChemistryReactionTicker {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        RegistryOps<JsonElement> ops = ChemistryRecipe.ops(player.level().registryAccess());

        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            Integer ticks = stack.get(ChemistryDataComponents.REACT_TICKS.get());
            if (ticks == null || ticks <= 0) continue;

            List<ContainedItem> solids = stack.get(ChemistryDataComponents.SOLIDS.get());
            boolean hasSol = stack.getOrDefault(ChemistryDataComponents.HAS_SOLUTION.get(), false);
            ItemStack solStack = hasSol ? new ItemStack(Items.POTION) : ItemStack.EMPTY;

            Optional<com.google.gson.JsonObject> recipeOpt = ChemistryRecipeManager.INSTANCE
                    .findMatchingRecipe(solids != null ? solids : List.of(), solStack, ops);

            if (recipeOpt.isPresent()) {
                com.google.gson.JsonObject recipe = recipeOpt.get();
                boolean keepRing = recipe.has("sound")
                        && recipe.getAsJsonObject("sound").has("keepsringing")
                        && recipe.getAsJsonObject("sound").get("keepsringing").getAsBoolean();
                if (keepRing && ticks % 5 == 0) {
                    TestTubeItem.playRecipeSound(player.level(), player, recipe, false);
                }
            }

            int left = ticks - 1;
            if (left <= 0) {
                stack.remove(ChemistryDataComponents.REACT_TICKS.get());
                if (recipeOpt.isPresent()) {
                    ChemistryRecipe.executeRecipe(recipeOpt.get(), stack, ops);
                    TestTubeItem.playRecipeSound(player.level(), player, recipeOpt.get(), true);
                }
            } else {
                stack.set(ChemistryDataComponents.REACT_TICKS.get(), left);
            }
        }
    }
}