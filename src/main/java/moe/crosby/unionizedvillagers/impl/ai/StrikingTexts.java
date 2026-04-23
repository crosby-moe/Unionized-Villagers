package moe.crosby.unionizedvillagers.impl.ai;

import net.minecraft.util.random.WeightedList;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class StrikingTexts {
    private static final WeightedList<@NotNull String> STRIKING_LINES = WeightedList.of(
        new Weighted<>("unionized-villagers.text.common_striking_1", 10),
        new Weighted<>("unionized-villagers.text.common_striking_2", 10),
        new Weighted<>("unionized-villagers.text.common_striking_3", 10),
        new Weighted<>("unionized-villagers.text.rare_striking_1", 1)
    );

    public static String get(RandomSource random) {
        return STRIKING_LINES.getRandomOrThrow(random);
    }
}
