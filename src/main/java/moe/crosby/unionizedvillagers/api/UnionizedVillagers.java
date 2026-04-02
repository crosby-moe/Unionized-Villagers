package moe.crosby.unionizedvillagers.api;

import com.google.common.collect.ImmutableList;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.rule.GameRule;

import java.util.Collection;

public class UnionizedVillagers {
    public static GameRule<Boolean> DEBUG;
    public static GameRule<Integer> VIEW_RANGE;
    public static GameRule<Boolean> SEE_MONSTERS_THROUGH_WALLS;
    public static GameRule<Integer> ROOM_SIZE;

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
