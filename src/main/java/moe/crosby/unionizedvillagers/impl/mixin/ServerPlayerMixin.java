package moe.crosby.unionizedvillagers.impl.mixin;

import moe.crosby.unionizedvillagers.impl.IServerPlayer;
import moe.crosby.unionizedvillagers.impl.VillagerStrikeWarningManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements IServerPlayer {
    @Unique private static final String KEY = "unionized$villager_strike_tracker";
    @Unique private VillagerStrikeWarningManager villagerStrikeWarningManager = new VillagerStrikeWarningManager();

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void deserializeManager(ValueInput input, CallbackInfo ci) {
        this.villagerStrikeWarningManager = input.read(KEY, VillagerStrikeWarningManager.CODEC).orElseGet(VillagerStrikeWarningManager::new);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void serializeManager(ValueOutput output, CallbackInfo ci) {
        output.store(KEY, VillagerStrikeWarningManager.CODEC, this.villagerStrikeWarningManager);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tickManager(CallbackInfo ci) {
        this.villagerStrikeWarningManager.tick();
    }

    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void restoreFrom(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        this.villagerStrikeWarningManager.copy(((IServerPlayer) oldPlayer).unionized$getWarningManager());
    }

    @Override
    public VillagerStrikeWarningManager unionized$getWarningManager() {
        return this.villagerStrikeWarningManager;
    }
}
