package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Villager need that ensures a villager has a visible golem (or suitable guardian) near
 */
public class GuardianNeed extends VillagerNeed {
    public GuardianNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        int searchRadius = world.getGameRules().get(UnionizedVillagers.VIEW_RANGE);
        AABB searchBox = new AABB(Villager.blockPosition()).inflate(searchRadius);

        List<Entity> guardians =  world.getEntities(Villager, searchBox, entity -> entity.isAlive() && !entity.isInvisible() && entity.is(UnionizedVillagers.GUARDIANS_ENTITY_TAG));
        boolean isMet = !guardians.isEmpty();

        debug(Villager, isMet, () -> "guardian is at " + (guardians.isEmpty() ? null : guardians.getFirst().blockPosition()));

        return isMet;
    }
}
