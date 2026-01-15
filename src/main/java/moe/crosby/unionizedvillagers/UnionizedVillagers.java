package moe.crosby.unionizedvillagers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class UnionizedVillagers implements ModInitializer {
    public static final String MODID = "unionized-villagers";
    public static final GameRules.Key<GameRules.IntRule> LIVING_SPACE = GameRuleRegistry.register("villagerLivingSpace", GameRules.Category.MOBS, GameRuleFactory.createIntRule(16, 0));

    @Override
    public void onInitialize() {}
}
