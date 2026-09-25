package MoreBreedingOptimize.MD.JNHC;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class JNHCItemUse extends Item {

    public JNHCItemUse() {  // ← 这里改成 JNHCItemUse，和类名一致
        super(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 获取宾洲强化药水效果
        Holder<MobEffect> bzqhHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(JNHCITEM.BZSTRONGER.get());
        player.addEffect(new MobEffectInstance(
                bzqhHolder,
                2400,
                0,
                false,
                false,
                true
        ));

        // 播放饮用音效
        player.playSound(net.minecraft.sounds.SoundEvents.GENERIC_DRINK, 1.0F, 1.0F);

        // 消耗物品（创造模式不消耗，生存模式消耗1个）
        return InteractionResultHolder.consume(stack);
    }
}