package moe.crosby.unionizedvillagers.mixin.client;

import moe.crosby.unionizedvillagers.IOshaViolationHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(TradeOffer.class)
public abstract class TradeOfferMixin {
    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void fromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(IOshaViolationHolder.IDENTIFIER)) ((IOshaViolationHolder) this).villagerBalancing$setOshaViolationStatus(true);
    }
}
