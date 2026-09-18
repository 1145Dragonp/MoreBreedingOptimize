package MoreBreedingOptimize.ADV;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * 自定义成就触发器：种公！
 *
 * 当玩家第一次触发 PAMB 末地烛催生逻辑时，授予此成就。
 * 该触发器由 BreedAdvancementHandler 监听相同的 EntityInteract 事件并调用。
 */
public class BreedTrigger extends SimpleCriterionTrigger<BreedTrigger.Instance> {

    /**
     * 触发器 ID：morebo:breed_with_end_rod
     */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("morebo", "breed_with_end_rod");

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    /**
     * 触发成就。
     * 在检测到玩家使用末地烛催生动物时调用。
     *
     * @param player 触发事件的玩家
     */
    public void trigger(ServerPlayer player) {
        // @AI(BAPI.SimpleCriterionTrigger, trigger)
        this.trigger(player, instance -> true);
    }

    /**
     * 成就触发实例。
     * 本触发器无额外条件，只要 trigger() 被调用就授予成就。
     */
    public record Instance(Optional<ContextAwarePredicate> player)
            implements SimpleCriterionTrigger.SimpleInstance {

        /**
         * 序列化/反序列化 Codec
         */
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player)
                ).apply(instance, Instance::new)
        );
    }
}
