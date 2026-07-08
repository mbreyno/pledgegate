# PledgeGate

A server-side Fabric mod for Minecraft **26.1.2 – 26.2** that requires players to read and agree to your server rules before they can play.

- **Players don't install anything.** PledgeGate uses Minecraft's built-in dialog screens, so completely vanilla clients see the rules popup with a real checkbox and buttons.
- Players must tick **"I have read and agree to the rules"** and click **Enter Server**. Clicking **I Do Not Agree** (or ignoring the prompt) kicks them.
- Show the rules **once ever**, or **periodically** (every few days, weekly — whatever you choose).
- Players are automatically re-prompted whenever you **change the rules** (optional).
- While the prompt is open the player can't chat or run commands (optional), can't close the dialog with Escape, and is kicked after a timeout if they never respond.

## Installation

1. Install the [Fabric server launcher](https://fabricmc.net/use/server/) for Minecraft 26.1.2 or 26.2.
2. Drop [Fabric API](https://modrinth.com/mod/fabric-api) (matching your Minecraft version) and `pledgegate-1.1.1.jar` into the server's `mods/` folder.
3. Start the server once. PledgeGate creates `config/pledgegate/config.json` with example rules.
4. Edit the config, then run `/pledgegate reload` (or restart the server).

## Configuration — `config/pledgegate/config.json`

```json
{
  "title": "Server Rules",
  "rules": [
    "&e1. &fPlease treat all players respectfully. No bullying, personal insults, hate speech, or verbal abuse.",
    "&e2. &fPlease keep your language clean and family-friendly in chat and/or voice chat.",
    "&e3. &fNo cheating, hacking, or exploiting bugs.",
    "&e4. &fNo advertising other servers."
  ],
  "displayMode": "once",
  "intervalDays": 7.0,
  "repromptWhenRulesChange": true,
  "agreeTimeoutSeconds": 300,
  "blockChatWhilePending": true,
  "checkboxLabel": "I have read and agree to the rules",
  "agreeButtonLabel": "Enter Server",
  "declineButtonLabel": "I Do Not Agree",
  "mustCheckWarning": "You must check the box below to agree to the rules!",
  "kickMessage": "You must agree to the server rules to play here.",
  "timeoutKickMessage": "You did not respond to the rules prompt in time.",
  "welcomeMessage": "&aThanks for agreeing to the rules. Have fun!"
}
```

| Setting | What it does |
| --- | --- |
| `title` | Heading shown at the top of the rules screen. |
| `rules` | One entry per line of the rules. Supports `&` color codes (`&c` red, `&e` yellow, `&l` bold, `&f` white, ...). |
| `displayMode` | `"once"` — players agree a single time. `"interval"` — players must agree again every `intervalDays`. |
| `intervalDays` | How often the rules pop up in `interval` mode. Fractions work: `0.5` = every 12 hours, `3` = every 3 days, `7` = weekly. |
| `repromptWhenRulesChange` | If `true`, everyone must re-agree whenever you edit `title` or `rules` — even in `once` mode. |
| `agreeTimeoutSeconds` | Kick players who neither agree nor decline within this many seconds. `0` disables the timeout. |
| `blockChatWhilePending` | If `true`, players can't chat or run commands until they've agreed. |
| `welcomeMessage` | Chat message sent after agreeing. Set to `""` to disable. |

## Commands (require operator / permission level 2)

| Command | Effect |
| --- | --- |
| `/pledgegate reload` | Reloads the config without restarting. If the rules changed, players will re-agree on their next join. |
| `/pledgegate preview` | Shows you the rules dialog without affecting your acceptance (buttons just close it). |
| `/pledgegate reset all` | Wipes everyone's acceptance; online players are re-prompted immediately. |
| `/pledgegate reset <player>` | Wipes one player's acceptance; if online, they're re-prompted immediately. |

Acceptances are stored in `config/pledgegate/acceptances.json` (player UUID, name, timestamp, and which version of the rules they accepted). You can delete that file to reset everyone.

## Building from source

```
JAVA_HOME=<path to JDK 25> ./gradlew build
```

The jar lands in `build/libs/`. This repo includes a project-local JDK setup under `.jdk/` (gitignored).

## Notes

- The mod is server-side only. It also loads harmlessly in single-player, where it does nothing meaningful (you're the operator of your own world).
- Requires Fabric Loader ≥ 0.19.3, Fabric API, and Java 25 (the standard requirements for Minecraft 26.1.2+ servers).
- One jar covers Minecraft 26.1.2 through 26.2 — the mod is compiled against 26.2, and every API it touches is binary-identical in 26.1.2 (verified member-by-member, and smoke-tested on real dedicated servers of both versions).
