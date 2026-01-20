package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.api.VillagerNeed;
import moe.crosby.unionizedvillagers.api.VillagerNeeds;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {
    @Unique private static final int RECHECK_DELAY_TICKS = 4;
    @Unique private int tickDelay;

    @Shadow protected abstract void sayNo();

    private VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "beginTradeWith", at = @At("HEAD"), cancellable = true)
    private void uh(PlayerEntity customer, CallbackInfo ci) {
        if (shouldCancel(customer)) {
            ci.cancel();
            sayNo();
        }

        tickDelay = RECHECK_DELAY_TICKS;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (offers == null || getCustomer() == null) return;

        if (tickDelay-- > 0) return;
        tickDelay = RECHECK_DELAY_TICKS;

        if (getCustomer() instanceof ServerPlayerEntity serverPlayerEntity && shouldCancel(getCustomer())) {
            serverPlayerEntity.closeHandledScreen();
            sayNo();
        }
    }

    @Unique
    private boolean shouldCancel(PlayerEntity customer) {
        VillagerNeeds.VILLAGER_NEEDS.sort(Comparator.comparingInt(need -> need.priority));

        boolean shouldCancel = false;

        for (VillagerNeed need : VillagerNeeds.VILLAGER_NEEDS) {
            boolean isMet = need.isMet(getWorld(), (VillagerEntity) (Object) this, customer);

            shouldCancel |= !isMet;

            if (!UnionizedVillagersImpl.DEBUG && !isMet) {
                break;
            }
        }

        return shouldCancel;
    }
}
