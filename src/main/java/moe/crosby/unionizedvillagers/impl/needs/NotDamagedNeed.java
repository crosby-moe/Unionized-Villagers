package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Villager need that ensures it is not damaged
 */
public class NotDamagedNeed extends VillagerNeed {
    public NotDamagedNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(World world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        boolean isMet = villagerEntity.getHealth() >= villagerEntity.getMaxHealth();
        debug(villagerEntity, isMet, () -> String.format("villager health is %.1f out of %.1f", villagerEntity.getHealth(), villagerEntity.getMaxHealth()));

        return isMet;
    }
}
