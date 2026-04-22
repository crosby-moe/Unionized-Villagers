package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.api.StrikeTriggers;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.impl.ai.VillagerPossessions;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootableInventory.class)
public interface LootableInventoryMixin {
    @Shadow @Nullable RegistryKey<LootTable> getLootTable();
    @Shadow @Nullable World getWorld();

    @Inject(method = "generateLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/LootableInventory;getLootTable()Lnet/minecraft/registry/RegistryKey;"))
    private void checkLootIn(PlayerEntity player, CallbackInfo ci) {
        if (this.getWorld() instanceof ServerWorld serverWorld && (Object) this instanceof ChestBlockEntity blockEntity && this.getLootTable() != null && this.getLootTable().getValue().getPath().startsWith("chests/village/")
            && player instanceof ServerPlayerEntity serverPlayer && EntitySensing.isVisible(player)) {
            int searchDistance = serverWorld.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE);
            EntitySensing.forEach(serverWorld, EntitySensing.VILLAGER_FILTER, blockEntity.getPos(), searchDistance, villager -> {
                VillagerData data = villager.getVillagerData();

                if (VillagerPossessions.isVillagerPossession(data, this.getLootTable().getValue()) && villager.getVisibilityCache().canSee(player)) {
                    UnionizedVillagers.emitTrigger(serverWorld, serverPlayer, villager, villager, StrikeTriggers.STEALING_POSSESSION);
                }

                return LazyIterationConsumer.NextIteration.CONTINUE;
            });
        }
    }

}
