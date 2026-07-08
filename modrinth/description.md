# PledgeGate

**Players must read and agree to your server rules before they can play.**

PledgeGate uses Minecraft's built-in dialog system, so completely vanilla clients see a native rules screen — with a real checkbox and buttons — the moment they join. Nothing for your players to install, ever.

## ✅ How it works

- On join, players see your rules in a dialog they can't dismiss with Escape
- They must tick **"I have read and agree to the rules"** and click **Enter Server**
- Clicking **I Do Not Agree** kicks them with a message you control
- Ignoring the prompt kicks them after a timeout (5 minutes by default)
- Until they agree, they can't chat or run commands

## ✨ Features

- **100% server-side** — works with vanilla clients on the standard launcher
- **Flexible scheduling** — show the rules once ever, or re-prompt every few days / weekly / any interval (`intervalDays` supports fractions)
- **Re-agree on rule changes** — edit your rules and everyone sees the new version on their next join (optional)
- **Every word customizable** — rules, buttons, kick messages, welcome message, with `&` color-code support
- **Live reload** — `/pledgegate reload` applies config changes with no restart
- **Acceptance records** — who agreed, when, and to which version of the rules, stored in a plain JSON file

## 🔧 Setup

1. Drop the jar (plus [Fabric API](https://modrinth.com/mod/fabric-api)) into your server's `mods/` folder
2. Start the server once — `config/pledgegate/config.json` is generated with example rules
3. Edit your rules, then run `/pledgegate reload`

## 📋 Admin commands (permission level 2)

| Command | Effect |
| --- | --- |
| `/pledgegate reload` | Reload the config without restarting |
| `/pledgegate preview` | See the dialog exactly as players will |
| `/pledgegate reset all` | Everyone must agree again |
| `/pledgegate reset <player>` | One player must agree again |

## 📖 Full documentation

Complete config reference and editing guide: **[pledgegate.com](https://pledgegate.com)**
