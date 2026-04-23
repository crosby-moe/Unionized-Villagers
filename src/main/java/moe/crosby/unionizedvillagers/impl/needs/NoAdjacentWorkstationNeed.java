package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiManager;

import java.util.Optional;

public class NoAdjacentWorkstationNeed extends VillagerNeed {
    public NoAdjacentWorkstationNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        Optional<GlobalPos> jobOpt = Villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);

        if (world instanceof ServerLevel serverWorld && jobOpt.isPresent() && jobOpt.get().dimension() == world.dimension()) {
            BlockPos jobPos = jobOpt.get().pos();

            Optional<BlockPos> otherPos = serverWorld.getPoiManager().getInSquare(
                poi -> poi.is(PoiTypeTags.ACQUIRABLE_JOB_SITE),
                Villager.blockPosition(),
                8,
                PoiManager.Occupancy.IS_OCCUPIED)
                .map(PoiRecord::getPos)
                .filter(pos -> !pos.equals(jobPos))
                .findAny();

            boolean isMet = otherPos.isEmpty();

            debug(Villager, isMet, () -> "other job site at " + otherPos.orElse(null));

            return isMet;
        }

        return false;
    }
}
