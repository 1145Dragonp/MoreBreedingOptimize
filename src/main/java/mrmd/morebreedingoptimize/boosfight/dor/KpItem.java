package mrmd.morebreedingoptimize.boosfight.dor;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class KpItem extends Item {
    public KpItem(Properties props) { super(props); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new KpScreen());
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}