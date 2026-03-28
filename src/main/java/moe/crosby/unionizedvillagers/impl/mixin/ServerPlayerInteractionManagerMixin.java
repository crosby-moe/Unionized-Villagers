package moe.crosby.unionizedvillagers.impl.mixin;

import com.google.common.base.Predicates;
import com.llamalad7.mixinextras.sugar.Local;
import moe.crosby.unionizedvillagers.api.StrikeTriggers;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow protected ServerWorld world;

    @Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;afterBreak(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/item/ItemStack;)V"))
    private void injectAfterBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockState state) {
        if (player.isInvisible()) {
            return;
        }

        // sense villagers
        int searchDistance = world.getGameRules().getValue(UnionizedVillagers.VIEW_RANGE);
        List<VillagerEntity> villagers = EntitySensing.getEntities(world, EntitySensing.VILLAGER_FILTER, pos, searchDistance, Predicates.alwaysTrue());

        boolean isJobSite = false;

        // check if breaking own workstation
        for (VillagerEntity villager : villagers) {
            Optional<GlobalPos> jobOpt = villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);

            if (jobOpt.isPresent() && jobOpt.get().dimension() == world.getRegistryKey() && jobOpt.get().pos().equals(pos)) {
                if (villager.getVisibilityCache().canSee(player)) {
                    UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.BREAKING_OWN_WORKSPACE);
                    return;
                } else {
                    isJobSite = true;
                }
            }
        }

        for (VillagerEntity villager : villagers) {
            // check if breaking other's workstation
            if (isJobSite && villager.getVisibilityCache().canSee(player)) {
                UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.BREAKING_WORKSPACE);
                return;
            }

            // check if breaking own possession
            Optional<RegistryKey<VillagerProfession>> key = villager.getVillagerData().profession().getKey();
            if (key.isPresent()) {
                TagKey<Block> tag = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id(key.get().getValue().getPath() + "_possessions"));

                if (state.isIn(tag) && villager.getVisibilityCache().canSee(player)) {
                    UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.BREAKING_POSSESSION);
                    return;
                }
            }
        }
    }
}
