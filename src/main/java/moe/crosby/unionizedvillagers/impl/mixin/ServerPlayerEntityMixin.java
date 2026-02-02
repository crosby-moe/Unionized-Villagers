package moe.crosby.unionizedvillagers.impl.mixin;

import com.mojang.serialization.Dynamic;
import moe.crosby.unionizedvillagers.impl.IServerPlayerEntity;
import moe.crosby.unionizedvillagers.impl.VillagerStrikeWarningManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements IServerPlayerEntity {
    @Shadow @Final private static Logger LOGGER;

    @Unique private static final String KEY = "unionized$villager_strike_tracker";
    @Unique private VillagerStrikeWarningManager villagerStrikeWarningManager = new VillagerStrikeWarningManager(0, 0, 0);

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void deserializeManager(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(KEY, NbtElement.COMPOUND_TYPE)) {
            VillagerStrikeWarningManager.CODEC
                .parse(new Dynamic<>(NbtOps.INSTANCE, nbt.get(KEY)))
                .resultOrPartial(LOGGER::error)
                .ifPresent(manager -> this.villagerStrikeWarningManager = manager);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void serializeManager(NbtCompound nbt, CallbackInfo ci) {
        VillagerStrikeWarningManager.CODEC
            .encodeStart(NbtOps.INSTANCE, this.villagerStrikeWarningManager)
            .resultOrPartial(LOGGER::error)
            .ifPresent(encoded -> nbt.put(KEY, encoded));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tickManager(CallbackInfo ci) {
        this.villagerStrikeWarningManager.tick();
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.villagerStrikeWarningManager.copy(((IServerPlayerEntity) oldPlayer).unionized$getWarningManager());
    }

    @Override
    public VillagerStrikeWarningManager unionized$getWarningManager() {
        return this.villagerStrikeWarningManager;
    }
}
