package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.impl.IVillagerEntity;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import moe.crosby.unionizedvillagers.impl.ai.StrikeTradeOffers;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TradeOutputSlot.class)
public class TradeOutputSlotMixin {
    @Shadow @Final private Merchant merchant;
    @Shadow @Final private MerchantInventory merchantInventory;

    @Inject(method = "onTakeItem", at = @At("HEAD"), cancellable = true)
    private void interceptTrade(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        TradeOffer tradeOffer = this.merchantInventory.getTradeOffer();
        if (tradeOffer != null) {
            ItemStack itemStack = this.merchantInventory.getStack(0);
            ItemStack itemStack2 = this.merchantInventory.getStack(1);

            if (this.merchant instanceof VillagerEntity villager && villager.getEntityWorld() instanceof ServerWorld world &&player instanceof ServerPlayerEntity serverPlayer && ((IVillagerEntity) villager).unionized$isInStrike() && StrikeTradeOffers.isEndStrikeStack(stack) && (tradeOffer.depleteBuyItems(itemStack, itemStack2) || tradeOffer.depleteBuyItems(itemStack2, itemStack))) {
                // disable striking state
                int searchDistance = Math.max(world.getGameRules().getValue(UnionizedVillagers.VIEW_RANGE), 48) + 16;
                EntitySensing.forEach(world, EntitySensing.VILLAGER_FILTER, villager.getBlockPos(), searchDistance, innerVillager -> {
                    UnionizedVillagersImpl.endStrike(innerVillager);
                    return LazyIterationConsumer.NextIteration.CONTINUE;
                });

                // cancel the trade
                ci.cancel();

                // manually decrement inputs
                this.merchantInventory.setStack(0, itemStack);
                this.merchantInventory.setStack(1, itemStack2);

                // close screen
                serverPlayer.closeHandledScreen();
            }
        }
    }
}
