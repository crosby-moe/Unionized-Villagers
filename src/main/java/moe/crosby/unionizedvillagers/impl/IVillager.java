package moe.crosby.unionizedvillagers.impl;

import net.minecraft.world.item.trading.MerchantOffers;

public interface IVillager {
    void unionized$endStrike();
    boolean unionized$isInStrike();
    MerchantOffers unionized$getStrikeOffers();

    void unionized$triggerMonsterNoise();
    boolean unionized$heardMonsterNoise();
}
