package moe.crosby.unionizedvillagers.api;

import net.minecraft.world.entity.ai.gossip.GossipType;

public enum Severity {
    MINOR(1, GossipType.MINOR_NEGATIVE),
    MAJOR(3, GossipType.MAJOR_NEGATIVE);

    public final int warningLevel;
    public final GossipType gossipType;

    Severity(int warningLevel, GossipType gossipType) {
        this.warningLevel = warningLevel;
        this.gossipType = gossipType;
    }
}
