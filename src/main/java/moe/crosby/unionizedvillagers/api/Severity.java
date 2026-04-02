package moe.crosby.unionizedvillagers.api;

import net.minecraft.village.VillagerGossipType;

public enum Severity {
    MINOR(1, VillagerGossipType.MINOR_NEGATIVE),
    MAJOR(3, VillagerGossipType.MAJOR_NEGATIVE);

    public int warningLevel;
    public VillagerGossipType gossipType;

    Severity(int warningLevel, VillagerGossipType gossipType) {
        this.warningLevel = warningLevel;
        this.gossipType = gossipType;
    }
}
