package moe.crosby.unionizedvillagers.impl.mixin;

import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Villager.class)
public interface VillagerInvoker {
    @Invoker("setUnhappy")
    void unionized$setUnhappy();
}
