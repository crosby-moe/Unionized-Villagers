package moe.crosby.unionizedvillagers.mixin;

import moe.crosby.unionizedvillagers.IOshaViolationHolder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TradeOffer.class)
public abstract class TradeOfferMixin implements IOshaViolationHolder {
    @Shadow @Final private int maxUses;
    @Unique private boolean oshaViolated = false;

    @Inject(method = "toNbt", at = @At("TAIL"))
    private void setNbt(CallbackInfoReturnable<NbtCompound> cir) {
        if (oshaViolated) {
            cir.getReturnValue().putInt("uses", maxUses); //vanilla client compat
            cir.getReturnValue().putBoolean(IOshaViolationHolder.IDENTIFIER, true); //show clientside gui element
        }
    }


    @Inject(method = "isDisabled", at = @At("HEAD"), cancellable = true)
    private void isOshaViolated(CallbackInfoReturnable<Boolean> cir) {
        if (oshaViolated) cir.setReturnValue(true);
    }

    @Override
    public void villagerBalancing$setOshaViolationStatus(boolean b) {
        oshaViolated = b;
    }

    @Override
    public boolean villagerBalancing$getOshaViolationStatus() {
        return oshaViolated;
    }
}
