# Modrinth Submission Guide

Everything you need to publish PledgeGate on Modrinth. Total time: ~10 minutes, then Modrinth's moderation review (typically 1–2 days for new projects).

## 1. Create the project

Sign in at [modrinth.com](https://modrinth.com) → click the **+** (Create a project).

| Field | Value |
| --- | --- |
| Project type | Mod |
| Name | `PledgeGate` |
| URL / slug | `pledgegate` |
| Summary | `Players must read and agree to your server rules before they can play. Server-side only — vanilla clients see a native rules dialog with a checkbox.` |
| Visibility | Public |

## 2. Project settings

- **Icon**: upload `icon.png` from this folder
- **Categories**: Management, Social, Utility
- **Environment**: Client = **Unsupported**, Server = **Required**
- **License**: MIT
- **Links**: Website → `https://pledgegate.com`, Source → `https://github.com/mbreyno/pledgegate`

## 3. Description

Paste the contents of `description.md` from this folder into the project Description (it's Modrinth-flavored markdown; the preview should show headings, tables, and the Fabric API link).

## 4. Upload the version

Versions → **Create a version**:

| Field | Value |
| --- | --- |
| File | `../build/libs/pledgegate-1.1.1.jar` |
| Version number | `1.1.1` |
| Version title | `PledgeGate 1.1.1` |
| Channel | Release |
| Loaders | Fabric |
| Game versions | 26.1.2, 26.2 |
| Dependencies | Add **Fabric API** as *Required* |

Changelog text you can paste:

```
Initial public release.
- Rules-agreement dialog on join (native UI, no client mod needed)
- Once-only or interval display modes
- Auto re-prompt when rules change
- Kick on decline or timeout; chat/commands blocked until agreed
- /pledgegate reload, preview, and reset commands
```

## 5. Submit for review

Click **Submit for review**. Moderators check new projects (usually within 48 hours); you'll get a notification when it's live. After that, every listing page shows a public download counter, and the project dashboard gives you daily download and page-view analytics.

## After it's live

- Add the Modrinth link to the website (I can add a badge/button to pledgegate.com — just ask)
- Future releases: Versions → Create a version → upload the new jar. Modrinth handles update notifications for users of launchers like Prism/ATLauncher.
