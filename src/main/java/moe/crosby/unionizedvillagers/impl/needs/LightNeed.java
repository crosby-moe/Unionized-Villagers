package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;

/**
 * Villager need that ensures a minimum amount of light on the villager
 */
public class LightNeed extends VillagerNeed {
    private static final int MINIMUM_LIGHT_LEVEL = 3;

    public LightNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        boolean nightVision = Villager.hasEffect(MobEffects.NIGHT_VISION);

        if (nightVision) {
            debug(Villager, true, () -> "villager can see in the dark");
            return true;
        }

        int lightLevel = world.getRawBrightness(Villager.blockPosition(), 0);
        boolean isMet = lightLevel >= MINIMUM_LIGHT_LEVEL;

        debug(Villager, isMet, () -> "light level is " + lightLevel);

        return isMet;
    }
}
