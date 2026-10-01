package mrmd.morebreedingoptimize.MD.chemistrysynthesis;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 陵寝神殿程序化搭建器。
 * 以 origin 为西南角地板基准，生成一座 11x9 的对称小殿：
 * 深板岩砖地板 + 四角石柱 + 中央石英献祭台 + 石椁（白床+末地烛）。
 */
public class TempleBuilder {

    public static void build(Level level, BlockPos origin) {
        BlockState floor = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState pillar = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        BlockState altar = Blocks.CALCITE.defaultBlockState();
        BlockState slab = Blocks.DEEPSLATE_BRICK_SLAB.defaultBlockState();

        // 1. 地板 11(x) x 9(z)
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                level.setBlock(origin.offset(dx, 0, dz), floor, 3);
            }
        }

        // 2. 四角石柱（高 4）
        for (int[] corner : new int[][]{{-5, -4}, {5, -4}, {-5, 4}, {5, 4}}) {
            for (int dy = 1; dy <= 4; dy++) {
                level.setBlock(origin.offset(corner[0], dy, corner[1]), pillar, 3);
            }
        }

        // 3. 中央献祭台 3x3（y=1）
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(origin.offset(dx, 1, dz), altar, 3);
            }
        }

        // 4. 石椁：中央白色羊毛 + 两侧石英台阶（代表沉睡带翼女子）
        level.setBlock(origin.offset(0, 2, 0), Blocks.WHITE_WOOL.defaultBlockState(), 3);
        level.setBlock(origin.offset(0, 3, 0), Blocks.WHITE_CARPET.defaultBlockState(), 3);
        level.setBlock(origin.offset(-1, 2, 0), Blocks.QUARTZ_STAIRS.defaultBlockState(), 3);
        level.setBlock(origin.offset(1, 2, 0), Blocks.QUARTZ_STAIRS.defaultBlockState(), 3);

        // 5. 椁前末地烛（香火）
        level.setBlock(origin.offset(0, 1, 2), Blocks.SOUL_LANTERN.defaultBlockState(), 3);

        // 6. 屋顶：y=5 四周 slabs（中间留空）
        for (int dx = -5; dx <= 5; dx++) {
            level.setBlock(origin.offset(dx, 5, -4), slab, 3);
            level.setBlock(origin.offset(dx, 5, 4), slab, 3);
        }
        for (int dz = -3; dz <= 3; dz++) {
            level.setBlock(origin.offset(-5, 5, dz), slab, 3);
            level.setBlock(origin.offset(5, 5, dz), slab, 3);
        }

        // 7. 入口台阶（南侧 dz=+5 外）
        level.setBlock(origin.offset(0, 0, 5), Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState(), 3);
    }
}