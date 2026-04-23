package moe.crosby.unionizedvillagers.impl.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import moe.crosby.unionizedvillagers.impl.IServerPlayer;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.Collection;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

public class StrikeTrackerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("strike_tracker")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))

            .then(literal("get")
                .executes(context -> get(context.getSource(), context.getSource().getPlayerOrException()))
                .then(argument("targets", EntityArgument.player())
                    .executes(context -> get(context.getSource(), EntityArgument.getPlayer(context, "targets")))
                )
            )

            .then(literal("set")
                .then(argument("warning_level", IntegerArgumentType.integer(0, 3))
                    .executes(context -> set(context.getSource(), ImmutableList.of(context.getSource().getPlayerOrException()), IntegerArgumentType.getInteger(context, "warning_level")))
                )

                .then(argument("targets", EntityArgument.players())
                    .then(argument("warning_level", IntegerArgumentType.integer(0, 3))
                        .executes(context -> set(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "warning_level")))
                    )
                )
            )

            .then(literal("clear")
                .executes(context -> clear(context.getSource(), ImmutableList.of(context.getSource().getPlayerOrException())))
                .then(argument("targets", EntityArgument.players())
                    .executes(context -> clear(context.getSource(), EntityArgument.getPlayers(context, "targets")))
                )
            )
        );
    }

    private static int get(CommandSourceStack source, ServerPlayer player) {
        int warningLevel = ((IServerPlayer) player).unionized$getWarningManager().getWarningLevel();

        source.sendSuccess(() -> Component.translatable("unionized-villagers.commands.strike_tracker.get", player.getDisplayName(), warningLevel), true);

        return warningLevel;
    }

    private static int set(CommandSourceStack source, Collection<ServerPlayer> players, int warningLevel) {
        for (ServerPlayer player : players) {
            ((IServerPlayer) player).unionized$getWarningManager().setWarningLevel(warningLevel);
        }

        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable("unionized-villagers.commands.strike_tracker.set.single", players.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("unionized-villagers.commands.strike_tracker.set.multiple", players.size()), true);
        }

        return players.size();
    }

    private static int clear(CommandSourceStack source, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            ((IServerPlayer) player).unionized$getWarningManager().reset();
        }

        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable("unionized-villagers.commands.strike_tracker.clear.single", players.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("unionized-villagers.commands.strike_tracker.clear.multiple", players.size()), true);
        }

        return players.size();
    }
}
