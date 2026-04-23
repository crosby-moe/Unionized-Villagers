package moe.crosby.unionizedvillagers.impl.ai;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.ItemCost;

import java.util.List;
import java.util.function.Supplier;

public class StrikeTradeOffers {
    private static final String KEY = "unionized-villagers.text.end_strike";

    public static final List<Supplier<MerchantOffer>> OFFERS = List.of(
        create(Items.GOLDEN_APPLE, 1),
        create(Items.DIAMOND, 4),
        create(Items.HEART_OF_THE_SEA, 1),
        create(Items.ANCIENT_DEBRIS, 1),
        create(Items.OBSIDIAN, 8)
    );

    private static final ItemStackTemplate END_STRIKE_STACK_TEMPLATE = new ItemStackTemplate(
        Items.EMERALD,
        DataComponentPatch.builder()
            .set(DataComponents.CUSTOM_NAME, Component.translatable(KEY).withStyle(ChatFormatting.GREEN))
            .build()
    );

    public static boolean isEndStrikeStack(ItemStack stack) {
        return stack.is(END_STRIKE_STACK_TEMPLATE.item()) && stack.count() == END_STRIKE_STACK_TEMPLATE.count() && stack.getComponentsPatch().equals(END_STRIKE_STACK_TEMPLATE.components());
    }

    private static Supplier<MerchantOffer> create(Item item, int count) {
        return () -> new MerchantOffer(new ItemCost(item, count), END_STRIKE_STACK_TEMPLATE.create(), 1, 0, 0);
    }
}
