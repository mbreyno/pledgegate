package com.michaelreynolds.pledgegate;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.dialog.input.BooleanInput;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds the rules dialog, shows it to joining players, and reacts to their
 * button clicks (which arrive via {@code ServerCommonPacketListenerImplMixin}).
 */
public final class DialogManager {
	public static final Identifier ACCEPT_ID = Identifier.fromNamespaceAndPath(PledgeGate.MOD_ID, "accept");
	public static final Identifier DECLINE_ID = Identifier.fromNamespaceAndPath(PledgeGate.MOD_ID, "decline");
	private static final String AGREED_KEY = "agreed";
	private static final int BODY_WIDTH = 300;
	private static final int BUTTON_WIDTH = 200;

	/** Players who must accept before playing, mapped to when they were prompted. */
	private static final Map<UUID, Long> PENDING = new ConcurrentHashMap<>();

	private DialogManager() {}

	public static boolean isPending(UUID playerId) {
		return PENDING.containsKey(playerId);
	}

	public static void clearPending(UUID playerId) {
		PENDING.remove(playerId);
	}

	public static void onJoin(ServerPlayer player) {
		PledgeGateConfig config = PledgeGate.config();
		if (PledgeGate.store().needsPrompt(player.getUUID(), config)) {
			PENDING.put(player.getUUID(), System.currentTimeMillis());
			openRulesDialog(player, false);
		}
	}

	/** Shows the dialog without requiring acceptance (admin preview). */
	public static void preview(ServerPlayer player) {
		openRulesDialog(player, false);
	}

	public static void handleClick(ServerPlayer player, Identifier id, Optional<Tag> payload) {
		boolean pending = isPending(player.getUUID());

		if (DECLINE_ID.equals(id)) {
			if (pending) {
				player.connection.disconnect(colored(PledgeGate.config().kickMessage));
			} else {
				closeDialog(player);
			}
			return;
		}

		if (ACCEPT_ID.equals(id)) {
			if (!pending) {
				closeDialog(player);
				return;
			}
			if (isBoxChecked(payload)) {
				PledgeGateConfig config = PledgeGate.config();
				PledgeGate.store().recordAcceptance(player.getUUID(), player.getScoreboardName(), config.rulesHash());
				PENDING.remove(player.getUUID());
				closeDialog(player);
				if (!config.welcomeMessage.isBlank()) {
					player.sendSystemMessage(colored(config.welcomeMessage));
				}
				PledgeGate.LOGGER.info("{} accepted the server rules", player.getScoreboardName());
			} else {
				openRulesDialog(player, true);
			}
		}
	}

	/** Kicks pending players who ignored the prompt past the configured timeout. */
	public static void onTick(MinecraftServer server) {
		int timeoutSeconds = PledgeGate.config().agreeTimeoutSeconds;
		if (timeoutSeconds <= 0 || PENDING.isEmpty()) {
			return;
		}
		long cutoff = System.currentTimeMillis() - timeoutSeconds * 1000L;
		for (Map.Entry<UUID, Long> entry : PENDING.entrySet()) {
			if (entry.getValue() < cutoff) {
				ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
				if (player != null) {
					player.connection.disconnect(colored(PledgeGate.config().timeoutKickMessage));
				}
				PENDING.remove(entry.getKey());
			}
		}
	}

	private static boolean isBoxChecked(Optional<Tag> payload) {
		return payload.orElse(null) instanceof CompoundTag compound
				&& compound.getBoolean(AGREED_KEY).orElse(false);
	}

	private static void closeDialog(ServerPlayer player) {
		player.connection.send(ClientboundClearDialogPacket.INSTANCE);
	}

	private static void openRulesDialog(ServerPlayer player, boolean withWarning) {
		player.openDialog(Holder.direct(buildDialog(PledgeGate.config(), withWarning)));
	}

	private static Dialog buildDialog(PledgeGateConfig config, boolean withWarning) {
		List<DialogBody> body = new ArrayList<>();
		if (withWarning) {
			body.add(new PlainMessage(
					Component.literal(PledgeGateConfig.colorize(config.mustCheckWarning))
							.withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
					BODY_WIDTH));
			body.add(new PlainMessage(Component.empty(), BODY_WIDTH));
		}
		for (String rule : config.rules) {
			body.add(new PlainMessage(Component.literal(PledgeGateConfig.colorize(rule)), BODY_WIDTH));
		}

		CommonDialogData common = new CommonDialogData(
				Component.literal(PledgeGateConfig.colorize(config.title)),
				Optional.empty(),
				false,               // cannot be closed with Escape
				false,               // don't pause the game
				DialogAction.NONE,   // stay open until the server clears it
				body,
				List.of(new Input(AGREED_KEY, new BooleanInput(
						Component.literal(PledgeGateConfig.colorize(config.checkboxLabel)),
						false, "true", "false")))
		);

		ActionButton agree = new ActionButton(
				new CommonButtonData(Component.literal(PledgeGateConfig.colorize(config.agreeButtonLabel)), BUTTON_WIDTH),
				Optional.of(new CustomAll(ACCEPT_ID, Optional.empty())));
		ActionButton decline = new ActionButton(
				new CommonButtonData(Component.literal(PledgeGateConfig.colorize(config.declineButtonLabel)), BUTTON_WIDTH),
				Optional.of(new CustomAll(DECLINE_ID, Optional.empty())));

		return new MultiActionDialog(common, List.of(agree), Optional.of(decline), 1);
	}

	private static Component colored(String text) {
		return Component.literal(PledgeGateConfig.colorize(text));
	}
}
