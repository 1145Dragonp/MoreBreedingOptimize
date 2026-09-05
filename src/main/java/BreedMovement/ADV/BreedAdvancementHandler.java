package BreedMovement.ADV;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = "breedmovement")
public class BreedAdvancementHandler {
    private static final Logger log = LoggerFactory.getLogger(BreedAdvancementHandler.class);

    // 记录已授予过成就的玩家 UUID，避免对同一玩家反复触发指令导致控制台刷报错
    // 使用 WeakHashMap 作为底层，UUID 没有外部强引用时可被 GC 回收
    private static final Set<UUID> grantedPlayers = Collections.newSetFromMap(new WeakHashMap<>());

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();

        // 基础条件过滤
        if (!(target instanceof Animal animal)) return;
        if (animal.isBaby()) return;
        if (animal.getPersistentData().getBoolean("breedmovement:neutered")) return;
        if (event.getLevel().isClientSide) return;

        // 检查手持物品是否为末地烛
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.is(Items.END_ROD)) return;

        // 同一玩家只授予一次，防止指令反复报错
        UUID playerId = player.getUUID();
        if (grantedPlayers.contains(playerId)) {
            return;
        }
        grantedPlayers.add(playerId);

        // 直接调用原版指令授予进度，无需任何复杂的判断
        if (player.getServer() != null) {
            player.getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "advancement grant @s only breedmovement:breed_with_end_rod"
            );
        }

        log.info("[BreedMovement] Advancement '种公！' granted via command to player {}", player.getName().getString());
    }
}