package moe.crosby.unionizedvillagers.impl.mixin;

import com.google.common.base.Predicates;
import com.llamalad7.mixinextras.sugar.Local;
import moe.crosby.unionizedvillagers.api.StrikeTriggers;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow @Final protected ServerPlayer player;
    @Shadow protected ServerLevel level;

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/item/ItemStack;)V"))
    private void injectAfterBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local(name = "adjustedState") BlockState adjustedState) {
        if (player.isInvisible()) {
            return;
        }

        // sense villagers
        int searchDistance = level.getGameRules().get(UnionizedVillagers.VIEW_RANGE);
        List<Villager> villagers = EntitySensing.getEntities(level, EntitySensing.VILLAGER_FILTER, pos, searchDistance, Predicates.alwaysTrue());

        boolean isJobSite = false;

        // check if breaking own workstation
        for (Villager villager : villagers) {
            Optional<GlobalPos> jobOpt = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);

            if (jobOpt.isPresent() && jobOpt.get().dimension() == level.dimension() && jobOpt.get().pos().equals(pos)) {
                if (villager.getSensing().hasLineOfSight(player)) {
                    UnionizedVillagers.emitTrigger(level, player, villager, villager, StrikeTriggers.BREAKING_OWN_WORKSPACE);
                    return;
                } else {
                    isJobSite = true;
                }
            }
        }

        for (Villager villager : villagers) {
            // check if breaking other's workstation
            if (isJobSite && villager.getSensing().hasLineOfSight(player)) {
                UnionizedVillagers.emitTrigger(level, player, villager, villager, StrikeTriggers.BREAKING_WORKSPACE);
                return;
            }

            // check if breaking own possession
            Optional<ResourceKey<@NotNull VillagerProfession>> key = villager.getVillagerData().profession().unwrapKey();
            if (key.isPresent()) {
                TagKey<@NotNull Block> tag = TagKey.create(Registries.BLOCK, UnionizedVillagersImpl.id(key.get().identifier().getPath() + "_possessions"));

                if (adjustedState.is(tag) && villager.getSensing().hasLineOfSight(player)) {
                    UnionizedVillagers.emitTrigger(level, player, villager, villager, StrikeTriggers.BREAKING_POSSESSION);
                    return;
                }
            }
        }
    }
}
