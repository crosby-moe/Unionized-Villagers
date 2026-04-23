package moe.crosby.unionizedvillagers.impl.ai;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import org.jetbrains.annotations.NotNull;

public class StrikeTaskList {
    public static ImmutableList<@NotNull Pair<Integer, ? extends BehaviorControl<? super Villager>>> createStrikeTasks(Holder<@NotNull VillagerProfession> profession, float speed) {
        return ImmutableList.of(
            Pair.of(0, StrikingTasks.createStop()),
            Pair.of(
                2,
                new RunOne<>(ImmutableList.of(Pair.of(StrollAroundPoi.create(MemoryModuleType.MEETING_POINT, 0.4F, 40), 2), Pair.of(SocializeAtBell.create(), 2)))
            ),
            Pair.of(3, ValidateNearbyPoi.create(poiType -> poiType.is(PoiTypes.MEETING), MemoryModuleType.MEETING_POINT)),
            Pair.of(10, SetLookAndInteract.create(EntityType.PLAYER, 4))
        );
    }
}
