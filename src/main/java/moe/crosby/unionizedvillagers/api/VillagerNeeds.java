package moe.crosby.unionizedvillagers.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import moe.crosby.unionizedvillagers.impl.needs.*;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.List;

public final class VillagerNeeds {
    public static final List<VillagerNeed> VILLAGER_NEEDS = new ObjectArrayList<>();

    public static final VillagerNeed IN_HOME_DIMENSION = register("in_home_dimension", InHomeDimensionNeed::new, 1);
    public static final VillagerNeed NOT_DAMAGED = register("not_damaged", NotDamagedNeed::new, 2);
    public static final VillagerNeed NOT_POISONED = register("not_poisoned", NotPoisonedNeed::new, 2);
    public static final VillagerNeed NOT_ATTACKED = register("not_attacked", NotAttackedNeed::new, 2);
    public static final VillagerNeed DAY_TIME = register("day_time", DayTimeNeed::new, 3);
    public static final VillagerNeed WORKSTATION = register("workstation", WorkstationNeed::new, 3);
    public static final VillagerNeed HOME = register("home", HomeNeed::new, 3);
    public static final VillagerNeed NO_ADJACENT_WORKSTATION = register("no_adjacent_workstation", NoAdjacentWorkstationNeed::new, 4);
    public static final VillagerNeed LIGHT = register("light", LightNeed::new, 4);
    public static final VillagerNeed NO_MONSTER_NEARBY = register("no_monster_nearby", NoMonsterNeed::new, 5);
    public static final VillagerNeed NO_HAZARD_NEARBY = register("no_hazard_nearby", NoHazardNearbyNeed::new, 5);
    public static final VillagerNeed ROOM = register("room", RoomNeed::new, 6);
    public static final VillagerNeed GUARDIAN = register("guardian", GuardianNeed::new, 7);
    public static final VillagerNeed IN_HOME_VILLAGE = register("in_home_village", InHomeVillageNeed::new, 8);

    @FunctionalInterface
    private interface VillagerNeedFactory {
        VillagerNeed create(Identifier identifier, int priority);
    }

    // IMPL

    private static VillagerNeed register(String name, VillagerNeedFactory factory, int priority) {
        VillagerNeed need = factory.create(UnionizedVillagersImpl.id(name), priority);
        VILLAGER_NEEDS.add(need);
        return need;
    }

    public static void initialize() {
        RegisterVillagerNeed.EVENT.invoker().register(VILLAGER_NEEDS);
        VillagerNeeds.VILLAGER_NEEDS.sort(Comparator.comparingInt(need -> need.priority));
    }
}
