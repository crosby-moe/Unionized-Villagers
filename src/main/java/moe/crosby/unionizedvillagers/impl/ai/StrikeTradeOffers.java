package moe.crosby.unionizedvillagers.impl.ai;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;

public class StrikeTradeOffers {
    private static final String KEY = "unionized-villagers.text.end_strike";

    public static final TradeOffers.Factory[] OFFERS = {
        create(Items.GOLDEN_APPLE, 1),
        create(Items.DIAMOND, 4),
        create(Items.HEART_OF_THE_SEA, 1),
        create(Items.ANCIENT_DEBRIS, 1),
        create(Items.OBSIDIAN, 8)
    };

    public static final ItemStack END_STRIKE_STACK;

    static {
        ItemStack endStrikeStack = Items.EMERALD.getDefaultStack();
        endStrikeStack.setCustomName(Text.translatable(KEY).formatted(Formatting.GREEN));
        END_STRIKE_STACK = endStrikeStack;
    }

    private static TradeOffers.Factory create(Item item, int count) {
        return (entity, random) -> {
            ItemStack stack = item.getDefaultStack();
            stack.setCount(count);
            return new TradeOffer(stack, END_STRIKE_STACK, 1, 0, 0);
        };
    }
}
