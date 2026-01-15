package moe.crosby.unionizedvillagers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public final class WorldUtils {
    public static final Direction[] HORIZONTAL_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST};

    public static boolean isNavigable(World world, BlockPos pos) {
        WorldChunk c = world.getWorldChunk(pos);
        return c.getBlockState(pos).canPathfindThrough(world, pos, NavigationType.LAND) && c.getBlockState(pos.up()).canPathfindThrough(world, pos, NavigationType.LAND);
    }

    public static void offsetFrom(BlockPos.Mutable mutable, BlockPos origin, Direction direction) {
        mutable.set(
                origin.getX() + direction.getOffsetX(),
                origin.getY() + direction.getOffsetY(),
                origin.getZ() + direction.getOffsetZ()
        );
    }

    public static void offsetFrom(BlockPos.Mutable mutable, BlockPos origin, Direction direction, int distance) {
        mutable.set(
                origin.getX() + direction.getOffsetX() * distance,
                origin.getY() + direction.getOffsetY() * distance,
                origin.getZ() + direction.getOffsetZ() * distance
        );
    }

    public static boolean isLivable(World world, Entity entity) {
        int livingSpace = world.getGameRules().getInt(UnionizedVillagers.LIVING_SPACE);
        if (livingSpace <= 1) return true;

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int xMin = 0;
        int xMax = 0;
        int zMin = 0;
        int zMax = 0;

        int livableBlocks = 1;

        // Get room bounds
        for (var direction : WorldUtils.HORIZONTAL_DIRECTIONS) {
            int i = 0;
            WorldUtils.offsetFrom(mutable, entity.getBlockPos(), direction);
            while (WorldUtils.isNavigable(world, mutable)) {
                if (i + livableBlocks >= livingSpace) return true;
                WorldUtils.offsetFrom(mutable, entity.getBlockPos(), direction, ++i + 1);
            }

            switch (direction) {
                case WEST -> xMin = i;
                case EAST -> xMax = i;
                case NORTH -> zMin = i;
                case SOUTH -> zMax = i;
            }

            livableBlocks += i;
        }

        // Iterate through room
        for (int x = entity.getBlockX() - xMin; x <= entity.getBlockX() + xMax; x++) {
            if (x == entity.getBlockX()) continue;
            mutable.setX(x);
            for (int z = entity.getBlockZ() - zMin; z <= entity.getBlockZ() + zMax; z++) {
                if (z == entity.getBlockZ()) continue;
                mutable.setZ(z);

                if (WorldUtils.isNavigable(world, mutable) && ++livableBlocks >= livingSpace) return true;
            }
        }
        return false;
    }
}
