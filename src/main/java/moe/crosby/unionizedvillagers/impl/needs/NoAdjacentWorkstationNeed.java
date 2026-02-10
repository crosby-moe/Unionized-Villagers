package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;

import java.util.Optional;

public class NoAdjacentWorkstationNeed extends VillagerNeed {
    public NoAdjacentWorkstationNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(World world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        Optional<GlobalPos> jobOpt = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);

        if (world instanceof ServerWorld serverWorld && jobOpt.isPresent() && jobOpt.get().getDimension() == world.getRegistryKey()) {
            BlockPos jobPos = jobOpt.get().getPos();

            Optional<BlockPos> otherPos = serverWorld.getPointOfInterestStorage().getInSquare(
                poi -> poi.isIn(PointOfInterestTypeTags.ACQUIRABLE_JOB_SITE),
                villagerEntity.getBlockPos(),
                8,
                PointOfInterestStorage.OccupationStatus.IS_OCCUPIED)
                .map(PointOfInterest::getPos)
                .filter(pos -> !pos.equals(jobPos))
                .findAny();

            boolean isMet = otherPos.isEmpty();

            debug(villagerEntity, isMet, () -> "other job site at " + otherPos.orElse(null));

            return isMet;
        }

        return false;
    }
}
