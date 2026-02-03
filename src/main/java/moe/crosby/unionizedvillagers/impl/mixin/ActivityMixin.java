package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.entity.ai.brain.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Activity.class)
public abstract class ActivityMixin {
    @Shadow
    private static Activity register(String id) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerActivity(CallbackInfo ci) {
        UnionizedVillagersImpl.STRIKE = register("unionized_strike");
    }
}
