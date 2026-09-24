package MoreBreedingOptimize.MD.growthhormone;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = "morebo")
public class GrowthHormoneHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrowthHormoneHandler.class);

    /** 目标剩余时间：5秒 = 100游戏刻 */
    private static final int TARGET_REMAINING_TICKS = 100;

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(GrowthHormoneItems.GH.get())) {
            return;
        }

        if (!(event.getTarget() instanceof AgeableMob mob)) {
            return;
        }

        // 必须真的是幼年
        if (!mob.isBaby() || mob.getAge() >= 0) {
            LOGGER.info("[GH] 目标不是幼年生物，跳过。age={}", mob.getAge());
            return;
        }

        Level level = event.getLevel();
        if (level.isClientSide()) return;

        int currentAge = mob.getAge();       // 负数，如 -24000
        int remainingTicks = -currentAge;    // 正数，如 24000

        LOGGER.info("[GH] 处理前 age={}, 剩余={}刻({}秒)",
                currentAge, remainingTicks, remainingTicks / 20.0);

        // 核心逻辑：直接 setAge，不用 ageUp
        if (remainingTicks <= TARGET_REMAINING_TICKS) {
            mob.setAge(0);                   // 不足5秒，直接成年
        } else {
            mob.setAge(-TARGET_REMAINING_TICKS);  // 设成 -100，剩5秒成年
        }

        LOGGER.info("[GH] 处理后 age={}", mob.getAge());

        ServerPlayer player = (ServerPlayer) event.getEntity();

        level.playSound(null, mob.blockPosition(),
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.8F, 1.2F);

        ((ServerLevel) level).sendParticles(player, ParticleTypes.HAPPY_VILLAGER, true,
                mob.getRandomX(0.6), mob.getY(1.0), mob.getRandomZ(0.6),
                8, 0.0, 0.2, 0.0, 0.05);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        event.setCanceled(true);
    }
}