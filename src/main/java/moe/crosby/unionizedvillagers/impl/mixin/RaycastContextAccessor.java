package moe.crosby.unionizedvillagers.impl.mixin;

import net.minecraft.block.ShapeContext;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RaycastContext.class)
public interface RaycastContextAccessor {
    @Accessor("entityPosition")
    ShapeContext unionized$getShapeContext();
}
