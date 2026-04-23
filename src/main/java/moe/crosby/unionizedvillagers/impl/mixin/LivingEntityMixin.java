package moe.crosby.unionizedvillagers.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.crosby.unionizedvillagers.impl.fast.CachingRaycastFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    /**
     * Replace expensive raycast with faster one
     * @see CachingRaycastFunction
     */
    @WrapOperation(method = "hasLineOfSight(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/ClipContext$Block;Lnet/minecraft/world/level/ClipContext$Fluid;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"))
    private BlockHitResult replaceRaycast(Level world, ClipContext context, Operation<BlockHitResult> original, @Local(argsOnly = true) ClipContext.Block blockCollidingContext, @Local(argsOnly = true) ClipContext.Fluid fluidCollidingContext) {
        if (blockCollidingContext == ClipContext.Block.COLLIDER && fluidCollidingContext == ClipContext.Fluid.NONE) {
            return CachingRaycastFunction.Collision.raycast(world, context);
        } else {
            return original.call(world, context);
        }
    }
}
