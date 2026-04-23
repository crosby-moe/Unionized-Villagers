package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;

/**
 * Villager need that ensures it is not night
 */
public class DayTimeNeed extends VillagerNeed {
    public DayTimeNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        boolean isMet = !world.isDarkOutside();
        debug(Villager, isMet, null);
        return isMet;
    }
}
