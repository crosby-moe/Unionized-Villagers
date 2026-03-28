package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.impl.IServerPlayerEntity;
import moe.crosby.unionizedvillagers.impl.VillagerStrikeWarningManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements IServerPlayerEntity {
    @Unique private static final String KEY = "unionized$villager_strike_tracker";
    @Unique private VillagerStrikeWarningManager villagerStrikeWarningManager = new VillagerStrikeWarningManager();

    @Inject(method = "readCustomData", at = @At("RETURN"))
    private void deserializeManager(ReadView view, CallbackInfo ci) {
        this.villagerStrikeWarningManager = view.read(KEY, VillagerStrikeWarningManager.CODEC).orElseGet(VillagerStrikeWarningManager::new);
    }

    @Inject(method = "writeCustomData", at = @At("RETURN"))
    private void serializeManager(WriteView view, CallbackInfo ci) {
        view.put(KEY, VillagerStrikeWarningManager.CODEC, this.villagerStrikeWarningManager);
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
