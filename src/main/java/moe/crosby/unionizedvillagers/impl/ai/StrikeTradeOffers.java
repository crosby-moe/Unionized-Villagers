package moe.crosby.unionizedvillagers.impl.ai;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;

import java.util.Objects;

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

    public static boolean isEndStrikeStack(ItemStack stack) {
        return stack.isOf(END_STRIKE_STACK.getItem()) && stack.getCount() == END_STRIKE_STACK.getCount() && stack.hasNbt()
            ? (stack.getNbt() != null ? Objects.equals(stack.getNbt(), END_STRIKE_STACK.getNbt()) : END_STRIKE_STACK.getNbt() == null)
            : stack.hasNbt() == END_STRIKE_STACK.hasNbt();
    }

    private static TradeOffers.Factory create(Item item, int count) {
        return (entity, random) -> {
            ItemStack stack = item.getDefaultStack();
            stack.setCount(count);
            return new TradeOffer(stack, END_STRIKE_STACK, 1, 0, 0);
        };
    }
}
