package moe.crosby.unionizedvillagers.impl.fast;

import com.google.common.collect.AbstractIterator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/**
 * Taken from lithium @ <a href="https://github.com/CaffeineMC/lithium/blob/develop/common/src/main/java/net/caffeinemc/mods/lithium/common/entity/movement/ChunkAwareBlockCollisionSweeper.java">ChunkAwareBlockCollisionSweeper</a> & <a href="https://github.com/CaffeineMC/lithium/blob/develop/common/src/main/java/net/caffeinemc/mods/lithium/common/entity/movement/ChunkAwareBlockCollisionSweeperBlockPos.java">ChunkAwareBlockCollisionSweeperBlockPos</a>
 * Modified to completely remove block collision handling, just optimized chunk aware block sweeping.
 */
public class ChunkAwareBlockSweeper extends AbstractIterator<ChunkAwareBlockSweeper.Entry> {
    protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    protected final Entry entry = new Entry(this.pos);

    protected final Level world;

    //limits of the area without extension for oversized blocks
    protected final int minX, minY, minZ, maxX, maxY, maxZ;

    //variables prefixed with c refer to the iteration of the currently cached chunk section
    private int chunkX, chunkYIndex, chunkZ;
    protected int cStartX, cStartZ;
    protected int cEndX, cEndZ;
    protected int cX, cY, cZ;

    protected int cTotalSize;
    protected int cIterated;

    private ChunkAccess cachedChunk;
    protected LevelChunkSection cachedChunkSection;

    public ChunkAwareBlockSweeper(Level world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.world = world;

        this.minX = minX;
        this.maxX = maxX;
        this.minY = Mth.clamp(minY, this.world.getMinY(), this.world.getMaxY());
        this.maxY = Mth.clamp(maxY, this.world.getMinY(), this.world.getMaxY());
        this.minZ = minZ;
        this.maxZ = maxZ;

        this.chunkX = SectionPos.blockToSectionCoord(this.minX);
        this.chunkZ = SectionPos.blockToSectionCoord(this.minZ);

        this.cIterated = 0;
        this.cTotalSize = 0;

        //decrement as first nextSection call will increment it again
        this.chunkX--;
    }

    final protected boolean nextSection() {
        do {
            do {
                // Find the coordinates of the next section
                if (
                    this.cachedChunk != null &&
                        this.chunkYIndex < this.world.getMaxSectionY() &&
                        this.chunkYIndex < this.cachedChunk.getSectionIndex(this.maxY)
                ) {
                    this.chunkYIndex++;
                    this.cachedChunkSection = this.cachedChunk.getSections()[this.chunkYIndex];
                } else {
                    if (this.chunkX < SectionPos.blockToSectionCoord(this.maxX)) {
                        //first initialization takes this branch
                        this.chunkX++;
                    } else {
                        if (this.chunkZ < SectionPos.blockToSectionCoord(this.maxZ)) {
                            this.chunkX = SectionPos.blockToSectionCoord(this.minX);
                            this.chunkZ++;
                        } else {
                            // Important: No field assignment / mutation happens in the code path to this, so
                            // consecutive nextSection calls keep returning false, instead of working on invalid data.
                            // Otherwise, additional chunk sections would be iterated incorrectly:
                            // https://github.com/CaffeineMC/lithium/issues/628
                            return false; // no more sections to iterate
                        }
                    }
                    this.cachedChunk = this.world.getChunk(this.chunkX, this.chunkZ, ChunkStatus.FULL, false);
                    if (this.cachedChunk != null) {
                        this.chunkYIndex = Mth.clamp(
                            this.cachedChunk.getSectionIndex(this.minY),
                            0,
                            this.world.getMaxSectionY()
                        );
                        this.cachedChunkSection = this.cachedChunk.getSections()[this.chunkYIndex];
                    }
                }
                //skip empty chunks and empty chunk sections
            } while (this.cachedChunk == null || this.cachedChunkSection == null || this.cachedChunkSection.hasOnlyAir());

            this.cEndX = Math.min(this.maxX, SectionPos.sectionToBlockCoord(this.chunkX, 15));
            int cEndY = Math.min(this.maxY, SectionPos.sectionToBlockCoord(this.world.getSectionYFromSectionIndex(this.chunkYIndex), 15));
            this.cEndZ = Math.min(this.maxZ, SectionPos.sectionToBlockCoord(this.chunkZ, 15));

            this.cStartX = Math.max(this.minX, SectionPos.sectionToBlockCoord(this.chunkX));
            int cStartY = Math.max(this.minY, SectionPos.sectionToBlockCoord(this.world.getSectionYFromSectionIndex(this.chunkYIndex)));
            this.cStartZ = Math.max(this.minZ, SectionPos.sectionToBlockCoord(this.chunkZ));
            this.cX = this.cStartX;
            this.cY = cStartY;
            this.cZ = this.cStartZ;

            this.cTotalSize = (this.cEndX - this.cStartX + 1) * (cEndY - cStartY + 1) * (this.cEndZ - this.cStartZ + 1);
            //skip completely empty section iterations
        } while (this.cTotalSize == 0);
        this.cIterated = 0;

        return true;
    }

    /**
     * Advances the sweep forward.
     *
     * @return the next position as {@link Entry}, or {@link #endOfData()} when no blocks are left
     */
    @Override
    public Entry computeNext() {
        if (this.cIterated >= this.cTotalSize) {
            if (!this.nextSection()) {
                return this.endOfData();
            }
        }

        this.cIterated++;

        final int x = this.cX;
        final int y = this.cY;
        final int z = this.cZ;

        //The iteration order within a chunk section is chosen so that it causes a mostly linear array access in the storage.
        //In net.minecraft.world.chunk.PalettedContainer.toIndex x gets the 4 least significant bits, z the 4 above, and y the 4 even higher ones.
        //Linearly accessing arrays is faster than other access patterns.
        if (this.cX < this.cEndX) {
            this.cX++;
        } else if (this.cZ < this.cEndZ) {
            this.cX = this.cStartX;
            this.cZ++;
        } else {
            this.cX = this.cStartX;
            this.cZ = this.cStartZ;
            this.cY++;
            //stop condition was already checked using this.cIterated at the start of the method
        }

        final BlockState state = this.cachedChunkSection.getBlockState(x & 15, y & 15, z & 15);
        this.pos.set(x, y, z);

        return this.entry.set(state);
    }

    public static class Entry {
        private final BlockPos.MutableBlockPos pos;
        private BlockState state;

        public Entry(BlockPos.MutableBlockPos pos) {
            this.pos = pos;
        }

        public BlockState getState() {
            return this.state;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public Entry set(BlockState state) {
            this.state = state;
            return this;
        }
    }
}
