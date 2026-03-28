package moe.crosby.unionizedvillagers.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.crosby.unionizedvillagers.impl.fast.CachingRaycastFunction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    /**
     * Replace expensive raycast with faster one
     * @see CachingRaycastFunction
     */
    @WrapOperation(method = "canSee(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/RaycastContext$ShapeType;Lnet/minecraft/world/RaycastContext$FluidHandling;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;raycast(Lnet/minecraft/world/RaycastContext;)Lnet/minecraft/util/hit/BlockHitResult;"))
    private BlockHitResult replaceRaycast(World world, RaycastContext context, Operation<BlockHitResult> original, @Local(argsOnly = true) RaycastContext.ShapeType shapeType, @Local(argsOnly = true) RaycastContext.FluidHandling fluidHandling) {
        if (shapeType == RaycastContext.ShapeType.COLLIDER && fluidHandling == RaycastContext.FluidHandling.NONE) {
            return CachingRaycastFunction.Collision.raycast(world, context);
        } else {
            return original.call(world, context);
        }
    }
}
