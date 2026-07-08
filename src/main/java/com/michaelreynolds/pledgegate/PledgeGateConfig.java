package com.michaelreynolds.pledgegate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Loaded from config/pledgegate/config.json. All fields are admin-editable;
 * missing fields fall back to the defaults below.
 */
public class PledgeGateConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public String title = "Server Rules";
	public List<String> rules = List.of(
			"&e1. &fPlease treat all players respectfully. No bullying, personal insults, hate speech, or verbal abuse.",
			"&e2. &fPlease keep your language clean and family-friendly in chat and/or voice chat.",
			"&e3. &fNo cheating, hacking, or exploiting bugs.",
			"&e4. &fNo advertising other servers."
	);

	/** "once" = accept a single time; "interval" = must re-accept every intervalDays. */
	public String displayMode = "once";
	/** Used when displayMode is "interval". Supports fractions (0.5 = every 12 hours). */
	public double intervalDays = 7.0;
	/** Re-prompt everyone when the rules text changes, even in "once" mode. */
	public boolean repromptWhenRulesChange = true;

	/** Kick players who neither agree nor decline within this many seconds. 0 disables. */
	public int agreeTimeoutSeconds = 300;
	/** Block chat and commands from players who have not agreed yet. */
	public boolean blockChatWhilePending = true;

	public String checkboxLabel = "I have read and agree to the rules";
	public String agreeButtonLabel = "Enter Server";
	public String declineButtonLabel = "I Do Not Agree";
	public String mustCheckWarning = "You must check the box below to agree to the rules!";
	public String kickMessage = "You must agree to the server rules to play here.";
	public String timeoutKickMessage = "You did not respond to the rules prompt in time.";
	/** Sent in chat after agreeing. Empty string disables. */
	public String welcomeMessage = "&aThanks for agreeing to the rules. Have fun!";

	private transient String rulesHash;

	public static PledgeGateConfig load(Path file) throws IOException {
		PledgeGateConfig config;
		if (Files.exists(file)) {
			config = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), PledgeGateConfig.class);
			if (config == null) {
				config = new PledgeGateConfig();
			}
		} else {
			config = new PledgeGateConfig();
		}
		// Rewrite so newly added fields show up in the file for admins to discover.
		Files.createDirectories(file.getParent());
		Files.writeString(file, GSON.toJson(config), StandardCharsets.UTF_8);
		config.rulesHash = config.computeRulesHash();
		return config;
	}

	public boolean isIntervalMode() {
		return "interval".equalsIgnoreCase(displayMode.trim());
	}

	public long intervalMillis() {
		return (long) (intervalDays * 24.0 * 60.0 * 60.0 * 1000.0);
	}

	public String rulesHash() {
		return rulesHash;
	}

	private String computeRulesHash() {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(title.getBytes(StandardCharsets.UTF_8));
			for (String rule : rules) {
				digest.update((byte) 0);
				digest.update(rule.getBytes(StandardCharsets.UTF_8));
			}
			return HexFormat.of().formatHex(digest.digest(), 0, 8);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/** Translates '&' color codes to the '§' formatting codes Minecraft renders. */
	public static String colorize(String text) {
		StringBuilder out = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '&' && i + 1 < text.length()
					&& "0123456789abcdefklmnorABCDEFKLMNOR".indexOf(text.charAt(i + 1)) >= 0) {
				out.append('§').append(Character.toLowerCase(text.charAt(i + 1)));
				i++;
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}

	public String describeMode() {
		return isIntervalMode()
				? String.format(Locale.ROOT, "interval (every %s days)", intervalDays)
				: "once";
	}
}
