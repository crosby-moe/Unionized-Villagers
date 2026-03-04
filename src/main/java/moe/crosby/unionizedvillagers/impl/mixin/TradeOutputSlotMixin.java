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
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.village.Merchant;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TradeOutputSlot.class)
public class TradeOutputSlotMixin {
    @Shadow @Final private Merchant merchant;

    @Inject(method = "onTakeItem", at = @At("HEAD"), cancellable = true)
    private void interceptTrade(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (this.merchant instanceof VillagerEntity villager && ((IVillagerEntity) villager).unionized$isInStrike() && stack.equals(StrikeTradeOffers.END_STRIKE_STACK)) {
            World world = villager.getWorld();

            int searchDistance = Math.max(world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE), 48) + 16;
            EntitySensing.forEach(world, EntitySensing.VILLAGER_FILTER, villager.getBlockPos(), searchDistance, innerVillager -> {
                innerVillager.getBrain().forget(UnionizedVillagersImpl.STRIKE_START_TIME);
                innerVillager.getBrain().resetPossibleActivities();

                return LazyIterationConsumer.NextIteration.CONTINUE;
            });

            ci.cancel();
        }
    }
}
