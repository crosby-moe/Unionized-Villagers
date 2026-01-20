package moe.crosby.unionizedvillagers.api;

import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class UnionizedVillagers {
    public static final TagKey<EntityType<?>> GUARDIAN_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id("guardians"));
    public static final TagKey<StatusEffect> POISONS_TAG = TagKey.of(RegistryKeys.STATUS_EFFECT, UnionizedVillagersImpl.id("poisons"));
}
