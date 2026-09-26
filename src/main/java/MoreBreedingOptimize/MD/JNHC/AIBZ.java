package MoreBreedingOptimize.MD.JNHC;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import MoreBreedingOptimize.Config;

public class AIBZ {

    // ==========================================
    // 配置区域
    // ==========================================

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String SYSTEM_PROMPT = "你是 主人的宾州，你有了自我意识 ，你是主人身体的一部分，请用使用者用的语言回复。";
    private static final double SPEECH_FREQ_TOKEN = 0.5;  // ----> 如果代替就删除
    private static final Random RANDOM = new Random();
    private static final Gson GSON = new Gson();

    // ==========================================
    // 逻辑区域
    // ==========================================

    private static int dialogueCooldown = 0;

    /**
     * 核心方法：传入 prompt，异步获取 AI 回复。
     * 保证不会以异常方式结束：网络失败 / 非200 / 解析失败时返回 null，并把具体原因写入日志。
     */
    public static CompletableFuture<String> getAIResponse(String prompt) {
        // 用 Gson 构造请求体，自动转义玩家消息里的引号、换行等特殊字符，避免 JSON 被破坏
        JsonObject body = new JsonObject();
        body.addProperty("model", Config.MODEL_NAME.get());

        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", SYSTEM_PROMPT);
        messages.add(systemMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);
        messages.add(userMsg);

        body.add("messages", messages);
        body.addProperty("temperature", 0.8);
        body.addProperty("max_tokens", 200);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.APIRUL.get()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Config.APIKEY.get())
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(AIBZ::parseReply)
                // 网络异常 / 超时 / 连接被拒等：记录原因并返回 null，由调用方显示兜底提示
                .exceptionally(ex -> {
                    LOGGER.error("[JNHC-AI] 请求 AI 接口失败：{}", ex.toString());
                    return null;
                });
    }

    /**
     * 从智谱 API 响应中提取回复文本。
     * 标准结构：choices[0].message.content
     */
    private static String parseReply(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            LOGGER.error("[JNHC-AI] API RC {}：{}", response.statusCode(), response.body());
            return null;
        }
        try {
            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                LOGGER.error("[JNHC-AI] 响应缺少 choices 字段：{}", response.body());
                return null;
            }
            JsonElement content = choices.get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content");
            if (content == null || content.isJsonNull()) {
                LOGGER.error("[JNHC-AI] 响应 message.content 为空：{}", response.body());
                return null;
            }
            return content.getAsString();
        } catch (Exception e) {
            LOGGER.error("[JNHC-AI] 解析 AI 回复失败，原始响应：{}", response.body(), e);
            return null;
        }
    }

    /**
     * 一键调用：传入玩家和提示词，AI回复自动显示在聊天栏
     */
    public static void showDialogue(Player player, String prompt) {
        getAIResponse(prompt).thenAccept(reply -> {
            if (reply != null && player.getServer() != null) {
                player.getServer().execute(() -> {
                    player.displayClientMessage(Component.literal(reply), false);
                });
            }
        });
    }

    /**
     * 给玩家显示一句随机台词（带冷却）
     */

    public static void showRandomDialogue(Player player) {
        if (dialogueCooldown <= 0) {
            String[] randomPrompts = {
                    "说一句话，假装你是一个正在喝可乐的游戏角色",
                    "说一句话，表达对力量的渴望",
                    "说一句话，抱怨一下可乐的味道",
                    "说一句话，假装自己是宾州"
            };

      String prompt = randomPrompts[RANDOM.nextInt(randomPrompts.length)];

            showDialogue(player, prompt);

            double token = Math.max(0.1, Config.MAX_TOKEN_LINIT.get());
            dialogueCooldown = (int)(80 + 80 * token * RANDOM.nextDouble());
        }
    }

    public static void tick() {
        if (dialogueCooldown > 0) {
            dialogueCooldown--;
        }
    }

    public static void showOpeningDialogue(Player player) {
        showDialogue(player, "你是 主人的宾州，你有了自我意识 ，你是主人身体的一部分，请用使用者用的语言回复。");
    }
}
