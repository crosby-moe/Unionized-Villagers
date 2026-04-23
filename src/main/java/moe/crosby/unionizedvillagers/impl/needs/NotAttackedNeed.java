package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * Villager need that ensures a villager was not attacked by the player
 */
public class NotAttackedNeed extends VillagerNeed {
    public NotAttackedNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        Optional<LivingEntity> hurtByEntityMemory = Villager.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY);
        if (hurtByEntityMemory.isPresent() && hurtByEntityMemory.get() == playerEntity) {
            debug(Villager, false, () -> "villager attacked by " + hurtByEntityMemory.get().getName().getString());
            return false;
        }

        Optional<DamageSource> hurtByMemory = Villager.getBrain().getMemory(MemoryModuleType.HURT_BY);
        if (hurtByMemory.isPresent() && hurtByMemory.get().getEntity() == playerEntity) {
            debug(Villager, false, () -> "villager attacked by " + hurtByMemory.get().getEntity().getName().getString());
            return false;
        }

        debug(Villager, true, null);
        return true;
    }
}
