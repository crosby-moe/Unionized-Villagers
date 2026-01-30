package moe.crosby.unionizedvillagers.impl.mixin;

import com.google.common.base.Predicates;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
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
        int searchDistance = 32;
        Box searchBox = new Box(other.getBlockPos()).expand(searchDistance);
        List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, searchBox, entity -> !entity.isInvisible() && !entity.isSpectator());
        List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, Predicates.alwaysTrue());

        for (VillagerEntity villager : villagers) {
            if (villager.getVisibilityCache().canSee(other)) {
                for (Iterator<PlayerEntity> it = players.iterator(); it.hasNext();) {
                    PlayerEntity player = it.next();
                    if (villager.getVisibilityCache().canSee(player)) {
                        UnionizedVillagersImpl.emitTrigger(world, player, villager, Text.translatable("unionized-villagers.trigger.zombified"));
                        it.remove();

                        if (players.isEmpty()) {
                            return;
                        }
                    }
                }
            }
        }
    }
}
