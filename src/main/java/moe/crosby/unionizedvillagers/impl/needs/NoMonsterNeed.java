package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import moe.crosby.unionizedvillagers.impl.IVillagerEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.Difficulty;

import java.util.List;

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

        int searchRadius = world.getGameRules().getValue(UnionizedVillagers.VIEW_RANGE);
        Box searchBox = new Box(villagerEntity.getBlockPos()).expand(searchRadius);

        List<HostileEntity> monsters = world.getEntitiesByType(TypeFilter.instanceOf(HostileEntity.class), searchBox, monster -> canSee(villagerEntity, monster));
        boolean isMet = monsters.isEmpty();

        debug(villagerEntity, isMet, () -> "monster is at " + (monsters.isEmpty() ? null : monsters.getFirst().getBlockPos()));

        return isMet;
    }

    private boolean canSee(VillagerEntity villagerEntity, HostileEntity hostileEntity) {
        return hostileEntity.isAlive() && !hostileEntity.isInvisible() && villagerEntity.getVisibilityCache().canSee(hostileEntity);
    }
}
