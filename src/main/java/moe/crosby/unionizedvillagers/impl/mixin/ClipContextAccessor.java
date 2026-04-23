package moe.crosby.unionizedvillagers.impl.mixin;

import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.ClipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClipContext.class)
public interface ClipContextAccessor {
    @Accessor("collisionContext")
    CollisionContext unionized$getCollisionContext();
}
