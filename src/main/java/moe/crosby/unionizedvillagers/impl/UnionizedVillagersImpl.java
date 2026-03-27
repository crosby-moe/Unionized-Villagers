package moe.crosby.unionizedvillagers.impl;

import moe.crosby.unionizedvillagers.api.*;
import moe.crosby.unionizedvillagers.impl.ai.VillagerPossessions;
import moe.crosby.unionizedvillagers.impl.commands.StrikeTrackerCommand;
import moe.crosby.unionizedvillagers.impl.fast.EntitySensing;
import moe.crosby.unionizedvillagers.impl.mixin.LootableContainerBlockEntityAccessor;
import moe.crosby.unionizedvillagers.impl.mixin.VillagerEntityInvoker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.village.VillageGossipType;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class UnionizedVillagersImpl implements ModInitializer {
    public static final String MODID = "unionized-villagers";
    public static Activity STRIKE;
    public static MemoryModuleType<Long> STRIKE_START_TIME;

    @Override
    public void onInitialize() {
        VillagerNeeds.initialize();

        try {
            MethodHandles.lookup().ensureInitialized(UnionizedVillagers.class);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, attacker, victim) -> {
            if (attacker instanceof ServerPlayerEntity player && !player.isInvisible() && !victim.isInvisible()) {
                boolean isGuardian = victim.getType().isIn(UnionizedVillagers.GUARDIANS_ENTITY_TAG);

                // sense villagers
                int searchDistance = world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE);
                EntitySensing.forEach(world, EntitySensing.VILLAGER_FILTER, victim.getBlockPos(), searchDistance, villager -> {
                    if (isGuardian && villager.getVisibilityCache().canSee(player)) {
                        UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.KILLING_GUARDIAN);
                        return LazyIterationConsumer.NextIteration.ABORT;
                    }

                    TagKey<EntityType<?>> tag = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id(villager.getVillagerData().getProfession().id() + "_possessions"));

                    if (victim.getType().isIn(tag) && villager.getVisibilityCache().canSee(player)) {
                        UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.KILLING_POSSESSION);
                        return LazyIterationConsumer.NextIteration.ABORT;
                    }

                    return LazyIterationConsumer.NextIteration.CONTINUE;
                });
            }
        });

        // todo implement other triggers:
        // - breaking structure

        // todo use villager memories to prevent chat spam when killling golem
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld() instanceof ServerWorld world && source.getAttacker() instanceof ServerPlayerEntity player && EntitySensing.isVisible(player)) {
                if (entity.getType().isIn(UnionizedVillagers.GUARDIANS_ENTITY_TAG) && EntitySensing.isVisible(entity)) {
                    // sense villagers
                    int searchDistance = world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE);
                    @Nullable VillagerEntity witness = EntitySensing.getFirst(world, EntitySensing.VILLAGER_FILTER, entity.getBlockPos(), searchDistance, villager -> villager.getVisibilityCache().canSee(player));
                    if (witness != null) {
                        UnionizedVillagers.emitTrigger(world, player, witness, witness, StrikeTriggers.ATTACKING_GUARDIAN);
                        return true;
                    }
                }

                if (entity instanceof VillagerEntity villager) {
                    UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.HARMING_VILLAGER);
                    return true;
                }
            }

            // i dislike that there's no after damage event, but oh well
            return true;
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world instanceof ServerWorld serverWorld && player instanceof ServerPlayerEntity serverPlayer && blockEntity instanceof ChestBlockEntity chest && EntitySensing.isVisible(serverPlayer)) {
                @Nullable Identifier lootTableId = ((LootableContainerBlockEntityAccessor) chest).unionized$getLootTableId();
                if (lootTableId != null && lootTableId.getPath().startsWith("chests/village/")) {
                    int searchDistance = world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE);
                    EntitySensing.forEach(world, EntitySensing.VILLAGER_FILTER, chest.getPos(), searchDistance, villager -> {
                        VillagerData data = villager.getVillagerData();

                        if (VillagerPossessions.isVillagerPossession(data, lootTableId) && villager.getVisibilityCache().canSee(player)) {
                            UnionizedVillagers.emitTrigger(serverWorld, serverPlayer, villager, villager, StrikeTriggers.STEALING_POSSESSION);
                        }

                        return LazyIterationConsumer.NextIteration.CONTINUE;
                    });
                }
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StrikeTrackerCommand.register(dispatcher);
        });
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }

    public static MutableText of(Entity entity) {
        return Text.literal("[").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
            .append(Text.empty().setStyle(Style.EMPTY.withColor(Formatting.WHITE)).append(entity.getDisplayName()))
            .append("] ");
    }

    public static void emitTriggers(ServerWorld world, Collection<ServerPlayerEntity> criminals, Entity victim, VillagerEntity witness, StrikeTrigger trigger) {
        boolean causesStrike = false;

        for (ServerPlayerEntity criminal : criminals) {
            VillagerStrikeWarningManager strikeWarningManager = ((IServerPlayerEntity) criminal).unionized$getWarningManager();
            if (strikeWarningManager.increaseWarningLevel(trigger.severity().warningLevel)) {
                // %1$s -> victim
                // %2$s -> witness
                // %3$s -> criminal
                Text feedback = Text.translatable(trigger.translationKey(), victim.getDisplayName(), witness.getDisplayName(), criminal.getDisplayName());

                criminal.sendMessage(Text.empty().formatted(Formatting.YELLOW).append(feedback));

                if (victim instanceof VillagerEntity villager) {
                    world.sendEntityStatus(villager, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
                    ((VillagerEntityInvoker) villager).unionized$sayNo();
                    villager.getGossip().startGossip(criminal.getUuid(), VillageGossipType.MINOR_NEGATIVE, 25);
                }

                world.sendEntityStatus(witness, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
                ((VillagerEntityInvoker) witness).unionized$sayNo();
                witness.getGossip().startGossip(criminal.getUuid(), VillageGossipType.MINOR_NEGATIVE, 25);

                causesStrike |= strikeWarningManager.getWarningLevel() >= 3;
            }
        }

        if (causesStrike) {
            beginStrike(world, witness);
        }
    }

    private static void beginStrike(ServerWorld world, VillagerEntity leader) {
        // sense villagers
        int searchDistance = Math.max(world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE), 48);

        // start strike
        EntitySensing.forEach(world, EntitySensing.VILLAGER_FILTER, leader.getBlockPos(), searchDistance, villager -> {
            villager.getBrain().remember(STRIKE_START_TIME, world.getTime());
            villager.getBrain().doExclusively(STRIKE);

            return LazyIterationConsumer.NextIteration.CONTINUE;
        });

        // play sound effect
        EntitySensing.forEach(world, EntitySensing.PLAYER_FILTER, leader.getBlockPos(), searchDistance + 16, player -> {
            double distance = player.distanceTo(leader);
            double x = player.getX() + 13.0 / distance * (leader.getX() - player.getX());
            double z = player.getZ() + 13.0 / distance * (leader.getZ() - player.getZ());

            if (distance <= searchDistance + 16) {
                player.networkHandler.sendPacket(new PlaySoundS2CPacket(
                    SoundEvents.EVENT_RAID_HORN,
                    SoundCategory.NEUTRAL,
                    x, player.getY(), z,
                    64f,
                    1f,
                    world.getRandom().nextLong()
                ));
            }

            return LazyIterationConsumer.NextIteration.CONTINUE;
        });
    }

    public static void sendDebug(World world, Text debugText) {
        for (ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList()) {
            player.sendMessage(debugText);
        }
    }
}
