package BreedMovement;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {

    // 1. 创建一个 Builder
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 2. 定义你的“致命爱情”专属配置项
    // 注意：这里只定义，千万不要在这里调用 .get()，否则会导致游戏崩溃！

    public static final ModConfigSpec.DoubleValue STRENGTH = BUILDER
            .comment("推力力度 (建议范围: 0.1 ~ 2.0)")
            .defineInRange("thrustStrength", 0.3, 0.1, 5.0);

    public static final ModConfigSpec.DoubleValue DROP_CHANCE = BUILDER
            .comment("繁殖过程中掉落水桶/牛奶桶的概率 (0.0 ~ 1.0)")
            .defineInRange("dropChance", 0.05, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue DIE_CHANCE = BUILDER
            .comment("繁殖时暴毙并掉落末地烛的概率 (0.0 ~ 1.0)")
            .defineInRange("fatalDieChance", 0.005, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue MAX_LOVE_DISTANCE = BUILDER
            .comment("检擦距离")
            .defineInRange("detectionrange", 2.0, 0.0, 20.0);

    // 3. 构建最终的配置规范
    // ===== 跨物种繁殖 =====
    public static final ModConfigSpec.DoubleValue CROSSBREED_CHANCE = BUILDER
            .comment("跨物种繁殖触发概率 (0.0 ~ 1.0)")
            .defineInRange("crossbreedChance", 0.5, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue CROSSBREED_DROP_CHANCE = BUILDER
            .comment("跨物种繁殖掉落金苹果的概率 (0.0 ~ 1.0)")
            .defineInRange("crossbreedDropChance", 0.25, 0.0, 1.0);

    // ===== 村民繁殖效果 =====
    public static final ModConfigSpec.DoubleValue VILLAGER_LOVE_CHANCE = BUILDER
            .comment("村民繁殖后触发爱情效果的概率 (0.0 ~ 1.0)")
            .defineInRange("villagerLoveChance", 0.6, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue VILLAGER_EXPLOSION_CHANCE = BUILDER
            .comment("村民爱情爆炸概率 (0.0 ~ 1.0)")
            .defineInRange("villagerExplosionChance", 0.3, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue VILLAGER_EXPLOSION_RADIUS = BUILDER
            .comment("村民爱情爆炸半径 (建议 1.0 ~ 5.0)")
            .defineInRange("villagerExplosionRadius", 2.0, 0.5, 10.0);

    // ===== 玩家感染效果 =====
    public static final ModConfigSpec.DoubleValue PLAYER_INFECTION_CHANCE = BUILDER
            .comment("玩家被爱情感染的概率 (0.0 ~ 1.0)")
            .defineInRange("playerInfectionChance", 0.4, 0.0, 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));






    }
}
