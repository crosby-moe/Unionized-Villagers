package moe.crosby.unionizedvillagers.mixin;

import moe.crosby.unionizedvillagers.IOshaViolationHolder;
import moe.crosby.unionizedvillagers.WorldUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {
    @Unique private int tickDelay = 4;
    @Unique private boolean oshaViolated = false;

    @Shadow protected abstract void sayNo();
    @Shadow protected abstract void sendOffersToCustomer();

    private VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "prepareOffersFor", at = @At("TAIL"))
    private void livingSpace(CallbackInfo ci) {
        if (offers == null) return;

        if (!WorldUtils.isLivable(getWorld(), (VillagerEntity) (Object) this)) {
            oshaViolated = true;
            for (var trade : offers) ((IOshaViolationHolder) trade).villagerBalancing$setOshaViolationStatus(true);
            sayNo();
        } else {
            oshaViolated = false;
            for (var trade : offers) ((IOshaViolationHolder) trade).villagerBalancing$setOshaViolationStatus(false);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (offers == null || getCustomer() == null) return;

        if (tickDelay-- > 0) return;
        tickDelay = 4;

        if (!WorldUtils.isLivable(getWorld(), (VillagerEntity) (Object) this)) {
            if (!oshaViolated) {
                oshaViolated = true;
                for (var trade : offers) ((IOshaViolationHolder) trade).villagerBalancing$setOshaViolationStatus(true);
                sendOffersToCustomer();
            }
        } else {
            if (oshaViolated) {
                oshaViolated = false;
                for (var trade : offers) ((IOshaViolationHolder) trade).villagerBalancing$setOshaViolationStatus(false);
                sendOffersToCustomer();
            }
        }
    }
}
