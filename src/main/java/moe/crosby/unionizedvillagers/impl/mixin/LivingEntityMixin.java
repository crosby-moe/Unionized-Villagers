package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.impl.fast.CachingRaycastFunction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Unique private static final BlockHitResult MISS = BlockHitResult.createMissed(null, null, null);

    /**
     * Replace expensive raycast with faster one
     * @see CachingRaycastFunction
     */
    @Redirect(method = "canSee", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;raycast(Lnet/minecraft/world/RaycastContext;)Lnet/minecraft/util/hit/BlockHitResult;"))
    private BlockHitResult replaceRaycast(World world, RaycastContext context) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, new CachingRaycastFunction(world, context), ctx -> MISS);
    }
}
