package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Villager need that ensures a villager does not have a status effect that is considered a poison
 * @see moe.crosby.unionizedvillagers.api.UnionizedVillagers#POISONS_TAG
 */
public class NotPoisonedNeed extends VillagerNeed {
    public NotPoisonedNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(World world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        for (StatusEffect effectType : villagerEntity.getActiveStatusEffects().keySet()) {
            if (Registries.STATUS_EFFECT.getEntry(effectType).isIn(UnionizedVillagers.POISONS_TAG)) {
                debug(villagerEntity, false, () -> "villager has effect " + effectType.getName().getString());
                return false;
            }
        }

        debug(villagerEntity, true, null);
        return true;
    }
}
