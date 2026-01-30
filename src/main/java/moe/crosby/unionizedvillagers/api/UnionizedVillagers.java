package moe.crosby.unionizedvillagers.api;

import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.GameRules;

public class UnionizedVillagers {
    public static final GameRules.Key<GameRules.BooleanRule> DEBUG = GameRuleRegistry.register("debugUnionizedVillagers", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(false));

    public static final TagKey<EntityType<?>> GUARDIAN_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("guardians"));
    public static final TagKey<StatusEffect> POISONS_TAG = TagKey.of(RegistryKeys.STATUS_EFFECT, UnionizedVillagersImpl.id("poisons"));
    public static final TagKey<Block> HAZARDS_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("hazards"));

    // Villager profession block possessions
    public static final TagKey<Block> ARMORER_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("armorer_possessions"));
    public static final TagKey<Block> BUTCHER_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("butcher_possessions"));
    public static final TagKey<Block> CARTOGRAPHER_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("cartographer_possessions"));
    public static final TagKey<Block> CLERIC_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("cleric_possessions"));
    public static final TagKey<Block> FARMER_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("farmer_possessions"));
    public static final TagKey<Block> FISHERMAN_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("fisherman_possessions"));
    public static final TagKey<Block> FLETCHER_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("fletcher_possessions"));
    public static final TagKey<Block> LEATHERWORKER_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("leatherworker_possessions"));
    public static final TagKey<Block> LIBRARIAN_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("librarian_possessions"));
    public static final TagKey<Block> MASON_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("mason_possessions"));
    public static final TagKey<Block> SHEPHERD_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("shepherd_possessions"));
    public static final TagKey<Block> TOOLSMITH_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("toolsmith_possessions"));
    public static final TagKey<Block> WEAPONSMITH_POSSESSIONS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, UnionizedVillagersImpl.id("weaponsmith_possessions"));

    // Villager profession entity possessions
    public static final TagKey<EntityType<?>> ARMORER_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("armorer_possessions"));
    public static final TagKey<EntityType<?>> BUTCHER_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("butcher_possessions"));
    public static final TagKey<EntityType<?>> CARTOGRAPHER_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("cartographer_possessions"));
    public static final TagKey<EntityType<?>> CLERIC_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("cleric_possessions"));
    public static final TagKey<EntityType<?>> FARMER_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("farmer_possessions"));
    public static final TagKey<EntityType<?>> FISHERMAN_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("fisherman_possessions"));
    public static final TagKey<EntityType<?>> FLETCHER_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("fletcher_possessions"));
    public static final TagKey<EntityType<?>> LEATHERWORKER_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("leatherworker_possessions"));
    public static final TagKey<EntityType<?>> LIBRARIAN_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("librarian_possessions"));
    public static final TagKey<EntityType<?>> MASON_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("mason_possessions"));
    public static final TagKey<EntityType<?>> SHEPHERD_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("shepherd_possessions"));
    public static final TagKey<EntityType<?>> TOOLSMITH_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("toolsmith_possessions"));
    public static final TagKey<EntityType<?>> WEAPONSMITH_POSSESSIONS_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("weaponsmith_possessions"));
}
