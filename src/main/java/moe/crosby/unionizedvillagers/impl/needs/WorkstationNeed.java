package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.GlobalPos;

import java.util.Optional;

/**
 * Villager need that ensures a villager has a workstation
 */
public class WorkstationNeed extends VillagerNeed {
    public WorkstationNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        Optional<GlobalPos> jobOpt = Villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        boolean isMet = jobOpt.isPresent();

        debug(Villager, isMet, () -> "job site at " + jobOpt.map(GlobalPos::pos).orElse(null));

        return isMet;
    }
}
