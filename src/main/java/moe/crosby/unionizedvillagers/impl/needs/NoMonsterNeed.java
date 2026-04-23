package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import moe.crosby.unionizedvillagers.impl.IVillager;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.Nullable;

/**
 * Villager need that ensures a villager does not have a visible monster near
 */
public class NoMonsterNeed extends VillagerNeed {
    public NoMonsterNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        if (world.getDifficulty() == Difficulty.PEACEFUL) {
            debug(Villager, true, () -> "world is peaceful");
            return true;
        }

        if (((IVillager) Villager).unionized$heardMonsterNoise()) {
            debug(Villager, false, () -> "heard monster");
            return false;
        }

        int searchRadius = world.getGameRules().get(UnionizedVillagers.VIEW_RANGE);
        boolean seeThroughWalls = world.getGameRules().get(UnionizedVillagers.SEE_MONSTERS_THROUGH_WALLS);

        @Nullable Monster seenMonster = EntitySensing.getFirst(
            world, EntitySensing.HOSTILE_FILTER, Villager.blockPosition(), searchRadius,
            monster -> EntitySensing.isVisible(monster) && (seeThroughWalls || Villager.getSensing().hasLineOfSight(monster))
        );

        boolean isMet = seenMonster == null;

        debug(Villager, isMet, () -> "monster is at " + (seenMonster == null ? null : seenMonster.blockPosition()));

        return isMet;
    }
}
