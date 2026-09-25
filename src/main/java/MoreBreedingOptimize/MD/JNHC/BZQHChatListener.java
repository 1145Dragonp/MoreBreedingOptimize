package MoreBreedingOptimize.MD.JNHC;

import MoreBreedingOptimize.MainClass;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = MainClass.MODID, bus = EventBusSubscriber.Bus.GAME)
public class BZQHChatListener {

    private static final String PREFIX = "@滨州";

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        String originalMessage = event.getMessage().getString();
        Player player = event.getPlayer();
        String playerName = player.getName().getString();

        // 1. 检查消息是否以 @滨州 开头
        if (!originalMessage.startsWith(PREFIX)) {
            return; // 不是指令，直接放行
        }

        // 2. 检查玩家是否拥有 buff，没有 buff 直接放行
        var effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(JNHCITEM.BZSTRONGER.get());
        if (!player.hasEffect(effectHolder)) {
            return;
        }

        // 3. 提取指令正文
        String content = originalMessage.substring(PREFIX.length()).trim();
        /*
        if (content.isEmpty()) {
            event.setCanceled(true);
            player.displayClientMessage(Component.literal("§7请输入你想对宾州说的话，例如：@滨州 今天吃什么"), false);
            return;


         */
        // 【核心修复】取消原消息，并用“模拟格式”重新发送，防止输入消失
        event.setCanceled(true);

        // 在控制台打印原始指令（方便你调试查看）


        if (player.getServer() != null) {
            player.getServer().execute(() -> {
                // 模拟真实玩家发送消息：灰色 <玩家名> 内容
                Component simulatedMsg = Component.literal("<" + playerName + ">" + content );
                player.sendSystemMessage(simulatedMsg);
            });
        }

        // 4. 异步调用 AI
        CompletableFuture<String> aiFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return AIBZ.getAIResponse("你是宾州，玩家对你说：" + content).get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });

        // 5. AI 返回结果，去掉了 [宾州] 标签
        aiFuture.whenComplete((reply, throwable) -> {
            if (player.getServer() != null) {
                player.getServer().execute(() -> {
                    if (reply != null) {
                        // 直接发送 AI 回复文本，不再加任何标签
                        player.sendSystemMessage(Component.literal("<滨州>" + reply));
                    }
                });
            }
        });
    }
}