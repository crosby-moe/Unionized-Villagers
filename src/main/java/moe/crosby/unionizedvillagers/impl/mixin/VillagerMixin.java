package moe.crosby.unionizedvillagers.impl.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import moe.crosby.unionizedvillagers.api.*;
import moe.crosby.unionizedvillagers.impl.IVillager;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import moe.crosby.unionizedvillagers.impl.ai.StrikeTaskList;
import moe.crosby.unionizedvillagers.impl.ai.StrikeTradeOffers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager implements IVillager {
    @Unique private static final int RECHECK_DELAY_TICKS = 4;
    @Unique private int tickDelay;
    @Unique private @Nullable MerchantOffers savedStrikeTrades;
    @Unique private int lastHeardMonsterNoise = Integer.MAX_VALUE;

    @Shadow protected abstract void setUnhappy();
    @Shadow public abstract VillagerData getVillagerData();

    @Shadow
    public abstract @NotNull Brain<@NotNull Villager> getBrain();

    private VillagerMixin(EntityType<? extends @NotNull AbstractVillager> entityType, ServerLevel world) {
        super(entityType, world);
    }

    @Inject(method = "startTrading", at = @At("HEAD"), cancellable = true)
    private void uh(Player player, CallbackInfo ci) {
        if (shouldCancel(player)) {
            ci.cancel();
            setUnhappy();
        }

        tickDelay = RECHECK_DELAY_TICKS;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (offers == null || getTradingPlayer() == null) return;

        if (tickDelay-- > 0) return;
        tickDelay = RECHECK_DELAY_TICKS;

        if (getTradingPlayer() instanceof ServerPlayer ServerPlayer && shouldCancel(getTradingPlayer())) {
            ServerPlayer.closeContainer();
            setUnhappy();
        }
    }

    @Unique
    private boolean shouldCancel(Player customer) {
        if (this.unionized$isInStrike()) { // can always trade during a strike
            return false;
        }

        if (this.level() instanceof ServerLevel serverWorld) {
            Villager Villager = (Villager) (Object) this;

            boolean shouldDebug = serverWorld.getGameRules().get(UnionizedVillagers.DEBUG);
            boolean shouldCancel = false;

            for (VillagerNeed need : VillagerNeeds.VILLAGER_NEEDS) {
                boolean isMet = need.isMet(serverWorld, Villager, customer);

                shouldCancel |= !isMet;

                if (!shouldDebug && !isMet) {
                    customer.sendSystemMessage(UnionizedVillagersImpl.of(Villager)
                        .append(Component.translatable(need.getTranslationKey())));

                    break;
                }
            }

            return shouldCancel;
        } else {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/Brain;provider(Ljava/util/Collection;Lnet/minecraft/world/entity/ai/Brain$ActivitySupplier;)Lnet/minecraft/world/entity/ai/Brain$Provider;"))
    private static <E extends Villager> Brain.Provider<@NotNull E> wrap(Collection<? extends SensorType<? extends @NotNull Sensor<? super E>>> sensorTypes, Brain.ActivitySupplier<@NotNull E> activities, Operation<Brain.Provider<@NotNull E>> original) {
        return Brain.provider(
            ImmutableList.of(UnionizedVillagersImpl.STRIKE_START_TIME),
            sensorTypes,
            activities
        );
    }

    @Inject(method = "lambda$static$0", at = @At("RETURN"))
    private static void registerActivityTasks(Villager body, CallbackInfoReturnable<List<ActivityData<@NotNull Villager>>> cir) {
        cir.getReturnValue().add(ActivityData.create(
            UnionizedVillagersImpl.STRIKE,
            StrikeTaskList.createStrikeTasks(body.getVillagerData().profession(), 0.5f),
            ImmutableSet.of(Pair.of(UnionizedVillagersImpl.STRIKE_START_TIME, MemoryStatus.VALUE_PRESENT))
        ));
    }

    @Override
    public void unionized$triggerMonsterNoise() {
        this.lastHeardMonsterNoise = this.age;
    }

    @Override
    public boolean unionized$heardMonsterNoise() {
        return this.lastHeardMonsterNoise < this.age - 500;
    }

    /**
     * Since strike triggers already give negative gossips, not removing these would make golems prematurely attack
     * players before a strike can begin.
     */
    @WrapOperation(method = "onReputationEventFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/gossip/GossipContainer;add(Ljava/util/UUID;Lnet/minecraft/world/entity/ai/gossip/GossipType;I)V"))
    private void removeVanillaNegativeGossip(GossipContainer instance, UUID target, GossipType type, int value, Operation<Void> original) {
        if (type != GossipType.MINOR_NEGATIVE && type != GossipType.MAJOR_NEGATIVE) {
            original.call(instance, target, type, value);
        }
    }

    /**
     * Allow trading with unjobbed villagers to end a strike
     */
    @WrapOperation(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/villager/Villager;stopTrading()V"))
    private void dontResetIfInStrike(Villager instance, Operation<Void> original) {
        if (!this.unionized$isInStrike()) {
            original.call(instance);
        }
    }

    // Handle trade serialization

    @Unique
    private static final String KEY = "unionized$StrikeOffers";

    @Override
    public void unionized$endStrike() {
        this.savedStrikeTrades = null;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        @Nullable MerchantOffers savedOffers = this.savedStrikeTrades;
        if (savedOffers != null && !savedOffers.isEmpty()) {
            output.store(KEY, MerchantOffers.CODEC, savedOffers);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        this.savedStrikeTrades = input.read(KEY, MerchantOffers.CODEC).orElse(null);
    }

    // Modify trades on strike

    @Override
    public boolean unionized$isInStrike() {
        return this.getBrain().isActive(UnionizedVillagersImpl.STRIKE) && this.getBrain().getMemory(UnionizedVillagersImpl.STRIKE_START_TIME).isPresent();
    }

    @Override
    public MerchantOffers unionized$getStrikeOffers() {
        if (this.savedStrikeTrades != null) {
            return this.savedStrikeTrades;
        }

        MerchantOffers tradeOffers = new MerchantOffers();
        if (this.level() instanceof ServerLevel serverWorld) {
            Supplier<MerchantOffer> factory = StrikeTradeOffers.OFFERS.get(serverWorld.getRandom().nextInt(StrikeTradeOffers.OFFERS.size()));
            tradeOffers.add(factory.get());
        }
        return this.savedStrikeTrades = tradeOffers;
    }
}
