package moe.crosby.unionizedvillagers.impl;

import com.google.common.base.Predicates;
import moe.crosby.unionizedvillagers.api.*;
import moe.crosby.unionizedvillagers.impl.commands.StrikeTrackerCommand;
import moe.crosby.unionizedvillagers.impl.mixin.VillagerEntityInvoker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.village.VillageGossipType;
import net.minecraft.world.World;

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
                Box searchBox = new Box(victim.getBlockPos()).expand(searchDistance);
                List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, Predicates.alwaysTrue());

                for (VillagerEntity villager : villagers) {
                    if (isGuardian && villager.getVisibilityCache().canSee(player)) {
                        UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.KILLING_GUARDIAN);
                        return;
                    }

                    TagKey<EntityType<?>> tag = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id(villager.getVillagerData().getProfession().id() + "_possessions"));

                    if (victim.getType().isIn(tag) && villager.getVisibilityCache().canSee(player)) {
                        UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.KILLING_POSSESSION);
                        return;
                    }
                }
            }
        });

        // todo implement other triggers:
        // - breaking structure

        // todo use villager memories to prevent chat spam when killling golem
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld() instanceof ServerWorld world && source.getAttacker() instanceof ServerPlayerEntity player && !player.isInvisible()) {
                if (entity.getType().isIn(UnionizedVillagers.GUARDIANS_ENTITY_TAG) && !entity.isInvisible()) {
                    // todo make sensing into util method with optimizations
                    // sense villagers
                    int searchDistance = world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE);
                    Box searchBox = new Box(entity.getBlockPos()).expand(searchDistance);
                    List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, Predicates.alwaysTrue());

                    for (VillagerEntity villager : villagers) {
                        if (villager.getVisibilityCache().canSee(player)) {
                            UnionizedVillagers.emitTrigger(world, player, villager, villager, StrikeTriggers.ATTACKING_GUARDIAN);
                            return true;
                        }
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

    public static void emitTriggers(ServerWorld world, Collection<ServerPlayerEntity> criminals, VillagerEntity victim, VillagerEntity witness, StrikeTrigger trigger) {
        boolean causesStrike = false;

        for (ServerPlayerEntity criminal : criminals) {
            VillagerStrikeWarningManager strikeWarningManager = ((IServerPlayerEntity) criminal).unionized$getWarningManager();
            if (strikeWarningManager.increaseWarningLevel(trigger.severity().warningLevel)) {
                // %1$s -> victim
                // %2$s -> witness
                // %3$s -> criminal
                Text feedback = Text.translatable(trigger.translationKey(), victim.getDisplayName(), witness.getDisplayName(), criminal.getDisplayName());

                criminal.sendMessage(Text.empty().formatted(Formatting.YELLOW).append(feedback));

                world.sendEntityStatus(victim, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
                ((VillagerEntityInvoker) victim).unionized$sayNo();
                victim.getGossip().startGossip(criminal.getUuid(), VillageGossipType.MINOR_NEGATIVE, 25);

                causesStrike |= strikeWarningManager.getWarningLevel() >= 3;
            }
        }

        if (causesStrike) {
            beginStrike(world, victim);
        }
    }

    private static void beginStrike(ServerWorld world, VillagerEntity leader) {
        // sense villagers
        int searchDistance = Math.max(world.getGameRules().getInt(UnionizedVillagers.VIEW_RANGE), 48);
        Box searchBox = new Box(leader.getBlockPos()).expand(searchDistance);
        List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, Predicates.alwaysTrue());

        for (VillagerEntity villager : villagers) {
            villager.getBrain().remember(STRIKE_START_TIME, world.getTime());
            villager.getBrain().doExclusively(STRIKE);
        }
    }

    public static void sendDebug(World world, Text debugText) {
        for (ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList()) {
            player.sendMessage(debugText);
        }
    }
}
