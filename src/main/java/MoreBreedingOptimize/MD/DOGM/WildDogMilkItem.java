package MoreBreedingOptimize.MD.DOGM;

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

public class WildDogMilkItem extends Item {

    public WildDogMilkItem() {
        super(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // @AI(BAPI.MobEffectInstance) 1.21.1 构造器要求 Holder<MobEffect>，用注册表包装
        Holder<MobEffect> eternalHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(DOGMITEM.ETERNAL.get());
        player.addEffect(new MobEffectInstance(
                eternalHolder,  // 传 Holder<MobEffect>
                2400,    // 120 秒
                0,
                false,   // 不是环境效果
                false,   // 显示粒子
                true     // 显示图标
        ));

        // 播放饮用音效
        player.playSound(net.minecraft.sounds.SoundEvents.GENERIC_DRINK, 1.0F, 1.0F);

        // 消耗物品


        //stack.shrink(1);
        return InteractionResultHolder.consume(stack);

        //return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}