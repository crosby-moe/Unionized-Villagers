package moe.crosby.unionizedvillagers.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.crosby.unionizedvillagers.impl.IVillager;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import moe.crosby.unionizedvillagers.impl.ai.StrikingTexts;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Merchant.class)
public interface MerchantMixin {
    @WrapOperation(method = "openTradingScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/Merchant;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"))
    private MerchantOffers wrapOffers(Merchant merchant, Operation<MerchantOffers> original, @Local(argsOnly = true) Player player) {
        if (merchant instanceof IVillager villager && villager.unionized$isInStrike()) {
            Villager Villager = (Villager) merchant;

            player.sendSystemMessage(UnionizedVillagersImpl.of(Villager).append(Component.translatable(StrikingTexts.get(Villager.getRandom()))));

            return villager.unionized$getStrikeOffers();
        } else {
            return original.call(merchant);
        }
    }
}
