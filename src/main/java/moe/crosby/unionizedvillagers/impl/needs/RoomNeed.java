package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Villager need that ensures a villager is not trapped
 */
public class RoomNeed extends VillagerNeed {
    private static final int DISTANCE_TARGET = 5;
    private static final int SQUARE_DISTANCE_TARGET = DISTANCE_TARGET * DISTANCE_TARGET;
    private static final int SEARCH_RADIUS = 32;

    public RoomNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(World world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        // if currently pathing, check the distance
        @Nullable Path path = villagerEntity.getNavigation().getCurrentPath();
        if (path != null && path.getEnd() != null) {
            if (path.getEnd().getSquaredDistance(villagerEntity.getBlockPos()) > SQUARE_DISTANCE_TARGET) {
                debug(villagerEntity, true, () -> "existing pathing target at " + path.getEnd().getBlockPos());
                return true;
            }
        }

        // get closest important location
        Vec3d closestPoi = null;

        Optional<GlobalPos> homePoi = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HOME);
        if (homePoi.isPresent() && homePoi.get().getDimension() == world.getRegistryKey()) {
            Vec3d homePos = homePoi.get().getPos().toCenterPos();
            if (villagerEntity.squaredDistanceTo(homePos) > SQUARE_DISTANCE_TARGET) {
                debug(villagerEntity, true, () -> "accessible home at " + homePos);
                return true;
            }
            closestPoi = homePos;
        }

        Optional<GlobalPos> jobPoi = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);
        if (jobPoi.isPresent() && jobPoi.get().getDimension() == world.getRegistryKey()) {
            Vec3d jobPos = jobPoi.get().getPos().toCenterPos();
            if (villagerEntity.squaredDistanceTo(jobPos) > SQUARE_DISTANCE_TARGET) {
                debug(villagerEntity, true, () -> "accessible job site at " + jobPos);
                return true;
            }

            if (closestPoi == null || villagerEntity.squaredDistanceTo(jobPos) < villagerEntity.squaredDistanceTo(closestPoi)) {
                closestPoi = jobPos;
            }
        }

        Optional<GlobalPos> potentialJobPoi = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
        if (potentialJobPoi.isPresent() && potentialJobPoi.get().getDimension() == world.getRegistryKey()) {
            Vec3d potentialJobPos = potentialJobPoi.get().getPos().toCenterPos();
            if (villagerEntity.squaredDistanceTo(potentialJobPos) > SQUARE_DISTANCE_TARGET) {
                debug(villagerEntity, true, () -> "accessible potential job site at " + potentialJobPos);
                return true;
            }

            if (closestPoi == null || villagerEntity.squaredDistanceTo(potentialJobPos) < villagerEntity.squaredDistanceTo(closestPoi)) {
                closestPoi = potentialJobPos;
            }
        }

        Optional<GlobalPos> meetingPointPoi = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.MEETING_POINT);
        if (meetingPointPoi.isPresent() && meetingPointPoi.get().getDimension() == world.getRegistryKey()) {
            Vec3d meetingPointPos = meetingPointPoi.get().getPos().toCenterPos();
            if (villagerEntity.squaredDistanceTo(meetingPointPos) > SQUARE_DISTANCE_TARGET) {
                debug(villagerEntity, true, () -> "accessible meeting point at " + meetingPointPos);
                return true;
            }

            if (closestPoi == null || villagerEntity.squaredDistanceTo(meetingPointPos) < villagerEntity.squaredDistanceTo(closestPoi)) {
                closestPoi = meetingPointPos;
            }
        }

        // find reachable position further than target
        for (int i = 0; i < 10; i++) {
            Vec3d target = FuzzyTargeting.findFrom(villagerEntity, SEARCH_RADIUS, SEARCH_RADIUS / 2, closestPoi);
            if (target != null && villagerEntity.squaredDistanceTo(target) > SQUARE_DISTANCE_TARGET) {
                debug(villagerEntity, true, () -> "can path to " + target);
                return true;
            }
        }

        debug(villagerEntity, false, null);

        return false;
    }
}
