package moe.crosby.unionizedvillagers.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.crosby.unionizedvillagers.impl.IVillagerEntity;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import moe.crosby.unionizedvillagers.impl.ai.StrikingTexts;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Merchant.class)
public interface MerchantMixin {
    @WrapOperation(method = "sendOffers", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/Merchant;getOffers()Lnet/minecraft/village/TradeOfferList;"))
    private TradeOfferList wrapOffers(Merchant merchant, Operation<TradeOfferList> original, @Local(argsOnly = true) PlayerEntity player) {
        if (merchant instanceof IVillagerEntity villager && villager.unionized$isInStrike()) {
            VillagerEntity villagerEntity = (VillagerEntity) merchant;

            player.sendMessage(UnionizedVillagersImpl.of(villagerEntity).append(Text.translatable(StrikingTexts.get(villagerEntity.getRandom()))), false);

            return villager.unionized$getStrikeOffers();
        } else {
            return original.call(merchant);
        }
    }
}
