package moe.crosby.unionizedvillagers.impl;

import moe.crosby.unionizedvillagers.api.*;
import moe.crosby.unionizedvillagers.impl.ai.VillagerPossessions;
import moe.crosby.unionizedvillagers.impl.commands.StrikeTrackerCommand;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import moe.crosby.unionizedvillagers.impl.mixin.RandomizableContainerBlockEntityAccessor;
import moe.crosby.unionizedvillagers.impl.mixin.VillagerInvoker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UnionizedVillagersImpl implements ModInitializer {
    public static final int STRIKE_RANGE = 320;
    public static final String MODID = "unionized-villagers";
    public static Activity STRIKE;
    public static MemoryModuleType<@NotNull Long> STRIKE_START_TIME;

    @Override
    public void onInitialize() {
        VillagerNeeds.initialize();

        UnionizedVillagers.DEBUG = GameRuleBuilder.forBoolean(false).category(GameRuleCategory.MOBS).buildAndRegister(UnionizedVillagersImpl.id("debug_unionized_villagers"));
        UnionizedVillagers.VIEW_RANGE = GameRuleBuilder.forInteger(32).range(1, 258).category(GameRuleCategory.MOBS).buildAndRegister(UnionizedVillagersImpl.id("villager_view_range"));
        UnionizedVillagers.SEE_MONSTERS_THROUGH_WALLS = GameRuleBuilder.forBoolean(false).category(GameRuleCategory.MOBS).buildAndRegister(UnionizedVillagersImpl.id("villager_see_monsters_through_walls"));
        UnionizedVillagers.ROOM_SIZE = GameRuleBuilder.forInteger(9).range(0, 64).category(GameRuleCategory.MOBS).buildAndRegister(UnionizedVillagersImpl.id("villager_room_minimum_size"));

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, attacker, victim, source) -> {
            if (attacker instanceof ServerPlayer player && !player.isInvisible() && !victim.isInvisible()) {
                boolean isGuardian = victim.is(UnionizedVillagers.GUARDIANS_ENTITY_TAG);

                // sense villagers
                int searchDistance = world.getGameRules().get(UnionizedVillagers.VIEW_RANGE);
                EntitySensing.forEach(world, EntitySensing.VILLAGER_FILTER, victim.blockPosition(), searchDistance, villager -> {
                    // killing guardian
                    if (isGuardian && villager.getSensing().hasLineOfSight(player)) {
                        UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.KILLING_GUARDIAN);
                        return AbortableIterationConsumer.Continuation.ABORT;
                    }

                    // killing possession
                    Optional<ResourceKey<@NotNull VillagerProfession>> key = villager.getVillagerData().profession().unwrapKey();
                    if (key.isPresent()) {
                        TagKey<@NotNull EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, UnionizedVillagersImpl.id(key.get().identifier().getPath() + "_possessions"));

                        if (victim.is(tag) && villager.getSensing().hasLineOfSight(player)) {
                            UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.KILLING_POSSESSION);
                            return AbortableIterationConsumer.Continuation.ABORT;
                        }
                    }

                    // killing villager
                    if (victim instanceof Villager) {
                        UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.KILLING_VILLAGER);
                        return AbortableIterationConsumer.Continuation.ABORT;
                    }

                    return AbortableIterationConsumer.Continuation.CONTINUE;
                });
            }
        });

        // todo implement other triggers:
        // - breaking structure

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.level() instanceof ServerLevel world && source.getEntity() instanceof ServerPlayer player && EntitySensing.isVisible(player)) {
                if (entity.is(UnionizedVillagers.GUARDIANS_ENTITY_TAG) && EntitySensing.isVisible(entity)) {
                    // sense villagers
                    int searchDistance = world.getGameRules().get(UnionizedVillagers.VIEW_RANGE);
                    @Nullable Villager witness = EntitySensing.getFirst(world, EntitySensing.VILLAGER_FILTER, entity.blockPosition(), searchDistance, villager -> villager.getSensing().hasLineOfSight(player));
                    if (witness != null) {
                        UnionizedVillagers.emitTrigger(world, player, witness, witness, StrikeTriggers.ATTACKING_GUARDIAN);
                        return true;
                    }
                }

                if (entity instanceof Villager villager) {
                    UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.HARMING_VILLAGER);
                    return true;
                }
            }

            // i dislike that there's no after damage event, but oh well
            return true;
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world instanceof ServerLevel serverWorld && player instanceof ServerPlayer serverPlayer && blockEntity instanceof ChestBlockEntity chest && EntitySensing.isVisible(serverPlayer)) {
                @Nullable ResourceKey<@NotNull LootTable> lootTable = ((RandomizableContainerBlockEntityAccessor) chest).unionized$getLootTable();
                if (lootTable != null && lootTable.identifier().getPath().startsWith("chests/village/")) {
                    int searchDistance = serverWorld.getGameRules().get(UnionizedVillagers.VIEW_RANGE);
                    EntitySensing.forEach(world, EntitySensing.VILLAGER_FILTER, chest.getBlockPos(), searchDistance, villager -> {
                        VillagerData data = villager.getVillagerData();

                        if (VillagerPossessions.isVillagerPossession(data, lootTable.identifier()) && villager.getSensing().hasLineOfSight(player)) {
                            UnionizedVillagers.emitTrigger(serverWorld, serverPlayer, villager, villager, StrikeTriggers.STEALING_POSSESSION);
                        }

                        return AbortableIterationConsumer.Continuation.CONTINUE;
                    });
                }
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StrikeTrackerCommand.register(dispatcher);
        });
    }

    public static Identifier id(String path) {
        return Identifier.tryBuild(MODID, path);
    }

    public static MutableComponent of(Villager entity) {
        return Component.literal("[").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
            .append(Component.empty().setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)).append(entity.getDisplayName()))
            .append("] ");
    }

    public static void emitTriggers(ServerLevel world, Collection<ServerPlayer> criminals, Entity victim, Villager witness, StrikeTrigger trigger) {
        boolean causesStrike = false;

        for (ServerPlayer criminal : criminals) {
            VillagerStrikeWarningManager strikeWarningManager = ((IServerPlayer) criminal).unionized$getWarningManager();
            if (strikeWarningManager.increaseWarningLevel(trigger.severity().warningLevel)) {
                // %1$s -> victim
                // %2$s -> witness
                // %3$s -> criminal
                MutableComponent feedback = Component.translatable(trigger.translationKey(), victim.getDisplayName(), witness.getDisplayName(), criminal.getDisplayName());

                criminal.sendSystemMessage(Component.empty().withStyle(ChatFormatting.YELLOW).append(feedback));

                if (victim instanceof Villager villager) {
                    world.broadcastEntityEvent(villager, EntityEvent.VILLAGER_ANGRY);
                    ((VillagerInvoker) villager).unionized$setUnhappy();
                    villager.getGossips().add(criminal.getUUID(), trigger.severity().gossipType, 25);
                }

                if (witness != victim) {
                    world.broadcastEntityEvent(witness, EntityEvent.VILLAGER_ANGRY);
                    ((VillagerInvoker) witness).unionized$setUnhappy();
                    witness.getGossips().add(criminal.getUUID(), trigger.severity().gossipType, 25);
                }

                causesStrike |= strikeWarningManager.getWarningLevel() >= 3;
            }
        }

        if (causesStrike) {
            beginStrike(world, witness.position());
        }
    }

    public static boolean beginStrike(ServerLevel world, Vec3 center) {
        // sense villagers
        int searchDistance = Math.max(world.getGameRules().get(UnionizedVillagers.VIEW_RANGE), STRIKE_RANGE);
        BlockPos centerPos = BlockPos.containing(center);

        MutableBoolean success = new MutableBoolean(false);

        // start strike
        EntitySensing.forEach(world, EntitySensing.VILLAGER_FILTER, centerPos, searchDistance, villager -> {
            villager.getBrain().setMemory(STRIKE_START_TIME, world.getGameTime());
            villager.getBrain().setActiveActivityIfPossible(STRIKE);

            ((VillagerInvoker) villager).unionized$setUnhappy();

            success.setTrue();

            return AbortableIterationConsumer.Continuation.CONTINUE;
        });

        if (success.booleanValue()) {
            // play sound effect
            EntitySensing.forEach(world, EntitySensing.PLAYER_FILTER, centerPos, searchDistance + 16, player -> {
                double distance = Math.sqrt(player.distanceToSqr(center));
                double x = player.getX() + 13.0 / distance * (center.x() - player.getX());
                double z = player.getZ() + 13.0 / distance * (center.z() - player.getZ());

                if (distance <= searchDistance + 16) {
                    player.connection.send(new ClientboundSoundPacket(
                        SoundEvents.RAID_HORN,
                        SoundSource.NEUTRAL,
                        x, player.getY(), z,
                        64f,
                        1f,
                        world.getRandom().nextLong()
                    ));
                }

                return AbortableIterationConsumer.Continuation.CONTINUE;
            });

            return true;
        } else {
            return false;
        }
    }

    public static void endStrike(Villager villager) {
        villager.getBrain().eraseMemory(UnionizedVillagersImpl.STRIKE_START_TIME);
        villager.getBrain().useDefaultActivity();
        ((IVillager) villager).unionized$endStrike();
    }

    public static void sendDebug(ServerLevel world, MutableComponent debugText) {
        for (ServerPlayer player : world.getServer().getPlayerList().getPlayers()) {
            if (Commands.LEVEL_GAMEMASTERS.check(player.permissions())) {
                player.sendSystemMessage(debugText);
            }
        }
    }
}
