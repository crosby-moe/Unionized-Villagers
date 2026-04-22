package moe.crosby.unionizedvillagers.api;

import com.google.common.collect.ImmutableList;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;

import java.util.Collection;

public class UnionizedVillagers {
    public static final GameRules.Key<GameRules.BooleanRule> DEBUG = GameRuleRegistry.register("unionized$debugVillagers", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.IntRule> VIEW_RANGE = GameRuleRegistry.register("unionized$villagerViewRange", GameRules.Category.MOBS, GameRuleFactory.createIntRule(32, 1, 256));
    public static final GameRules.Key<GameRules.BooleanRule> SEE_MONSTERS_THROUGH_WALLS = GameRuleRegistry.register("unionized$villagerSeeMonsterThroughWalls", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.IntRule> ROOM_SIZE = GameRuleRegistry.register("unionized$villagerRoomMinimumSize", GameRules.Category.MOBS, GameRuleFactory.createIntRule(9, 0, 64));

    public static final TagKey<EntityType<?>> GUARDIANS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("guardians"));
    public static final TagKey<StatusEffect> POISONS_TAG = TagKey.of(RegistryKeys.STATUS_EFFECT, UnionizedVillagersImpl.id("poisons"));
    public static final TagKey<Block> HAZARDS_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("hazards"));

    /**
     * @param world the world
     * @param criminal the player who caused the trigger
     * @param victim the victim villager (not necessarily witness)
     * @param witness the villager who witness the trigger (not necessarily victim)
     * @param trigger the trigger
     */
    public static void emitTrigger(ServerWorld world, ServerPlayerEntity criminal, Entity victim, VillagerEntity witness, StrikeTrigger trigger) {
        emitTriggers(world, ImmutableList.of(criminal), victim, witness, trigger);
    }

    /**
     * @param world the world
     * @param criminals the players who caused, or did not prevent the trigger
     * @param victim the victim villager (not necessarily witness)
     * @param witness the villager who witness the trigger (not necessarily victim)
     * @param trigger the trigger
     */
    public static void emitTriggers(ServerWorld world, Collection<ServerPlayerEntity> criminals, Entity victim, VillagerEntity witness, StrikeTrigger trigger) {
        UnionizedVillagersImpl.emitTriggers(world, criminals, victim, witness, trigger);
    }
}
