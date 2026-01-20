package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

/**
 * Villager need that ensures a villager has a visible golem (or suitable guardian) near
 */
public class GuardianNeed extends VillagerNeed {
    private static final double SEARCH_RADIUS = 32d;

    public GuardianNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(World world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        Box searchBox = new Box(villagerEntity.getBlockPos()).expand(SEARCH_RADIUS);

        List<Entity> guardians =  world.getOtherEntities(villagerEntity, searchBox, entity -> entity.isAlive() && !entity.isInvisible() && entity.getType().isIn(UnionizedVillagers.GUARDIAN_ENTITY_TAG));
        boolean isMet = !guardians.isEmpty();

        debug(villagerEntity, isMet, () -> "guardian is at " + (guardians.isEmpty() ? null : guardians.getFirst().getBlockPos()));

        return isMet;
    }
}
