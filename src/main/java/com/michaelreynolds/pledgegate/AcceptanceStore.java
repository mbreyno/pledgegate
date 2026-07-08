package com.michaelreynolds.pledgegate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Records which players accepted which version of the rules, persisted to
 * config/pledgegate/acceptances.json. Admins can delete the file (or single
 * entries) to force re-acceptance.
 */
public class AcceptanceStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type MAP_TYPE = new TypeToken<Map<UUID, Acceptance>>() {}.getType();

	public record Acceptance(String name, long acceptedAt, String rulesHash) {}

	private final Path file;
	private final Map<UUID, Acceptance> acceptances = new HashMap<>();

	public AcceptanceStore(Path file) {
		this.file = file;
	}

	public synchronized void load() throws IOException {
		acceptances.clear();
		if (Files.exists(file)) {
			Map<UUID, Acceptance> loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), MAP_TYPE);
			if (loaded != null) {
				acceptances.putAll(loaded);
			}
		}
	}

	public synchronized boolean needsPrompt(UUID playerId, PledgeGateConfig config) {
		Acceptance acceptance = acceptances.get(playerId);
		if (acceptance == null) {
			return true;
		}
		if (config.repromptWhenRulesChange && !config.rulesHash().equals(acceptance.rulesHash())) {
			return true;
		}
		return config.isIntervalMode()
				&& System.currentTimeMillis() - acceptance.acceptedAt() > config.intervalMillis();
	}

	public synchronized void recordAcceptance(UUID playerId, String playerName, String rulesHash) {
		acceptances.put(playerId, new Acceptance(playerName, System.currentTimeMillis(), rulesHash));
		save();
	}

	public synchronized boolean reset(UUID playerId) {
		boolean removed = acceptances.remove(playerId) != null;
		if (removed) {
			save();
		}
		return removed;
	}

	public synchronized int resetAll() {
		int count = acceptances.size();
		acceptances.clear();
		save();
		return count;
	}

	private void save() {
		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(acceptances, MAP_TYPE), StandardCharsets.UTF_8);
		} catch (IOException e) {
			PledgeGate.LOGGER.error("Failed to save acceptances to {}", file, e);
		}
	}
}
