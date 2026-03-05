package moe.crosby.unionizedvillagers.impl.needs;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

/**
 * Villager need that ensures a villager is not trapped
 */
public class RoomNeed extends VillagerNeed {
    private static final double ONE_VOXEL = 1 / 16d;
    private static final double EPSILON = 1e-5f;
    private static final int AREA = 3 * 3;

    public RoomNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(World world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        double height = villagerEntity.getHeight();
        int bodyBlocks = MathHelper.ceil(height - 1);
        double headSize = MathHelper.clamp(height - bodyBlocks + ONE_VOXEL, 0, 1);
        double jumpHeight = 1.2d;
        double walkHeight = 0.5d;

        ShapeContext shapeContext = ShapeContext.of(villagerEntity);
        Set<BlockPos> visited = new ObjectOpenHashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(villagerEntity.getBlockPos());
        visited.add(villagerEntity.getBlockPos());
        int count = 0;

        while (!queue.isEmpty() && count < AREA) {
            BlockPos footPos = queue.poll();

            double footHeight = footHeight(world, shapeContext, footPos);

            if (count > 0) {
                // attempt to move down
                if (footHeight < EPSILON && headHeight(world, shapeContext, footPos.up()) > headSize) {
                    BlockPos newFootPos = footPos.down();
                    if (footHeight(world, shapeContext, newFootPos) < 1) {
                        footPos = newFootPos;
                    }
                // attempt to move up
                } else if (footHeight > walkHeight && footHeight < jumpHeight && headHeight(world, shapeContext, footPos.up(2)) > headSize) {
                    BlockPos newFootPos = footPos.up();
                    if (footHeight(world, shapeContext, newFootPos) < jumpHeight - 1) {
                        footPos = newFootPos;
                    }
                }
            }

            // check if the whole body fits
            if (!canBodyFit(world, footPos, bodyBlocks, shapeContext)) {
                continue;
            }

            // count block & increase queue
            count++;
            for (int i = 0; i < 4; i++) {
                Direction direction = Direction.fromHorizontal(i);
                BlockPos offsetPos = footPos.offset(direction);
                if (visited.add(offsetPos)) {
                    queue.add(offsetPos);
                }
            }
        }

        boolean isMet = count >= AREA;

        int finalCount = count;
        debug(villagerEntity, isMet, () -> "has " + finalCount + " blocks of free space out of " + AREA + " required");

        return isMet;
    }

    private static double footHeight(World world, ShapeContext shapeContext, BlockPos pos) {
        return world.getBlockState(pos).getCollisionShape(world, pos, shapeContext).getMax(Direction.Axis.Y);
    }

    private static double headHeight(World world, ShapeContext shapeContext, BlockPos pos) {
        return world.getBlockState(pos).getCollisionShape(world, pos, shapeContext).getMin(Direction.Axis.Y);
    }

    private static boolean canBodyFit(World world, BlockPos footPos, int body, ShapeContext shapeContext) {
        BlockPos headPos = footPos;
        for (int i = 0; i < body; i++) {
            headPos = headPos.up();
            if (!world.getBlockState(headPos).getCollisionShape(world, headPos, shapeContext).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
