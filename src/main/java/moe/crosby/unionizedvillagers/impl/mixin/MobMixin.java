package moe.crosby.unionizedvillagers.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.crosby.unionizedvillagers.api.StrikeTriggers;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.impl.IVillager;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {
    @Shadow
    public abstract Sensing getSensing();

    protected MobMixin(EntityType<? extends @NotNull LivingEntity> entityType, ServerLevel world) {
        super(entityType, world);
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;playAmbientSound()V"))
    private void monsterNoises(CallbackInfo ci) {
        if (this.level() instanceof ServerLevel serverWorld && (Object) this instanceof Monster) {
            EntitySensing.forEach(serverWorld, EntitySensing.VILLAGER_FILTER, this.blockPosition(), 16, villager -> {
                ((IVillager) villager).unionized$triggerMonsterNoise();

                return AbortableIterationConsumer.Continuation.CONTINUE;
            });
        }
    }

    @WrapOperation(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;playAmbientSound()V"))
    private void wrapAmbientSounds(Mob instance, Operation<Void> original) {
        if (this.level() instanceof ServerLevel serverWorld) {
            // villagers hearing monsters
            if (instance instanceof Monster) {
                EntitySensing.forEach(serverWorld, EntitySensing.VILLAGER_FILTER, this.blockPosition(), 16, villager -> {
                    ((IVillager) villager).unionized$triggerMonsterNoise();

                    return AbortableIterationConsumer.Continuation.CONTINUE;
                });
                // replace villager ambient noise with a sayNo() when striking
            } else if (instance instanceof Villager villager && ((IVillager) villager).unionized$isInStrike() && !villager.isSleeping()) {
                ((VillagerInvoker) villager).unionized$setUnhappy();
                return;
            }
        }

        original.call(instance);
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "startRiding", at = @At("HEAD"))
    private void injectStartRiding(Entity entity, boolean force, boolean sendEventAndTriggers, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Villager villager && !force && (entity instanceof Boat || entity instanceof Minecart) && this.level() instanceof ServerLevel world) {
            // sense villagers
            int searchDistance = world.getGameRules().get(UnionizedVillagers.VIEW_RANGE);

            List<ServerPlayer> players = EntitySensing.getEntities(world, EntitySensing.PLAYER_FILTER, entity.blockPosition(), searchDistance, player -> EntitySensing.isVisible(player) && this.getSensing().hasLineOfSight(player));

            UnionizedVillagers.emitTriggers(world, players, villager, villager, StrikeTriggers.KIDNAPPING);
        }
    }
}
