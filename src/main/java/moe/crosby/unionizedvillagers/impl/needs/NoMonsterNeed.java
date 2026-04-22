package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import moe.crosby.unionizedvillagers.impl.IVillagerEntity;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
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
    public boolean isMet(ServerWorld world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        if (world.getDifficulty() == Difficulty.PEACEFUL) {
            debug(villagerEntity, true, () -> "world is peaceful");
            return true;
        }

        if (((IVillagerEntity) villagerEntity).unionized$heardMonsterNoise()) {
            debug(villagerEntity, false, () -> "heard monster");
            return false;
        }

        int searchRadius = world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE);
        boolean seeThroughWalls = world.getGameRules().getBoolean(UnionizedVillagers.SEE_MONSTERS_THROUGH_WALLS);

        @Nullable HostileEntity seenMonster = EntitySensing.getFirst(
            world, EntitySensing.HOSTILE_FILTER, villagerEntity.getBlockPos(), searchRadius,
            monster -> EntitySensing.isVisible(monster) && (seeThroughWalls || villagerEntity.getVisibilityCache().canSee(monster))
        );

        boolean isMet = seenMonster == null;

        debug(villagerEntity, isMet, () -> "monster is at " + (seenMonster == null ? null : seenMonster.getBlockPos()));

        return isMet;
    }
}
