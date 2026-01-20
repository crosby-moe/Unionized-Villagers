package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

/**
 * Villager need that ensures no two villagers share the same home position
 */
public class NoSharedHomeNeed extends VillagerNeed {
    private static final double SEARCH_DISTANCE = 32d;

    public NoSharedHomeNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(World world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        Optional<GlobalPos> opt = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HOME);
        if (opt.isEmpty()) {
            debug(villagerEntity, false, () -> "villager has no home");
            return false;
        }

        GlobalPos homePos = opt.get();
        Box searchBox = new Box(homePos.getPos()).expand(SEARCH_DISTANCE);
        List<Entity> villagers = world.getOtherEntities(villagerEntity, searchBox, entity -> isSharingBed(homePos, entity));
        boolean isMet = villagers.isEmpty();

        debug(villagerEntity, isMet, () -> "other villager is at " + (villagers.isEmpty() ? null : villagers.getFirst().getBlockPos()));

        return isMet;
    }

    private boolean isSharingBed(GlobalPos bedPos, Entity entity) {
        Optional<GlobalPos> opt;
        return entity instanceof VillagerEntity villagerEntity
            && (opt = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HOME)).isPresent()
            && !opt.get().equals(bedPos);
    }
}
