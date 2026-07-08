package com.michaelreynolds.pledgegate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.Collection;

public final class PledgeGateCommands {
	private PledgeGateCommands() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("pledgegate")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("reload")
						.executes(PledgeGateCommands::reload))
				.then(Commands.literal("preview")
						.executes(PledgeGateCommands::preview))
				.then(Commands.literal("reset")
						.then(Commands.literal("all")
								.executes(PledgeGateCommands::resetAll))
						.then(Commands.argument("player", GameProfileArgument.gameProfile())
								.executes(PledgeGateCommands::resetPlayer))));
	}

	private static int reload(CommandContext<CommandSourceStack> context) {
		try {
			PledgeGate.reload();
			PledgeGateConfig config = PledgeGate.config();
			context.getSource().sendSuccess(() -> Component.literal(
					"PledgeGate config reloaded. Mode: " + config.describeMode()
							+ ", " + config.rules.size() + " rule(s)."), true);
			return 1;
		} catch (Exception e) {
			PledgeGate.LOGGER.error("Failed to reload PledgeGate config", e);
			context.getSource().sendFailure(Component.literal("Reload failed: " + e.getMessage()));
			return 0;
		}
	}

	private static int preview(CommandContext<CommandSourceStack> context) {
		ServerPlayer player = context.getSource().getPlayer();
		if (player == null) {
			context.getSource().sendFailure(Component.literal("Only players can preview the rules dialog."));
			return 0;
		}
		DialogManager.preview(player);
		return 1;
	}

	private static int resetAll(CommandContext<CommandSourceStack> context) {
		int count = PledgeGate.store().resetAll();
		promptOnlinePlayers(context.getSource());
		context.getSource().sendSuccess(() -> Component.literal(
				"Cleared " + count + " rule acceptance(s). Online players must agree again."), true);
		return count;
	}

	private static int resetPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Collection<NameAndId> profiles = GameProfileArgument.getGameProfiles(context, "player");
		int count = 0;
		for (NameAndId profile : profiles) {
			PledgeGate.store().reset(profile.id());
			count++;
			ServerPlayer online = context.getSource().getServer().getPlayerList().getPlayer(profile.id());
			if (online != null) {
				DialogManager.onJoin(online);
			}
			String name = profile.name();
			context.getSource().sendSuccess(() -> Component.literal(
					"Cleared rule acceptance for " + name + "."), true);
		}
		return count;
	}

	private static void promptOnlinePlayers(CommandSourceStack source) {
		for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
			DialogManager.onJoin(player);
		}
	}
}
