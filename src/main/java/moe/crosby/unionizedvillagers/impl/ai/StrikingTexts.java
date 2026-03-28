package moe.crosby.unionizedvillagers.impl.ai;

import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.random.Random;

public class StrikingTexts {
    private static final Pool<String> STRIKING_LINES = Pool.of(
        new Weighted<>("unionized-villagers.text.common_striking_1", 10),
        new Weighted<>("unionized-villagers.text.common_striking_2", 10),
        new Weighted<>("unionized-villagers.text.common_striking_3", 10),
        new Weighted<>("unionized-villagers.text.rare_striking_1", 1)
    );

    public static String get(Random random) {
        return STRIKING_LINES.getOrEmpty(random).orElseThrow();
    }
}
