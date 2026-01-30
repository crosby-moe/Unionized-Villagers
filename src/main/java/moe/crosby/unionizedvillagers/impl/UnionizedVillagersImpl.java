package moe.crosby.unionizedvillagers.impl;

import com.google.common.base.Predicates;
import moe.crosby.unionizedvillagers.api.UnionizedVillagers;
import moe.crosby.unionizedvillagers.api.VillagerNeeds;
import moe.crosby.unionizedvillagers.impl.mixin.VillagerEntityInvoker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
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

    @Override
    public void onInitialize() {
        VillagerNeeds.initialize();

        try {
            MethodHandles.lookup().ensureInitialized(UnionizedVillagers.class);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, attacker, victim) -> {
            if (attacker instanceof PlayerEntity player && !player.isInvisible() && !victim.isInvisible()) {
                // sense villagers
                int searchDistance = 32;
                Box searchBox = new Box(victim.getBlockPos()).expand(searchDistance);
                List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, Predicates.alwaysTrue());

                for (VillagerEntity villager : villagers) {
                    TagKey<EntityType<?>> tag = TagKey.of(RegistryKeys.ENTITY_TYPE, UnionizedVillagersImpl.id(villager.getVillagerData().getProfession().id() + "_possessions"));

                    if (victim.getType().isIn(tag) && villager.canSee(victim)) {
                        world.sendEntityStatus(villager, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
                        ((VillagerEntityInvoker) villager).unionized$sayNo();
                        villager.getGossip().startGossip(player.getUuid(), VillageGossipType.MINOR_NEGATIVE, 25);
                    }
                }
            }
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

    public static void sendDebug(World world, Text debugText) {
        for (ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList()) {
            player.sendMessage(debugText);
        }
    }
}
