package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.impl.IVillager;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import moe.crosby.unionizedvillagers.impl.ai.StrikeTradeOffers;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantResultSlot.class)
public class MerchantResultSlotMixin {
    @Shadow @Final private Merchant merchant;
    @Shadow @Final private MerchantContainer slots;

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void interceptTrade(Player player, ItemStack carried, CallbackInfo ci) {
        MerchantOffer tradeOffer = this.slots.getActiveOffer();
        if (tradeOffer != null) {
            ItemStack itemStack = this.slots.getItem(0);
            ItemStack itemStack2 = this.slots.getItem(1);

            if (this.merchant instanceof Villager villager && villager.level() instanceof ServerLevel world && player instanceof ServerPlayer serverPlayer && ((IVillager) villager).unionized$isInStrike() && StrikeTradeOffers.isEndStrikeStack(carried) && (tradeOffer.take(itemStack, itemStack2) || tradeOffer.take(itemStack2, itemStack))) {
                // disable striking state
                int searchDistance = Math.max(world.getGameRules().get(UnionizedVillagers.VIEW_RANGE), 48) + 16;
                EntitySensing.forEach(world, EntitySensing.VILLAGER_FILTER, villager.blockPosition(), searchDistance, innerVillager -> {
                    UnionizedVillagersImpl.endStrike(innerVillager);
                    return AbortableIterationConsumer.Continuation.CONTINUE;
                });

                // cancel the trade
                ci.cancel();
                if (StrikeTradeOffers.isEndStrikeStack(serverPlayer.containerMenu.getCarried())) { // sanity check
                    serverPlayer.containerMenu.getCarried().setCount(0);
                }

                // manually decrement inputs
                this.slots.setItem(0, itemStack);
                this.slots.setItem(1, itemStack2);

                // close screen
                serverPlayer.closeContainer();

                // send message
                serverPlayer.sendSystemMessage(UnionizedVillagersImpl.of(villager).append(Component.translatable("unionized-villagers.text.strike_ended")));

                // allow ending strikes in the future
                tradeOffer.resetUses();
            }
        }
    }
}
