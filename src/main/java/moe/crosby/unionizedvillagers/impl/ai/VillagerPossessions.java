package moe.crosby.unionizedvillagers.impl.ai;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

import java.util.Optional;

public class VillagerPossessions {
    public static boolean isVillagerPossession(VillagerData data, Identifier lootTableId) {
        // villager type
        Optional<RegistryKey<VillagerType>> typeKey = data.type().getKey();
        if (typeKey.isPresent() && lootTableId.getPath().contains(typeKey.get().getValue().getPath())) {
            return true;
        }

        // villager profession
        Optional<RegistryKey<VillagerProfession>> professionKey = data.profession().getKey();
        if (professionKey.isPresent()) {
            if (lootTableId.getPath().contains(professionKey.get().getValue().getPath())) {
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
