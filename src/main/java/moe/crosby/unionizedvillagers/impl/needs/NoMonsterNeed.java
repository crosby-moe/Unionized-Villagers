package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

/**
 * Villager need that ensures a villager does not have a visible monster near
 * // todo add noise-based
 */
public class NoMonsterNeed extends VillagerNeed {
    private static final double SEARCH_DISTANCE = 32d;

    public NoMonsterNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(World world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        Box searchBox = new Box(villagerEntity.getBlockPos()).expand(SEARCH_DISTANCE);

        List<HostileEntity> monsters = world.getEntitiesByType(TypeFilter.instanceOf(HostileEntity.class), searchBox, monster -> canSee(villagerEntity, monster));
        boolean isMet = monsters.isEmpty();

        debug(villagerEntity, isMet, () -> "monster is at " + (monsters.isEmpty() ? null : monsters.getFirst().getBlockPos()));

        return isMet;
    }

    private boolean canSee(VillagerEntity villagerEntity, HostileEntity hostileEntity) {
        return hostileEntity.isAlive() && !hostileEntity.isInvisible() && villagerEntity.canSee(hostileEntity);
    }
}
