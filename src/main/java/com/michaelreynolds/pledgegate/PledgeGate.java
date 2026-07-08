package com.michaelreynolds.pledgegate;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class PledgeGate implements ModInitializer {
	public static final String MOD_ID = "pledgegate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static PledgeGateConfig config;
	private static AcceptanceStore store;

	@Override
	public void onInitialize() {
		try {
			reload();
		} catch (IOException e) {
			throw new RuntimeException("PledgeGate failed to load its config", e);
		}

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				DialogManager.onJoin(handler.getPlayer()));

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				DialogManager.clearPending(handler.getPlayer().getUUID()));

		// Timeout enforcement; checking once a second is plenty.
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 20 == 0) {
				DialogManager.onTick(server);
			}
		});

		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) ->
				!isMuted(sender));

		ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) -> {
			ServerPlayer player = source.getPlayer();
			return player == null || !isMuted(player);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				PledgeGateCommands.register(dispatcher));

		LOGGER.info("PledgeGate initialized (mode: {}, {} rule(s))", config.describeMode(), config.rules.size());
	}

	private static boolean isMuted(ServerPlayer player) {
		return config.blockChatWhilePending && DialogManager.isPending(player.getUUID());
	}

	public static void reload() throws IOException {
		Path dir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
		config = PledgeGateConfig.load(dir.resolve("config.json"));
		AcceptanceStore newStore = new AcceptanceStore(dir.resolve("acceptances.json"));
		newStore.load();
		store = newStore;
	}

	public static PledgeGateConfig config() {
		return config;
	}

	public static AcceptanceStore store() {
		return store;
	}
}
