package moe.crosby.unionizedvillagers.impl.mixin;

import com.google.common.base.Predicates;
import com.llamalad7.mixinextras.sugar.Local;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.village.VillageGossipType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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
        int searchDistance = 32;
        Box searchBox = new Box(pos).expand(searchDistance);
        List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, Predicates.alwaysTrue());

        for (VillagerEntity villager : villagers) {
            TagKey<Block> tag = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id(villager.getVillagerData().getProfession().id() + "_possessions"));

            if (state.isIn(tag) && villager.canSee(player)) {
                world.sendEntityStatus(villager, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
                ((VillagerEntityInvoker) villager).unionized$sayNo();
                villager.getGossip().startGossip(player.getUuid(), VillageGossipType.MINOR_NEGATIVE, 25);
            }
        }
    }
}
