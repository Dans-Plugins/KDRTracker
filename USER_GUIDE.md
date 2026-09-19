# KDR Tracker User Guide

## What is KDR Tracker?

KDR Tracker is a lightweight Spigot plugin that tracks each player's kills and deaths and lets them view their kill/death ratio at any time.

## Installation

1. Download the latest `KDRTracker-<version>.jar` from the [Releases](https://github.com/Dans-Plugins/KDRTracker/releases) page.
2. Place the JAR in your server's `plugins/` folder.
3. Restart the server.

## Getting Started

- Run `/kdrt info` to view your kills, deaths, and K/D ratio.
- Stats are tracked automatically. Every death counts as a death, whatever the cause — a fall, a mob, lava or `/kill` all count, not only being killed by another player. A kill is counted when the victim's killer is a player.
- Each time a kill or death is recorded, the player is told their new total in chat (`You now have 3 kills.`, `You now have 1 deaths.`).

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
