package moe.crosby.unionizedvillagers.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.crosby.unionizedvillagers.api.StrikeTriggers;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.impl.IVillagerEntity;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.MobVisibilityCache;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    @Shadow
    public abstract MobVisibilityCache getVisibilityCache();

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;playAmbientSound()V"))
    private void monsterNoises(CallbackInfo ci) {
        if (this.getEntityWorld() instanceof ServerWorld serverWorld && (Object) this instanceof HostileEntity) {
            EntitySensing.forEach(serverWorld, EntitySensing.VILLAGER_FILTER, this.getBlockPos(), 16, villager -> {
                ((IVillagerEntity) villager).unionized$triggerMonsterNoise();

                return LazyIterationConsumer.NextIteration.CONTINUE;
            });
        }
    }

    @WrapOperation(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;playAmbientSound()V"))
    private void wrapAmbientSounds(MobEntity instance, Operation<Void> original) {
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            // villagers hearing monsters
            if (instance instanceof HostileEntity) {
                EntitySensing.forEach(serverWorld, EntitySensing.VILLAGER_FILTER, this.getBlockPos(), 16, villager -> {
                    ((IVillagerEntity) villager).unionized$triggerMonsterNoise();

                    return LazyIterationConsumer.NextIteration.CONTINUE;
                });
                // replace villager ambient noise with a sayNo() when striking
            } else if (instance instanceof VillagerEntity villager && ((IVillagerEntity) villager).unionized$isInStrike() && !villager.isSleeping()) {
                ((VillagerEntityInvoker) villager).unionized$sayNo();
                return;
            }
        }

        original.call(instance);
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "startRiding", at = @At("HEAD"))
    private void injectStartRiding(Entity entity, boolean force, boolean emitEvent, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof VillagerEntity villager && !force && (entity instanceof BoatEntity || entity instanceof MinecartEntity) && this.getEntityWorld() instanceof ServerWorld world) {
            // sense villagers
            int searchDistance = world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE);

            List<ServerPlayerEntity> players = EntitySensing.getEntities(world, EntitySensing.PLAYER_FILTER, entity.getBlockPos(), searchDistance, player -> EntitySensing.isVisible(player) && this.getVisibilityCache().canSee(player));

            UnionizedVillagers.emitTriggers(world, players, villager, villager, StrikeTriggers.KIDNAPPING);
        }
    }
}
