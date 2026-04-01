package moe.crosby.unionizedvillagers.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOfferList;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MerchantEntity.class)
public class MerchantEntityMixin {
    @Shadow @Nullable protected TradeOfferList offers;

    /**
     * Prevent serializing strike offers as the regular offers
     */
    @WrapOperation(method = "writeCustomData", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/MerchantEntity;getOffers()Lnet/minecraft/village/TradeOfferList;"))
    private TradeOfferList wrapGetOffers(MerchantEntity instance, Operation<TradeOfferList> original) {
        if (instance instanceof VillagerEntity) {
            return this.offers != null ? this.offers : new TradeOfferList();
        } else {
            return original.call(instance);
        }
    }
}
