# KDR Tracker User Guide

## What is KDR Tracker?

KDR Tracker is a lightweight Spigot plugin that tracks each player's kills and deaths and lets them view their kill/death ratio at any time.

## Installation

1. Download the latest `KDRTracker-<version>.jar` from the [Releases](https://github.com/Dans-Plugins/KDRTracker/releases) page.
2. Place the JAR in your server's `plugins/` folder.
3. Restart the server.

## Getting Started

- Run `/kdrt info` to view your kills, deaths, and K/D ratio. It can only be used by a player, not from the console. The ratio is kills divided by deaths, rounded to two decimal places; a player with no deaths sees their kill count as the ratio (`3 kills, 0 deaths` shows `K/D Ratio: 3.00`).
- Run `/kdrt` for the plugin version and `/kdrt help` for a list of commands; see [COMMANDS.md](COMMANDS.md).
- A player's record is created the first time they join after the plugin is installed. Kills and deaths before then are not counted, and a player who was already online when the plugin was enabled has none recorded until they rejoin.
- Stats are tracked automatically. Every death counts as a death, whatever the cause — a fall, a mob, lava or `/kill` all count, not only being killed by another player. A kill is counted when the victim's killer is a player.
- Each time a kill or death is recorded, the player is told their new total in chat (`You now have 3 kills.`, `You now have 1 deaths.`).

## Data Storage

Kill and death counts are kept in `plugins/KDRTracker/playerRecords.json`. The file is read when the plugin is enabled and written when it is disabled, such as on a normal server stop; it is not written in between, so stats recorded since the last start are lost if the server crashes or is killed without shutting down.

## Configuration

A `config.yml` is written to `plugins/KDRTracker/` on first start. Usage reporting is on by default and can be turned off there; see [CONFIG.md](CONFIG.md) for every option and the opt-out.

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `kdrt.help` | `true` | View the help menu. |
| `kdrt.info` | `true` | View your K/D stats. |
| `kdrt.view` | `true` | Alternative to `kdrt.info`; either node grants `/kdrt info` and its `/kdrt view` alias. |

## Support

Ask questions in the [Discord server](https://discord.gg/xXtuAQ2) or open a [GitHub issue](https://github.com/Dans-Plugins/KDRTracker/issues).
