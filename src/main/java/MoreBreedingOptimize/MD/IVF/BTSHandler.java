package MoreBreedingOptimize.MD.IVF;

import MoreBreedingOptimize.MainClass;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

@EventBusSubscriber(modid = MainClass.MODID)
public class BTSHandler {

    private static final Logger log = LoggerFactory.getLogger(BTSHandler.class);

    // 用 String 常量代替 ResourceLocation 对象，彻底避开构造函数访问问题
    private static final String BTS_REGISTRY_NAME = MainClass.MODID + ":bts";

    // 装血后的模型 ID，对应 bts.json 里 overrides 的 custom_model_data: 1 -> bts1
    private static final int MODEL_FILLED = 1;

    /**
     * 安全获取 BTS 物品实例，返回 null 表示还未注册
     * 注意：此方法仅从服务端事件调用，DeferredRegister 已就绪
     */
    private static Item getBTSItem() {
        return IVFITEM.BTS.get();
    }

    /**
     * 右键成年动物抽血 -> 获得带生物ID的 BTS 物品
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();

        // 必须手持玻璃瓶来抽血
        // 必须手持BTS物品来抽血
        if (!stack.is(getBTSItem())) {
            //log.info("[BTS] 抽血跳过：手持物品不是BTS，而是 {}");
            return;
        }
        if  (!(event.getTarget() instanceof LivingEntity mob)) {
            // @AI 诊断日志：目标是幼年动物或非 AgeableMob 时提示原因
            log.info("[BTS] 01",
                    BuiltInRegistries.ENTITY_TYPE.getKey(event.getTarget().getType()));
            return;
        }

        // 播放抽血音效
        level.playSound(null, mob.blockPosition(),
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.8F, 0.8F);

        // 获取生物ID，如 "minecraft:cow"
        String entityId = EntityType.getKey(mob.getType()).toString();

        // 安全获取 BTS 物品（服务端注册已完成，不会为空）
        Item btsItem = getBTSItem();
        if (btsItem != null) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            ItemStack bts = new ItemStack(btsItem);
            // 记录来源生物 ID（tooltip 显示用）
            bts.set(ModDataComponents.SOURCE_ENTITY.get(), entityId);
            // 设置 CustomModelData=1，触发 bts.json 的 overrides 切换到装血纹理 bts1
            bts.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(MODEL_FILLED));
            player.addItem(bts);
            log.info("[BTS] 抽血成功：{} -> 获得带 {} 来源的 BTS",
                    player.getName().getString(), entityId);
        }
        // 只有在成功执行了抽血逻辑时才取消事件
        if (btsItem != null) {
            event.setCanceled(true);
        }
    }

    /**
     * 鼠标悬停时，在 tooltip 显示 "来源: 牛" 这样的中文名
     */
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // 用注册名字符串对比，不直接 .get()，避免客户端 DeferredHolder 未就绪时崩溃
        // BuiltInRegistries.ITEM.getKey() 返回 ResourceLocation，toString() 得到 "namespace:path"
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemKey == null || !itemKey.toString().equals(BTS_REGISTRY_NAME)) return;

        // 读取 source_entity 组件
        Optional<String> entityId = Optional.ofNullable(
                stack.get(ModDataComponents.SOURCE_ENTITY.get())
        );

        if (entityId.isPresent()) {
            // 通过 EntityType 反查生物的显示名称
            EntityType<?> type = EntityType.byString(entityId.get()).orElse(null);
            if (type != null) {
                // 获取生物的翻译名（中文环境自动显示中文名）
                Component name = type.getDescription();
                event.getToolTip().add(
                        Component.literal("§7来源: ").append(name)
                );
            } else {
                // 兜底：直接显示原始ID
                event.getToolTip().add(
                        Component.literal("§7来源: " + entityId.get())
                );
            }
        } else {
            // 空瓶（无来源数据）时提示这是空样本，方便区分
            event.getToolTip().add(
                    Component.literal("§8空样本（右键成年动物采集）")
            );
        }
    }
}
