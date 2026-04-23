package moe.crosby.unionizedvillagers.impl.needs;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

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
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        int requiredSize = world.getGameRules().get(UnionizedVillagers.ROOM_SIZE);

        CollisionContext shapeContext = CollisionContext.of(Villager);
        Set<BlockPos> visited = new ObjectOpenHashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(Villager.blockPosition());
        visited.add(Villager.blockPosition());
        int count = 0;

        while (!queue.isEmpty() && count < requiredSize) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            BlockState up = world.getBlockState(pos.above());

            // needs a floor
            if (world.getBlockState(pos.below()).getCollisionShape(world, pos, shapeContext).max(Direction.Axis.Y) < 1) {
                continue;
            }

            // door
            if ((isOpenableDoor(state) && isOpenableDoor(up))
                // can fit in
            || (state.getCollisionShape(world, pos, shapeContext).max(Direction.Axis.Y) <= ONE_VOXEL && up.getCollisionShape(world, pos.above(), shapeContext).isEmpty())) {

                // count block & increase queue
                count++;
                for (int i = 0; i < 4; i++) {
                    Direction direction = Direction.from2DDataValue(i);
                    BlockPos offsetPos = pos.relative(direction);
                    if (visited.add(offsetPos)) {
                        queue.add(offsetPos);
                    }
                }
            }
        }

        boolean isMet = count >= requiredSize;

        int finalCount = count;
        debug(Villager, isMet, () -> "has " + finalCount + " blocks of free space out of " + requiredSize + " required");

        return isMet;
    }

    private static boolean isOpenableDoor(BlockState state) {
        return state.getBlock() instanceof DoorBlock door && door.type().canOpenByHand();
    }
}
