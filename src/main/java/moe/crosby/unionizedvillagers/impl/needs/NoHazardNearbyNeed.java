package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import moe.crosby.unionizedvillagers.impl.fast.CachingRaycastFunction;
import moe.crosby.unionizedvillagers.impl.fast.ChunkAwareBlockSweeper;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * Villager need that ensures there are no visible hazards nearby
 */
public class NoHazardNearbyNeed extends VillagerNeed {
    private static final int SEARCH_RADIUS = 8;

    public NoHazardNearbyNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerWorld world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        BlockPos origin = villagerEntity.getBlockPos();
        int minX = origin.getX() - SEARCH_RADIUS;
        int minY = origin.getY() - SEARCH_RADIUS;
        int minZ = origin.getZ() - SEARCH_RADIUS;
        int maxX = origin.getX() + SEARCH_RADIUS;
        int maxY = origin.getY() + SEARCH_RADIUS;
        int maxZ = origin.getZ() + SEARCH_RADIUS;

        for (ChunkAwareBlockSweeper it = new ChunkAwareBlockSweeper(world, minX, maxX, minY, maxY, minZ, maxZ); it.hasNext(); ) {
            ChunkAwareBlockSweeper.Entry entry = it.next();

            if (entry.getState().isIn(UnionizedVillagers.HAZARDS_TAG) && canSee(villagerEntity, entry.getPos())) {
                debug(villagerEntity, false, () -> "hazard is at " + entry.getPos());
                return false;
            }
        }

        debug(villagerEntity, true, null);
        return true;
    }

    private boolean canSee(VillagerEntity villagerEntity, BlockPos blockPos) {
        Vec3d startPos = new Vec3d(villagerEntity.getX(), villagerEntity.getEyeY(), villagerEntity.getZ());
        Vec3d endPos = Vec3d.ofCenter(blockPos);

        RaycastContext context = new RaycastContext(
            startPos,
            endPos,
            RaycastContext.ShapeType.VISUAL,
            RaycastContext.FluidHandling.ANY,
            villagerEntity
        );

        BlockHitResult result = CachingRaycastFunction.Visual.raycast(villagerEntity.getEntityWorld(), context);

        return result.getType() == HitResult.Type.MISS || result.getBlockPos().equals(blockPos);
    }
}
