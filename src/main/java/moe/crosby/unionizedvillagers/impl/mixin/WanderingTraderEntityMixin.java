package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.api.StrikeTriggers;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {
    public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "afterUsing", at = @At("TAIL"))
    private void afterTradeComplete(TradeOffer offer, CallbackInfo ci) {
        if (getEntityWorld() instanceof ServerWorld world && this.getCustomer() instanceof ServerPlayerEntity player && EntitySensing.isVisible(player)) {
            // sense villagers
            int searchDistance = world.getGameRules().getValue(UnionizedVillagers.VIEW_RANGE);
            EntitySensing.forEach(world, EntitySensing.VILLAGER_FILTER, this.getBlockPos(), searchDistance, villager -> {
                if (villager.getVisibilityCache().canSee(player)) {
                    UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.ILLEGAL_TRADING);
                    return LazyIterationConsumer.NextIteration.ABORT;
                }

                return LazyIterationConsumer.NextIteration.CONTINUE;
            });
        }
    }
}
