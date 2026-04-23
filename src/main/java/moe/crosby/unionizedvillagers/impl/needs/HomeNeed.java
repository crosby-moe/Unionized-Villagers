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
 * Villager need that ensures a villager has a home
 */
public class HomeNeed extends VillagerNeed {
    public HomeNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        Optional<GlobalPos> homeOpt = Villager.getBrain().getMemory(MemoryModuleType.HOME);

        boolean isMet = homeOpt.isPresent();

        debug(Villager, isMet, () -> "home is at " + homeOpt.map(GlobalPos::pos).orElse(null));

        return isMet;
    }
}
