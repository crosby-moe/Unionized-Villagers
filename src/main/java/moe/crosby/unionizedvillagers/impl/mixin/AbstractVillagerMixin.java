package moe.crosby.unionizedvillagers.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.crosby.unionizedvillagers.impl.IVillager;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractVillager.class)
public class AbstractVillagerMixin {
    @Shadow @Nullable protected MerchantOffers offers;

    /**
     * Prevent serializing strike offers as the regular offers
     */
    @WrapOperation(method = "addAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/villager/AbstractVillager;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"))
    private MerchantOffers wrapGetOffers(AbstractVillager instance, Operation<MerchantOffers> original) {
        if (instance instanceof Villager) {
            return this.offers != null ? this.offers : new MerchantOffers();
        } else {
            return original.call(instance);
        }
    }

    @Inject(method = "getOffers", at = @At("HEAD"), cancellable = true)
    private void replaceOffersIfStriking(CallbackInfoReturnable<MerchantOffers> cir) {
        if (this instanceof IVillager villager && villager.unionized$isInStrike()) {
            cir.setReturnValue(villager.unionized$getStrikeOffers());
            cir.cancel();
        }
    }
}
