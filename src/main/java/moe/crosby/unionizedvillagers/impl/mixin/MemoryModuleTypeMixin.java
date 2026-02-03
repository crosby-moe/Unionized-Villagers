package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(MemoryModuleType.class)
public class MemoryModuleTypeMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerMemory(CallbackInfo ci) {
        UnionizedVillagersImpl.STRIKE_START_TIME = Registry.register(Registries.MEMORY_MODULE_TYPE, UnionizedVillagersImpl.id("strike_start_time"), new MemoryModuleType<>(Optional.empty()));
    }
}
