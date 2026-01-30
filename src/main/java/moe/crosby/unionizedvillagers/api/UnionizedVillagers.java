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
}
