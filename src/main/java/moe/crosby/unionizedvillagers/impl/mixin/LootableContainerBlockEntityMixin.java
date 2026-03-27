package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.api.StrikeTriggers;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.impl.ai.VillagerPossessions;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin extends BlockEntity {
    @Shadow @Nullable protected Identifier lootTableId;

    public LootableContainerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "checkLootInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootManager;getLootTable(Lnet/minecraft/util/Identifier;)Lnet/minecraft/loot/LootTable;"))
    private void checkLootIn(PlayerEntity player, CallbackInfo ci) {
        if (this.world instanceof ServerWorld serverWorld && (Object) this instanceof ChestBlockEntity && this.lootTableId.getPath().startsWith("chests/village/")
            && player instanceof ServerPlayerEntity serverPlayer && EntitySensing.isVisible(player)) {
            int searchDistance = this.world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE);
            EntitySensing.forEach(this.world, EntitySensing.VILLAGER_FILTER, this.getPos(), searchDistance, villager -> {
                VillagerData data = villager.getVillagerData();

                if (VillagerPossessions.isVillagerPossession(data, this.lootTableId) && villager.getVisibilityCache().canSee(player)) {
                    UnionizedVillagers.emitTrigger(serverWorld, serverPlayer, villager, villager, StrikeTriggers.STEALING_POSSESSION);
                }

                return LazyIterationConsumer.NextIteration.CONTINUE;
            });
        }
    }
}
