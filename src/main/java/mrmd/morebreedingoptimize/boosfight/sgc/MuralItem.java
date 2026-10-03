package mrmd.morebreedingoptimize.boosfight.sgc;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.decoration.Painting;

public class MuralItem extends Item {
    private final String variantName;

    public MuralItem(Properties props, String variantName) {
        super(props);
        this.variantName = variantName;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction dir = ctx.getClickedFace();

        if (dir == Direction.DOWN || dir == Direction.UP) return InteractionResult.FAIL;

        BlockPos placePos = pos.relative(dir);

        RegistryAccess reg = level.registryAccess();
        PaintingVariant variant = reg.registryOrThrow(Registries.PAINTING_VARIANT)
                .get(ResourceLocation.fromNamespaceAndPath("morebo", variantName));
        if (variant == null) return InteractionResult.FAIL;

        Holder<PaintingVariant> holder = Holder.direct(variant);
        Painting painting = new Painting(level, placePos, dir, holder);

        AABB box = painting.getBoundingBox();
        boolean space = level.noCollision(null, box);

        if (space) {
            if (!level.isClientSide) {
                level.addFreshEntity(painting);
                ctx.getItemInHand().shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.FAIL;
    }
}
