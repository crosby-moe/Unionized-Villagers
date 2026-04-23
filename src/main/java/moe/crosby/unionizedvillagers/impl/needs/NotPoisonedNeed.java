package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Villager need that ensures a villager does not have a status effect that is considered a poison
 * @see moe.crosby.unionizedvillagers.api.UnionizedVillagers#POISONS_TAG
 */
public class NotPoisonedNeed extends VillagerNeed {
    public NotPoisonedNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        for (Holder<@NotNull MobEffect> effectType : Villager.getActiveEffectsMap().keySet()) {
            if (effectType.is(UnionizedVillagers.POISONS_TAG)) {
                debug(Villager, false, () -> "villager has effect " + effectType.getRegisteredName());
                return false;
            }
        }

        debug(Villager, true, null);
        return true;
    }
}
