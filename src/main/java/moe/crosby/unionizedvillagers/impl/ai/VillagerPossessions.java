package moe.crosby.unionizedvillagers.impl.ai;

import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;

public class VillagerPossessions {
    public static boolean isVillagerPossession(VillagerData data, Identifier lootTableId) {
        // villager type
        if (lootTableId.getPath().contains(data.getType().toString())) {
            return true;
        }

        // villager profession
        VillagerProfession profession = data.getProfession();
        if (lootTableId.getPath().contains(profession.id())) {
            return true;
        }

        // special cases
        if (profession == VillagerProfession.CLERIC && lootTableId.getPath().contains("temple")) {
            return true;
        } else if (profession == VillagerProfession.LEATHERWORKER && lootTableId.getPath().contains("tannery")) {
            return true;
        }

        return false;
    }
}
