package moe.crosby.unionizedvillagers.impl.needs;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;

import java.util.Optional;

/**
 * Villager need that ensures a villager is in the same dimension as its home
 */
public class InHomeDimensionNeed extends VillagerNeed {
    public InHomeDimensionNeed(Identifier identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public boolean isMet(ServerWorld world, VillagerEntity villagerEntity, PlayerEntity playerEntity) {
        Optional<GlobalPos> opt = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HOME);

        boolean isMet = opt.isEmpty() || opt.get().dimension() == world.getRegistryKey();

        debug(villagerEntity, isMet, () -> "home is in " + opt.map(GlobalPos::dimension).map(RegistryKey::getValue).orElse(null));

        return isMet;
    }
}
