package moe.crosby.unionizedvillagers.api;

import com.google.common.collect.ImmutableList;
import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRule;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class UnionizedVillagers {
    public static GameRule<@NotNull Boolean> DEBUG;
    public static GameRule<@NotNull Integer> VIEW_RANGE;
    public static GameRule<@NotNull Boolean> SEE_MONSTERS_THROUGH_WALLS;
    public static GameRule<@NotNull Integer> ROOM_SIZE;

    public static final TagKey<@NotNull EntityType<?>> GUARDIANS_ENTITY_TAG = TagKey.create(Registries.ENTITY_TYPE, UnionizedVillagersImpl.id("guardians"));
    public static final TagKey<@NotNull MobEffect> POISONS_TAG = TagKey.create(Registries.MOB_EFFECT, UnionizedVillagersImpl.id("poisons"));
    public static final TagKey<@NotNull Block> HAZARDS_TAG = TagKey.create(Registries.BLOCK, UnionizedVillagersImpl.id("hazards"));

    /**
     * @param world the world
     * @param criminal the player who caused the trigger
     * @param victim the victim villager (not necessarily witness)
     * @param witness the villager who witness the trigger (not necessarily victim)
     * @param trigger the trigger
     */
    public static void emitTrigger(ServerLevel world, ServerPlayer criminal, Entity victim, Villager witness, StrikeTrigger trigger) {
        emitTriggers(world, ImmutableList.of(criminal), victim, witness, trigger);
    }

    /**
     * @param world the world
     * @param criminals the players who caused, or did not prevent the trigger
     * @param victim the victim villager (not necessarily witness)
     * @param witness the villager who witness the trigger (not necessarily victim)
     * @param trigger the trigger
     */
    public static void emitTriggers(ServerLevel world, Collection<ServerPlayer> criminals, Entity victim, Villager witness, StrikeTrigger trigger) {
        UnionizedVillagersImpl.emitTriggers(world, criminals, victim, witness, trigger);
    }
}
