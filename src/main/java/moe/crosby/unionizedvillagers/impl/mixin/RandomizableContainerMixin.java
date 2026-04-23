package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.api.StrikeTriggers;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.impl.ai.VillagerPossessions;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.npc.villager.VillagerData;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomizableContainer.class)
public interface RandomizableContainerMixin {
    @Shadow @Nullable ResourceKey<@NotNull LootTable> getLootTable();
    @Shadow @Nullable Level getLevel();

    @Inject(method = "unpackLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/RandomizableContainer;getLootTable()Lnet/minecraft/resources/ResourceKey;"))
    private void checkLootIn(Player player, CallbackInfo ci) {
        if (this.getLevel() instanceof ServerLevel serverWorld && (Object) this instanceof ChestBlockEntity blockEntity && this.getLootTable() != null && this.getLootTable().identifier().getPath().startsWith("chests/village/")
            && player instanceof ServerPlayer serverPlayer && EntitySensing.isVisible(player)) {
            int searchDistance = serverWorld.getGameRules().get(UnionizedVillagers.VIEW_RANGE);
            EntitySensing.forEach(serverWorld, EntitySensing.VILLAGER_FILTER, blockEntity.getBlockPos(), searchDistance, villager -> {
                VillagerData data = villager.getVillagerData();

                if (VillagerPossessions.isVillagerPossession(data, this.getLootTable().identifier()) && villager.getSensing().hasLineOfSight(player)) {
                    UnionizedVillagers.emitTrigger(serverWorld, serverPlayer, villager, villager, StrikeTriggers.STEALING_POSSESSION);
                }

                return AbortableIterationConsumer.Continuation.CONTINUE;
            });
        }
    }

}
