package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Optional;

/**
 * Villager need that ensures a villager was not attacked by the player
 */
public class NotAttackedNeed extends VillagerNeed {
    public NotAttackedNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerWorld world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        Optional<LivingEntity> hurtByEntityMemory = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HURT_BY_ENTITY);
        if (hurtByEntityMemory.isPresent() && hurtByEntityMemory.get() == playerEntity) {
            debug(villagerEntity, false, () -> "villager attacked by " + hurtByEntityMemory.get().getName().getString());
            return false;
        }

        Optional<DamageSource> hurtByMemory = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HURT_BY);
        if (hurtByMemory.isPresent() && hurtByMemory.get().getAttacker() == playerEntity) {
            debug(villagerEntity, false, () -> "villager attacked by " + hurtByMemory.get().getAttacker().getName().getString());
            return false;
        }

        debug(villagerEntity, true, null);
        return true;
    }
}
