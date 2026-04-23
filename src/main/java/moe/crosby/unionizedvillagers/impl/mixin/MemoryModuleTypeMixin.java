package moe.crosby.unionizedvillagers.impl.mixin;

import com.mojang.serialization.Codec;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(MemoryModuleType.class)
public class MemoryModuleTypeMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerMemory(CallbackInfo ci) {
        UnionizedVillagersImpl.STRIKE_START_TIME = Registry.register(BuiltInRegistries.MEMORY_MODULE_TYPE, UnionizedVillagersImpl.id("strike_start_time"), new MemoryModuleType<>(Optional.of(Codec.LONG)));
    }
}
