package BreedMovement.PAMB.PFER;

import BreedMovement.MainClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import BreedMovement.Config;

@EventBusSubscriber(modid = MainClass.MODID)
public class playerforendrod {

    // 记录每个玩家专属数据的容器
    private static final Map<UUID, SneakData> PLAYER_DATA = new HashMap<>();

    // 内部数据类：记录潜行频率和掉落冷却
    private static class SneakData {
        long lastSneakTime = 0;  // 上一次按下潜行的时间戳
        int sneakCount = 0;      // 1秒内的潜行次数
        long lastDropTime = 0;   // 上一次成功掉落物品的时间戳
    }

    // 监听玩家Tick事件（NeoForge 1.21 新写法）
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        // 只在服务端执行，防止客户端和服务端同时触发导致双倍掉落
        if (event.getEntity().level().isClientSide) {
            return;
        }

        Player player = event.getEntity();
        Level level = player.level();
        UUID uuid = player.getUUID();

        // 获取或创建当前玩家的数据
        SneakData data = PLAYER_DATA.computeIfAbsent(uuid, k -> new SneakData());

        // 检查玩家脚下是不是末地烛
        BlockPos belowPos = player.blockPosition().below();
        if (!level.getBlockState(belowPos).is(Blocks.END_ROD)) {
            return; // 不是末地烛，直接结束
        }

        // 检查玩家是否在潜行
        if (player.isCrouching()) {
            long currentTime = System.currentTimeMillis();

            // 判断是否在1秒内（1000毫秒）
            if (currentTime - data.lastSneakTime <= 1000) {
                data.sneakCount++;
            } else {
                // 超过1秒了，重置计数
                data.sneakCount = 1;
            }
            // 更新最后一次潜行的时间
            data.lastSneakTime = currentTime;

            // 检查是否满足掉落条件：1秒内按了3次以上，且距离上次掉落超过5秒（5000毫秒）
            if (data.sneakCount >= 3 && currentTime - data.lastDropTime >= 5000) {
                // 更新掉落时间
                data.lastDropTime = currentTime;
                // 重置潜行计数
                data.sneakCount = 0;

                // 执行掉落逻辑
                dropRandomItem(player, level);
            }
        }
    }

    // 随机掉落并设置拾取延迟（防刷机制）
    private static void dropRandomItem(Player player, Level level) {
        // 随机决定掉落牛奶桶还是水桶
        ItemStack dropItem = Math.random() > Config.PLAYFORENDROD.get()
                ? new ItemStack(Items.MILK_BUCKET)
                : new ItemStack(Items.WATER_BUCKET);

        // 使用最基础的构造方法生成物品实体
        ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), dropItem);

        // 核心防刷机制：通过 Java 反射强行修改私有字段 pickupDelay
        try {
            // 1. 获取 ItemEntity 类中的 pickupDelay 字段
            java.lang.reflect.Field field = ItemEntity.class.getDeclaredField("pickupDelay");
            // 2. 取消访问权限检查（暴力破解 private）
            field.setAccessible(true);
            // 3. 将当前物品的 pickupDelay 设置为 40 Tick (2秒)
            field.setInt(itemEntity, 40);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 如果反射失败，打印错误日志，防止游戏崩溃
            e.printStackTrace();
        }

        // 将物品生成在世界中
        level.addFreshEntity(itemEntity);
    }
}