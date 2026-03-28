package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * Villager need that ensures a villager does not have a status effect that is considered a poison
 * @see moe.crosby.unionizedvillagers.api.UnionizedVillagers#POISONS_TAG
 */
public class NotPoisonedNeed extends VillagerNeed {
    public NotPoisonedNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerWorld world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        for (RegistryEntry<StatusEffect> effectType : villagerEntity.getActiveStatusEffects().keySet()) {
            if (effectType.isIn(UnionizedVillagers.POISONS_TAG)) {
                debug(villagerEntity, false, () -> "villager has effect " + effectType.getIdAsString());
                return false;
            }
        }

        debug(villagerEntity, true, null);
        return true;
    }
}
