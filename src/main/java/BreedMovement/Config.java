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

    public static final ModConfigSpec.DoubleValue PLAYFORENDROD = BUILDER
            .comment("玩家自慰掉落物概率比")
            .defineInRange("pfed", 0.5, 0.0, 10.0);


    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));






    }
}
