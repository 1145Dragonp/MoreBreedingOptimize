package MoreBreedingOptimize.MD.ChemistrySynthesis;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * @AI 试管内含物品的不可变包装。
 * 用于解决 ItemStack 直接作为组件值导致的崩溃问题。
 */
public record ContainedItem(ItemStack stack) {

    // 编解码器
    public static final Codec<ContainedItem> CODEC = ItemStack.CODEC.xmap(ContainedItem::new, ContainedItem::stack);

    // 网络同步编解码器
    // @AI 修复：ItemStack.STREAM_CODEC 的缓冲区类型是 RegistryFriendlyByteBuf，
    // map(...) 的返回类型也随之是 StreamCodec<RegistryFriendlyByteBuf, ContainedItem>，
    // 声明成 StreamCodec<ByteBuf, ...> 会因泛型不协变而编译失败
    public static final StreamCodec<RegistryFriendlyByteBuf, ContainedItem> STREAM_CODEC =
            ItemStack.STREAM_CODEC.map(ContainedItem::new, ContainedItem::stack);

    public ContainedItem {
        // 防御性拷贝，保证组件值不可变
        stack = stack.copy();
    }
}