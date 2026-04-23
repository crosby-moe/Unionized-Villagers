package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;

/**
 * Villager need that ensures it is not damaged
 */
public class NotDamagedNeed extends VillagerNeed {
    public NotDamagedNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        boolean isMet = Villager.getHealth() >= Villager.getMaxHealth();
        debug(Villager, isMet, () -> String.format("villager health is %.1f out of %.1f", Villager.getHealth(), Villager.getMaxHealth()));

        return isMet;
    }
}
