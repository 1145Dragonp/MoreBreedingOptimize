package mrmd.morebreedingoptimize.MD.growthhormone;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrowthHormoneItem extends Item {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrowthHormoneItem.class);

    public GrowthHormoneItem() {
        super(new Properties().rarity(Rarity.EPIC).stacksTo(64));
    }
    /*
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                  LivingEntity target, InteractionHand hand) {
        // 先打印日志，验证右键是否触发
        LOGGER.info("[生长激素] 玩家 {} 右键点击了生物: {}", player.getName().getString(), target.getType().getDescriptionId());

        // 判断是否为幼年可成长生物
        if (target instanceof AgeableMob mob && mob.isBaby()) {
            LOGGER.info("[生长激素] 目标 {} 是幼年体，准备执行加速生长逻辑", target.getType().getDescriptionId());

            // TODO: 调用 GrowthHormoneHandler 处理核心加速逻辑
            // GrowthHormoneHandler.applyGrowth(mob, stack, player);

            return InteractionResult.SUCCESS;
        }

        LOGGER.info("[生长激素] 目标不是幼年体，不执行加速");
        return InteractionResult.PASS;
    }

     */
}