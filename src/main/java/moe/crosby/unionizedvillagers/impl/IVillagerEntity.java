package moe.crosby.unionizedvillagers.impl;

import net.minecraft.village.TradeOfferList;

public interface IVillagerEntity {
    void unionized$endStrike();
    boolean unionized$isInStrike();
    TradeOfferList unionized$getStrikeOffers();

    void unionized$triggerMonsterNoise();
    boolean unionized$heardMonsterNoise();
}
