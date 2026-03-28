package moe.crosby.unionizedvillagers.impl.fast;

import moe.crosby.unionizedvillagers.impl.mixin.RaycastContextAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import java.util.function.BiFunction;

/**
 * Caching version of {@link World#raycast(RaycastContext)} for the purposes of optimizing {@link net.minecraft.entity.LivingEntity#canSee(Entity)}.
 * Heavily inspired by lithium @ LGPLv3 ty 2no2name ily <3
 * @author Crosby
 */
public sealed abstract class CachingRaycastFunction implements BiFunction<RaycastContext, BlockPos, BlockHitResult> {
    private static final BlockHitResult MISS = BlockHitResult.createMissed(null, null, null);

    protected final World world;
    protected final ShapeContext shapeContext;

    private int chunkX = Integer.MIN_VALUE;
    private int chunkZ = Integer.MAX_VALUE;
    private Chunk chunk = null;

    private CachingRaycastFunction(World world, RaycastContext raycastContext) {
        this.world = world;
        this.shapeContext = ((RaycastContextAccessor) raycastContext).unionized$getShapeContext();
    }

    protected BlockState getBlock(BlockPos blockPos) {
        int chunkX = ChunkSectionPos.getSectionCoord(blockPos.getX());
        int chunkZ = ChunkSectionPos.getSectionCoord(blockPos.getZ());

        // Avoid calling into the chunk manager as much as possible through managing chunks locally
        if (this.chunkX != chunkX || this.chunkZ != chunkZ) {
            this.chunk = this.world.getChunk(chunkX, chunkZ);

            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        final Chunk chunk = this.chunk;

        // If the chunk is missing or out of bounds, assume that it is air
        if (chunk != null) {
            // We operate directly on chunk sections to avoid interacting with BlockPos and to squeeze out as much
            // performance as possible here
            ChunkSection section = chunk.getSectionArray()[chunk.getSectionIndex(blockPos.getY())];

            // If the section doesn't exist or is empty, assume that the block is air
            if (section != null && !section.isEmpty()) {
                return section.getBlockState(blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15);
            }
        }

        return Blocks.AIR.getDefaultState();
    }


    /**
     * Simplified raycast function using {@link net.minecraft.world.RaycastContext.ShapeType#COLLIDER} and
     * {@link net.minecraft.world.RaycastContext.FluidHandling#NONE}.
     */
    public static final class Collision extends CachingRaycastFunction {
        private Collision(World world, RaycastContext raycastContext) {
            super(world, raycastContext);
        }

        public static BlockHitResult raycast(World world, RaycastContext context) {
            return BlockView.raycast(context.getStart(), context.getEnd(), context, new Collision(world, context), ctx -> MISS);
        }

        @Override
        public BlockHitResult apply(RaycastContext context, BlockPos blockPos) {
            BlockState state = this.getBlock(blockPos);
            VoxelShape shape = state.getCollisionShape(this.world, blockPos, this.shapeContext);
            return shape.raycast(context.getStart(), context.getEnd(), blockPos);
        }
    }

    /**
     * Simplified raycast function using {@link net.minecraft.world.RaycastContext.ShapeType#VISUAL} and
     * {@link net.minecraft.world.RaycastContext.FluidHandling#ANY}.
     */
    public static final class Visual extends CachingRaycastFunction {
        private Visual(World world, RaycastContext raycastContext) {
            super(world, raycastContext);
        }

        public static BlockHitResult raycast(World world, RaycastContext context) {
            return BlockView.raycast(context.getStart(), context.getEnd(), context, new Visual(world, context), ctx -> MISS);
        }

        @Override
        public BlockHitResult apply(RaycastContext context, BlockPos blockPos) {
            BlockState state = this.getBlock(blockPos);

            BlockHitResult block = state.getCameraCollisionShape(this.world, blockPos, this.shapeContext)
                .raycast(context.getStart(), context.getEnd(), blockPos);

            BlockHitResult fluid = state.getFluidState().getShape(this.world, blockPos)
                .raycast(context.getStart(), context.getEnd(), blockPos);

            return block == null ? fluid : block;
        }
    }
}
