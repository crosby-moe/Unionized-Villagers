package moe.crosby.unionizedvillagers.api;

import moe.crosby.unionizedvillagers.impl.UnionizedVillagersImpl;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class VillagerNeed {
    public final Identifier identifier;
    public final int priority;

    protected VillagerNeed(Identifier identifier, int priority) {
        this.identifier = identifier;
        this.priority = priority;
    }

    public abstract boolean isMet(ServerLevel world, Villager Villager, Player playerEntity);

    protected void debug(Villager Villager, boolean met, @Nullable Supplier<String> extra) {
        if (Villager.level() instanceof ServerLevel serverWorld && serverWorld.getGameRules().get(UnionizedVillagers.DEBUG)) {
            @Nullable String extraString = extra == null ? "" : extra.get();
            UnionizedVillagersImpl.sendDebug(serverWorld, UnionizedVillagersImpl.of(Villager)
                .append("Need '" + this.identifier + "' is ")
                .append(Component.literal(met ? "met" : "unmet").withStyle(met ? ChatFormatting.GREEN : ChatFormatting.RED))
                .append(extraString == null ? "" : ", " + extraString));
        }
    }

    public String getTranslationKey() {
        return this.identifier.toLanguageKey("villager-need", "description");
    }
}
