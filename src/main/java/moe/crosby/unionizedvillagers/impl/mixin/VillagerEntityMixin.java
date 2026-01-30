package moe.crosby.unionizedvillagers.impl.mixin;

import com.google.common.base.Predicates;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeed;
import moe.crosby.unionizedvillagers.api.VillagerNeeds;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

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
        VillagerEntity villagerEntity = (VillagerEntity) (Object) this;

        boolean shouldDebug = getWorld().getGameRules().getBoolean(UnionizedVillagers.DEBUG);
        boolean shouldCancel = false;

        for (VillagerNeed need : VillagerNeeds.VILLAGER_NEEDS) {
            boolean isMet = need.isMet(getWorld(), villagerEntity, customer);

            shouldCancel |= !isMet;

            if (!shouldDebug && !isMet) {
                customer.sendMessage(UnionizedVillagersImpl.of(villagerEntity)
                        .append(Text.translatable(need.getTranslationKey())));

                break;
            }
        }

        return shouldCancel;
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        if (!force && (entity instanceof BoatEntity || entity instanceof MinecartEntity) && getWorld() instanceof ServerWorld world) {
            // sense villagers
            int searchDistance = 32;
            Box searchBox = new Box(entity.getBlockPos()).expand(searchDistance);
            List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, searchBox, player -> !player.isInvisible() && !player.isSpectator());

            for (PlayerEntity player : players) {
                if (this.getVisibilityCache().canSee(player)) {
                    UnionizedVillagersImpl.emitTrigger(world, player, (VillagerEntity) (Object) this, Text.translatable("unionized-villagers.trigger.kidnapping"));
                }
            }
        }

        return super.startRiding(entity, force);
    }
}
