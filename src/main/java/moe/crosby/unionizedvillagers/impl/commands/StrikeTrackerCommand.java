package moe.crosby.unionizedvillagers.impl.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import moe.crosby.unionizedvillagers.impl.IServerPlayerEntity;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class StrikeTrackerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("strike_tracker")
            .requires(source -> source.hasPermissionLevel(2))

            .then(literal("get")
                .executes(context -> get(context.getSource(), context.getSource().getPlayerOrThrow()))
                .then(argument("targets", EntityArgumentType.player())
                    .executes(context -> get(context.getSource(), EntityArgumentType.getPlayer(context, "targets")))
                )
            )

            .then(literal("set")
                .then(argument("warning_level", IntegerArgumentType.integer(0, 3))
                    .executes(context -> set(context.getSource(), ImmutableList.of(context.getSource().getPlayerOrThrow()), IntegerArgumentType.getInteger(context, "warning_level")))
                )

                .then(argument("targets", EntityArgumentType.players())
                    .then(argument("warning_level", IntegerArgumentType.integer(0, 3))
                        .executes(context -> set(context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "warning_level")))
                    )
                )
            )

            .then(literal("clear")
                .executes(context -> clear(context.getSource(), ImmutableList.of(context.getSource().getPlayerOrThrow())))
                .then(argument("targets", EntityArgumentType.players())
                    .executes(context -> clear(context.getSource(), EntityArgumentType.getPlayers(context, "targets")))
                )
            )
        );
    }

    private static int get(ServerCommandSource source, ServerPlayerEntity player) {
        int warningLevel = ((IServerPlayerEntity) player).unionized$getWarningManager().getWarningLevel();

        source.sendFeedback(() -> Text.translatable("unionized-villagers.commands.strike_tracker.get", player.getDisplayName(), warningLevel), true);

        return warningLevel;
    }

    private static int set(ServerCommandSource source, Collection<ServerPlayerEntity> players, int warningLevel) {
        for (ServerPlayerEntity player : players) {
            ((IServerPlayerEntity) player).unionized$getWarningManager().setWarningLevel(warningLevel);
        }

        if (players.size() == 1) {
            source.sendFeedback(() -> Text.translatable("unionized-villagers.commands.strike_tracker.set.single", players.iterator().next().getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("unionized-villagers.commands.strike_tracker.set.multiple", players.size()), true);
        }

        return players.size();
    }

    private static int clear(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            ((IServerPlayerEntity) player).unionized$getWarningManager().reset();
        }

        if (players.size() == 1) {
            source.sendFeedback(() -> Text.translatable("unionized-villagers.commands.strike_tracker.clear.single", players.iterator().next().getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("unionized-villagers.commands.strike_tracker.clear.multiple", players.size()), true);
        }

        return players.size();
    }
}
