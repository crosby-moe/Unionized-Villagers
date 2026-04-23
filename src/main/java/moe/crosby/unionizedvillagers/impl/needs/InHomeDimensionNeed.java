package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.GlobalPos;

import java.util.Optional;

/**
 * Villager need that ensures a villager is in the same dimension as its home
 */
public class InHomeDimensionNeed extends VillagerNeed {
    public InHomeDimensionNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerLevel world, Villager Villager, Player playerEntity) {
        Optional<GlobalPos> opt = Villager.getBrain().getMemory(MemoryModuleType.HOME);

        boolean isMet = opt.isEmpty() || opt.get().dimension() == world.dimension();

        debug(Villager, isMet, () -> "home is in " + opt.map(GlobalPos::dimension).map(ResourceKey::identifier).orElse(null));

        return isMet;
    }
}
