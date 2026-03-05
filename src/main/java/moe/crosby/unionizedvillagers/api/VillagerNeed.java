package moe.crosby.unionizedvillagers.api;

import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class VillagerNeed {
    public final Identifier identifier;
    public final int priority;

    protected VillagerNeed(Identifier identifier, int priority) {
        this.identifier = identifier;
        this.priority = priority;
    }

    public abstract boolean isMet(World world, VillagerEntity villagerEntity, PlayerEntity playerEntity);

    protected void debug(VillagerEntity villagerEntity, boolean met, @Nullable Supplier<String> extra) {
        if (villagerEntity.getWorld().getGameRules().getBoolean(UnionizedVillagers.DEBUG)) {
            @Nullable String extraString = extra == null ? "" : extra.get();
            UnionizedVillagersImpl.sendDebug(villagerEntity.getWorld(), UnionizedVillagersImpl.of(villagerEntity)
                .append("Need '" + this.identifier + "' is ")
                .append(Text.literal(met ? "met" : "unmet").formatted(met ? Formatting.GREEN : Formatting.RED))
                .append(extraString == null ? "" : ", " + extraString));
        }
    }

    public String getTranslationKey() {
        return this.identifier.toTranslationKey("villager-need", "description");
    }
}
