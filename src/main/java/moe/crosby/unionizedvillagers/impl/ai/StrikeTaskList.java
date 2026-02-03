package moe.crosby.unionizedvillagers.impl.ai;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestTypes;

public class StrikeTaskList {
    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> createStrikeTasks(VillagerProfession profession, float speed) {
        return ImmutableList.of(
            Pair.of(0, StrikingTasks.createStop()),
            Pair.of(
                2,
                Tasks.pickRandomly(ImmutableList.of(Pair.of(GoToIfNearbyTask.create(MemoryModuleType.MEETING_POINT, 0.4F, 40), 2), Pair.of(MeetVillagerTask.create(), 2)))
            ),
            Pair.of(3, ForgetCompletedPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.MEETING), MemoryModuleType.MEETING_POINT)),
            Pair.of(10, FindInteractionTargetTask.create(EntityType.PLAYER, 4))
        );
    }
}
