package moe.crosby.unionizedvillagers.impl.mixin;

import com.google.common.base.Predicates;
import moe.crosby.unionizedvillagers.api.StrikeTriggers;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {
    public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "afterUsing", at = @At("TAIL"))
    private void afterTradeComplete(TradeOffer offer, CallbackInfo ci) {
        if (getWorld() instanceof ServerWorld world && this.getCustomer() instanceof ServerPlayerEntity player) {
            // sense villagers
            int searchDistance = world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE);
            Box searchBox = new Box(this.getBlockPos()).expand(searchDistance);
            List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, Predicates.alwaysTrue());

            for (VillagerEntity villager : villagers) {
                if (villager.getVisibilityCache().canSee(player)) {
                    UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.ILLEGAL_TRADING);
                    return;
                }
            }
        }
    }
}
