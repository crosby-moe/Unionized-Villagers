package moe.crosby.unionizedvillagers.impl.ai;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VillagerPossessions {
    public static boolean isVillagerPossession(VillagerData data, Identifier lootTableId) {
        // villager type
        Optional<ResourceKey<@NotNull VillagerType>> typeKey = data.type().unwrapKey();
        if (typeKey.isPresent() && lootTableId.getPath().contains(typeKey.get().identifier().getPath())) {
            return true;
        }

        // villager profession
        Optional<ResourceKey<@NotNull VillagerProfession>> professionKey = data.profession().unwrapKey();
        if (professionKey.isPresent()) {
            if (lootTableId.getPath().contains(professionKey.get().identifier().getPath())) {
                return true;
            }

            // special cases
            if (professionKey.get() == VillagerProfession.CLERIC && lootTableId.getPath().contains("temple")) {
                return true;
            } else if (professionKey.get() == VillagerProfession.LEATHERWORKER && lootTableId.getPath().contains("tannery")) {
                return true;
            }
        }


        return false;
    }
}
