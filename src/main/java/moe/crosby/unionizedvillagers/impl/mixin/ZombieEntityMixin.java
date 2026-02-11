package moe.crosby.unionizedvillagers.impl.mixin;

import com.google.common.base.Predicates;
import moe.crosby.unionizedvillagers.api.StrikeTriggers;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.impl.lithography.EntitySensing;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(ZombieEntity.class)
public class ZombieEntityMixin {
    @Inject(method = "onKilledOther", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/ZombieVillagerEntity;initialize(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/world/LocalDifficulty;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/EntityData;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/entity/EntityData;"))
    private void onZombification(ServerWorld world, LivingEntity other, CallbackInfoReturnable<Boolean> cir) {
        if (other.isInvisible()) {
            return;
        }

        // sense villagers
        int searchDistance = world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE);
        Box searchBox = new Box(other.getBlockPos()).expand(searchDistance);
        List<ServerPlayerEntity> players = world.getEntitiesByClass(ServerPlayerEntity.class, searchBox, entity -> !entity.isInvisible() && !entity.isSpectator());

        EntitySensing.forEach(world, EntitySensing.VILLAGER_FILTER, other.getBlockPos(), searchDistance, villager -> {
            if (villager.getVisibilityCache().canSee(other)) {
                for (Iterator<ServerPlayerEntity> it = players.iterator(); it.hasNext();) {
                    ServerPlayerEntity player = it.next();
                    if (villager.getVisibilityCache().canSee(player)) {
                        UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.ZOMBIFIED);
                        it.remove();

                        if (players.isEmpty()) {
                            return LazyIterationConsumer.NextIteration.ABORT;
                        }
                    }
                }
            }

            return LazyIterationConsumer.NextIteration.CONTINUE;
        });
    }
}
