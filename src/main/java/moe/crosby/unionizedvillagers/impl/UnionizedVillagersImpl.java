package moe.crosby.unionizedvillagers.impl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class UnionizedVillagersImpl implements ModInitializer {
    public static final String MODID = "unionized-villagers";
    public static final boolean DEBUG = FabricLoader.getInstance().isDevelopmentEnvironment() || Boolean.getBoolean("unionized-villagers.debug");

    @Override
    public void onInitialize() {
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }

    public static MutableText of(Entity entity) {
        return Text.literal("[").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
            .append(Text.empty().setStyle(Style.EMPTY.withColor(Formatting.WHITE)).append(entity.getDisplayName()))
            .append("] ");
    }

    public static void sendDebug(World world, Text debugText) {
        for (ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList()) {
            player.sendMessage(debugText);
        }
    }
}
