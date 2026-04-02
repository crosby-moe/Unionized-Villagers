package moe.crosby.unionizedvillagers.impl.needs;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

/**
 * Villager need that ensures a villager is not trapped
 */
public class RoomNeed extends VillagerNeed {
    private static final double ONE_VOXEL = 1 / 16d;

    public RoomNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerWorld world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        int requiredSize = world.getGameRules().getValue(UnionizedVillagers.ROOM_SIZE);

        ShapeContext shapeContext = ShapeContext.of(villagerEntity);
        Set<BlockPos> visited = new ObjectOpenHashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(villagerEntity.getBlockPos());
        visited.add(villagerEntity.getBlockPos());
        int count = 0;

        while (!queue.isEmpty() && count < requiredSize) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            BlockState up = world.getBlockState(pos.up());

            // needs a floor
            if (world.getBlockState(pos.down()).getCollisionShape(world, pos, shapeContext).getMax(Direction.Axis.Y) < 1) {
                continue;
            }

            // door
            if ((isOpenableDoor(state) && isOpenableDoor(up))
                // can fit in
            || (state.getCollisionShape(world, pos, shapeContext).getMax(Direction.Axis.Y) <= ONE_VOXEL && up.getCollisionShape(world, pos.up(), shapeContext).isEmpty())) {

                // count block & increase queue
                count++;
                for (int i = 0; i < 4; i++) {
                    Direction direction = Direction.fromHorizontalQuarterTurns(i);
                    BlockPos offsetPos = pos.offset(direction);
                    if (visited.add(offsetPos)) {
                        queue.add(offsetPos);
                    }
                }
            }
        }

        boolean isMet = count >= requiredSize;

        int finalCount = count;
        debug(villagerEntity, isMet, () -> "has " + finalCount + " blocks of free space out of " + requiredSize + " required");

        return isMet;
    }

    private static boolean isOpenableDoor(BlockState state) {
        return state.getBlock() instanceof DoorBlock door && door.getBlockSetType().canOpenByHand();
    }
}
