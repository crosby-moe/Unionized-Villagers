package moe.crosby.unionizedvillagers.impl.fast;

import moe.crosby.unionizedvillagers.impl.mixin.ClipContextAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.function.BiFunction;

/**
 * Caching version of {@link Level#clip(ClipContext)} for the purposes of optimizing {@link net.minecraft.world.entity.LivingEntity#hasLineOfSight(Entity)}.
 * Heavily inspired by lithium @ LGPLv3 ty 2no2name ily <3
 * @author Crosby
 */
public sealed abstract class CachingRaycastFunction implements BiFunction<ClipContext, BlockPos, BlockHitResult> {
    @SuppressWarnings("DataFlowIssue")
    private static final BlockHitResult MISS = BlockHitResult.miss(null, null, null);

    protected final Level world;
    protected final CollisionContext shapeContext;

    private int chunkX = Integer.MIN_VALUE;
    private int chunkZ = Integer.MAX_VALUE;
    private ChunkAccess chunk = null;

    private CachingRaycastFunction(Level world, ClipContext raycastContext) {
        this.world = world;
        this.shapeContext = ((ClipContextAccessor) raycastContext).unionized$getCollisionContext();
    }

    protected BlockState getBlock(BlockPos blockPos) {
        int chunkX = SectionPos.blockToSectionCoord(blockPos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(blockPos.getZ());

        // Avoid calling into the chunk manager as much as possible through managing chunks locally
        if (this.chunkX != chunkX || this.chunkZ != chunkZ) {
            this.chunk = this.world.getChunk(chunkX, chunkZ);

            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        final ChunkAccess chunk = this.chunk;

        // If the chunk is missing or out of bounds, assume that it is air
        if (chunk != null) {
            // We operate directly on chunk sections to avoid interacting with BlockPos and to squeeze out as much
            // performance as possible here
            LevelChunkSection section = chunk.getSections()[chunk.getSectionIndex(blockPos.getY())];

            // If the section doesn't exist or is empty, assume that the block is air
            if (section != null && !section.hasOnlyAir()) {
                return section.getBlockState(blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15);
            }
        }

        return Blocks.AIR.defaultBlockState();
    }


    /**
     * Simplified raycast function using {@link net.minecraft.world.level.ClipContext.Block#COLLIDER} and
     * {@link net.minecraft.world.level.ClipContext.Fluid#NONE}.
     */
    public static final class Collision extends CachingRaycastFunction {
        private Collision(Level world, ClipContext raycastContext) {
            super(world, raycastContext);
        }

        public static BlockHitResult raycast(Level world, ClipContext context) {
            return BlockGetter.traverseBlocks(context.getFrom(), context.getTo(), context, new Collision(world, context), ctx -> MISS);
        }

        @Override
        public BlockHitResult apply(ClipContext context, BlockPos blockPos) {
            BlockState state = this.getBlock(blockPos);
            VoxelShape shape = state.getCollisionShape(this.world, blockPos, this.shapeContext);
            return shape.clip(context.getFrom(), context.getTo(), blockPos);
        }
    }

    /**
     * Simplified raycast function using {@link net.minecraft.world.level.ClipContext.Block#VISUAL} and
     * {@link net.minecraft.world.level.ClipContext.Fluid#ANY}.
     */
    public static final class Visual extends CachingRaycastFunction {
        private Visual(Level world, ClipContext raycastContext) {
            super(world, raycastContext);
        }

        public static BlockHitResult raycast(Level world, ClipContext context) {
            return BlockGetter.traverseBlocks(context.getFrom(), context.getTo(), context, new Visual(world, context), ctx -> MISS);
        }

        @Override
        public BlockHitResult apply(ClipContext context, BlockPos blockPos) {
            BlockState state = this.getBlock(blockPos);

            BlockHitResult block = state.getVisualShape(this.world, blockPos, this.shapeContext)
                .clip(context.getFrom(), context.getTo(), blockPos);

            BlockHitResult fluid = state.getFluidState().getShape(this.world, blockPos)
                .clip(context.getFrom(), context.getTo(), blockPos);

            return block == null ? fluid : block;
        }
    }
}
