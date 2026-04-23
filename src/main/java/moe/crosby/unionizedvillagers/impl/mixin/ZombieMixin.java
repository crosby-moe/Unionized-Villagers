package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.api.StrikeTriggers;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(Zombie.class)
public class ZombieMixin {
    @Inject(method = "killedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/zombie/Zombie;convertVillagerToZombieVillager(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;)Z"))
    private void onZombification(ServerLevel level, LivingEntity entity, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (entity.isInvisible()) {
            return;
        }

        // sense villagers
        int searchDistance = level.getGameRules().get(UnionizedVillagers.VIEW_RANGE);
        AABB searchBox = new AABB(entity.blockPosition()).inflate(searchDistance);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, searchBox, player -> !player.isInvisible() && !player.isSpectator());

        EntitySensing.forEach(level, EntitySensing.VILLAGER_FILTER, entity.blockPosition(), searchDistance, villager -> {
            if (villager.getSensing().hasLineOfSight(entity)) {
                for (Iterator<ServerPlayer> it = players.iterator(); it.hasNext();) {
                    ServerPlayer player = it.next();
                    if (villager.getSensing().hasLineOfSight(player)) {
                        UnionizedVillagers.emitTrigger(level, player, villager, villager, StrikeTriggers.ZOMBIFIED);
                        it.remove();

                        if (players.isEmpty()) {
                            return AbortableIterationConsumer.Continuation.ABORT;
                        }
                    }
                }
            }

            return AbortableIterationConsumer.Continuation.CONTINUE;
        });
    }
}
