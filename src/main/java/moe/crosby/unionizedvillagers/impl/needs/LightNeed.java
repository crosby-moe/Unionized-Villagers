package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * Villager need that ensures a minimum amount of light on the villager
 */
public class LightNeed extends VillagerNeed {
    private static final int MINIMUM_LIGHT_LEVEL = 3;

    public LightNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerWorld world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        boolean nightVision = villagerEntity.hasStatusEffect(StatusEffects.NIGHT_VISION);

        if (nightVision) {
            debug(villagerEntity, true, () -> "villager can see in the dark");
            return true;
        }

        int lightLevel = world.getLightLevel(villagerEntity.getBlockPos());
        boolean isMet = lightLevel >= MINIMUM_LIGHT_LEVEL;

        debug(villagerEntity, isMet, () -> "light level is " + lightLevel);

        return isMet;
    }
}
