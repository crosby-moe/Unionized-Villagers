package moe.crosby.unionizedvillagers.impl.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Pair;
import moe.crosby.unionizedvillagers.api.*;
import moe.crosby.unionizedvillagers.impl.IVillagerEntity;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import moe.crosby.unionizedvillagers.impl.ai.StrikeTaskList;
import moe.crosby.unionizedvillagers.impl.ai.StrikeTradeOffers;
import moe.crosby.unionizedvillagers.impl.ai.StrikingTexts;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.OptionalInt;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity implements IVillagerEntity {
    @Unique private static final int RECHECK_DELAY_TICKS = 4;
    @Unique private int tickDelay;
    @Unique private @Nullable TradeOfferList savedStrikeTrades;
    @Unique private int lastHeardMonsterNoise = Integer.MAX_VALUE;

    @Shadow protected abstract void sayNo();
    @Shadow public abstract VillagerData getVillagerData();

    @Shadow
    public abstract Brain<VillagerEntity> getBrain();

    private VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "beginTradeWith", at = @At("HEAD"), cancellable = true)
    private void uh(PlayerEntity customer, CallbackInfo ci) {
        if (shouldCancel(customer)) {
            ci.cancel();
            sayNo();
        }

        tickDelay = RECHECK_DELAY_TICKS;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (offers == null || getCustomer() == null) return;

        if (tickDelay-- > 0) return;
        tickDelay = RECHECK_DELAY_TICKS;

        if (getCustomer() instanceof ServerPlayerEntity serverPlayerEntity && shouldCancel(getCustomer())) {
            serverPlayerEntity.closeHandledScreen();
            sayNo();
        }
    }

    @Unique
    private boolean shouldCancel(PlayerEntity customer) {
        if (this.unionized$isInStrike()) { // can always trade during a strike
            return false;
        }

        VillagerEntity villagerEntity = (VillagerEntity) (Object) this;

        boolean shouldDebug = getWorld().getGameRules().getBoolean(UnionizedVillagers.DEBUG);
        boolean shouldCancel = false;

        for (VillagerNeed need : VillagerNeeds.VILLAGER_NEEDS) {
            boolean isMet = need.isMet(getWorld(), villagerEntity, customer);

            shouldCancel |= !isMet;

            if (!shouldDebug && !isMet) {
                customer.sendMessage(UnionizedVillagersImpl.of(villagerEntity)
                        .append(Text.translatable(need.getTranslationKey())));

                break;
            }
        }

        return shouldCancel;
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        if (!force && (entity instanceof BoatEntity || entity instanceof MinecartEntity) && getWorld() instanceof ServerWorld world) {
            // sense villagers
            int searchDistance = world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE);

            List<ServerPlayerEntity> players = EntitySensing.getEntities(world, EntitySensing.PLAYER_FILTER, entity.getBlockPos(), searchDistance, player -> EntitySensing.isVisible(player) && this.getVisibilityCache().canSee(player));

            VillagerEntity villager = (VillagerEntity) (Object) this;
            UnionizedVillagers.emitTriggers(world, players, villager, villager, StrikeTriggers.KIDNAPPING);
        }

        return super.startRiding(entity, force);
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"))
    private static ImmutableList<MemoryModuleType<?>> addMemoryModule(ImmutableList<MemoryModuleType<?>> original) {
        return ImmutableList.<MemoryModuleType<?>>builder().addAll(original)
            .add(UnionizedVillagersImpl.STRIKE_START_TIME)
            .build();
    }

    @Inject(method = "initBrain", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/Brain;setCoreActivities(Ljava/util/Set;)V"))
    private void registerActivityTasks(Brain<VillagerEntity> brain, CallbackInfo ci) {
        brain.setTaskList(
            UnionizedVillagersImpl.STRIKE,
            StrikeTaskList.createStrikeTasks(this.getVillagerData().getProfession(), 0.5f),
            ImmutableSet.of(Pair.of(UnionizedVillagersImpl.STRIKE_START_TIME, MemoryModuleState.VALUE_PRESENT))
        );
    }

    @Override
    public void unionized$triggerMonsterNoise() {
        this.lastHeardMonsterNoise = this.age;
    }

    @Override
    public boolean unionized$heardMonsterNoise() {
        return this.lastHeardMonsterNoise + 500 < this.age;
    }

    // Handle trade serialization

    @Unique
    private static final String KEY = "unionized$StrikeOffers";

    @Override
    public void unionized$endStrike() {
        this.savedStrikeTrades = null;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        @Nullable TradeOfferList savedOffers = this.savedStrikeTrades;
        if (savedOffers != null && !savedOffers.isEmpty()) {
            nbt.put(KEY, savedOffers.toNbt());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(KEY, NbtElement.COMPOUND_TYPE)) {
            this.savedStrikeTrades = new TradeOfferList(nbt.getCompound(KEY));
        }
    }

    // Modify trades on strike

    @Override
    public boolean unionized$isInStrike() {
        return this.getBrain().hasActivity(UnionizedVillagersImpl.STRIKE) && this.getBrain().getOptionalRegisteredMemory(UnionizedVillagersImpl.STRIKE_START_TIME).isPresent();
    }

    @Unique
    private TradeOfferList getStrikeOffers() {
        if (this.savedStrikeTrades != null) {
            return this.savedStrikeTrades;
        }

        TradeOfferList tradeOffers = new TradeOfferList();
        this.fillRecipesFromPool(tradeOffers, StrikeTradeOffers.OFFERS, 1);
        return this.savedStrikeTrades = tradeOffers;
    }

    @Override
    public TradeOfferList getOffers() {
        if (this.unionized$isInStrike()) {
            return this.getStrikeOffers();
        } else {
            return super.getOffers();
        }
    }

    @Override
    public void sendOffers(PlayerEntity player, Text test, int levelProgress) {
        if (this.unionized$isInStrike()) {
            // copies super
            OptionalInt optionalInt = player.openHandledScreen(
                new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> new MerchantScreenHandler(syncId, playerInventory, this), test)
            );
            if (optionalInt.isPresent()) {
                TradeOfferList tradeOfferList = this.getStrikeOffers();
                if (!tradeOfferList.isEmpty()) {
                    player.sendTradeOffers(optionalInt.getAsInt(), tradeOfferList, levelProgress, this.getExperience(), this.isLeveledMerchant(), this.canRefreshTrades());
                }
            }

            player.sendMessage(UnionizedVillagersImpl.of(this).append(Text.translatable(StrikingTexts.get(this.getRandom()))));
        } else {
            super.sendOffers(player, test, levelProgress);
        }
    }
}
