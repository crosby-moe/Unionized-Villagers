package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import moe.crosby.unionizedvillagers.impl.fast.CachingRaycastFunction;
import moe.crosby.unionizedvillagers.impl.fast.ChunkAwareBlockSweeper;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

/**
 * Villager need that ensures there are no visible hazards nearby
 */
public class NoHazardNearbyNeed extends VillagerNeed {
    private static final int SEARCH_RADIUS = 8;

    public NoHazardNearbyNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        BlockPos origin = Villager.blockPosition();
        int minX = origin.getX() - SEARCH_RADIUS;
        int minY = origin.getY() - SEARCH_RADIUS;
        int minZ = origin.getZ() - SEARCH_RADIUS;
        int maxX = origin.getX() + SEARCH_RADIUS;
        int maxY = origin.getY() + SEARCH_RADIUS;
        int maxZ = origin.getZ() + SEARCH_RADIUS;

        for (ChunkAwareBlockSweeper it = new ChunkAwareBlockSweeper(world, minX, maxX, minY, maxY, minZ, maxZ); it.hasNext(); ) {
            ChunkAwareBlockSweeper.Entry entry = it.next();

            if (entry.getState().is(UnionizedVillagers.HAZARDS_TAG) && canSee(Villager, entry.getPos())) {
                debug(Villager, false, () -> "hazard is at " + entry.getPos());
                return false;
            }
        }

        debug(Villager, true, null);
        return true;
    }

    private boolean canSee(Villager Villager, BlockPos blockPos) {
        Vec3 startPos = new Vec3(Villager.getX(), Villager.getEyeY(), Villager.getZ());
        Vec3 endPos = Vec3.atCenterOf(blockPos);

        ClipContext context = new ClipContext(
            startPos,
            endPos,
            ClipContext.Block.VISUAL,
            ClipContext.Fluid.ANY,
            Villager
        );

        BlockHitResult result = CachingRaycastFunction.Visual.raycast(Villager.level(), context);

        return result.getType() == BlockHitResult.Type.MISS || result.getBlockPos().equals(blockPos);
    }
}
