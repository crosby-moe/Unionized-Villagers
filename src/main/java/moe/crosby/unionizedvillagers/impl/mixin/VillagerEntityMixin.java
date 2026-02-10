package moe.crosby.unionizedvillagers.impl.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.crosby.unionizedvillagers.api.*;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import moe.crosby.unionizedvillagers.impl.ai.StrikeTaskList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.village.VillagerData;
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
    @Shadow public abstract VillagerData getVillagerData();

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
            int searchDistance = world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE);
            Box searchBox = new Box(entity.getBlockPos()).expand(searchDistance);
            List<ServerPlayerEntity> players = world.getEntitiesByClass(ServerPlayerEntity.class, searchBox, player -> !player.isInvisible() && !player.isSpectator() && this.getVisibilityCache().canSee(player));

            VillagerEntity villager = (VillagerEntity) (Object) this;
            UnionizedVillagers.emitTriggers(world, players, villager, villager, StrikeTriggers.KIDNAPPING);
        }

        return super.startRiding(entity, force);
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"))
    private static ImmutableList<MemoryModuleType<?>> addMemoryModule(ImmutableList<MemoryModuleType<?>> original) {
        return ImmutableList.<MemoryModuleType<?>>builder().addAll(original)
            .add(UnionizedVillagersImpl.STRIKE_START_TIME)
            .build();
    }

    @Inject(method = "initBrain", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/Brain;setCoreActivities(Ljava/util/Set;)V"))
    private void registerActivityTasks(Brain<VillagerEntity> brain, CallbackInfo ci) {
        brain.setTaskList(UnionizedVillagersImpl.STRIKE, StrikeTaskList.createStrikeTasks(this.getVillagerData().getProfession(), 0.5f));
    }
}
