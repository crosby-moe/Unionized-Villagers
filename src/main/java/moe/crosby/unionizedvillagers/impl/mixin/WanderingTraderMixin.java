package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.api.StrikeTriggers;
import moe.crosby.unionizedvillagers.impl.IServerPlayer;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin extends AbstractVillager {
    public WanderingTraderMixin(EntityType<? extends @NotNull AbstractVillager> entityType, ServerLevel world) {
        super(entityType, world);
    }

    @Inject(method = "rewardTradeXp", at = @At("TAIL"))
    private void afterTradeComplete(MerchantOffer offer, CallbackInfo ci) {
        if (this.level() instanceof ServerLevel world && this.getTradingPlayer() instanceof ServerPlayer player && EntitySensing.isVisible(player)) {
            ((IServerPlayer) player).unionized$getWarningManager().increaseWarningLevel(4);

            if (UnionizedVillagersImpl.beginStrike(world, this.position())) {
                // notify players
                EntitySensing.forEach(world, EntitySensing.PLAYER_FILTER, this.blockPosition(), UnionizedVillagersImpl.STRIKE_RANGE, somePlayer -> {
                    Component feeedback = Component.translatable(StrikeTriggers.ILLEGAL_TRADING.translationKey(), player.getDisplayName());
                    somePlayer.sendSystemMessage(Component.empty().withStyle(ChatFormatting.YELLOW).append(feeedback));

                    return AbortableIterationConsumer.Continuation.CONTINUE;
                });
            }
        }
    }
}
