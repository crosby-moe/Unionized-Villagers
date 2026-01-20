package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Villager need that ensures a villager has a workstation
 */
public class WorkstationNeed extends VillagerNeed {
    public WorkstationNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(World world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        Optional<GlobalPos> jobOpt = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);
        boolean isMet = jobOpt.isPresent();

        debug(villagerEntity, isMet, () -> "job site at " + jobOpt.map(GlobalPos::getPos).orElse(null));

        return isMet;
    }
}
