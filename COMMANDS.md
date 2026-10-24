# KDR Tracker Commands

All commands use `/kdrt` or `/kdrtracker` as the base.

| Command | Description | Permission |
|---------|-------------|------------|
| `/kdrt` | View the plugin version, developer, and wiki link. | *(none)* |
| `/kdrt help` | View a list of commands. | `kdrt.help` |
| `/kdrt info` (alias: `/kdrt view`) | View your kills, deaths, and K/D ratio. Players only. | `kdrt.info` or `kdrt.view` |

Sub-command names are case-sensitive: `/kdrt HELP` is not `/kdrt help`. A sub-command that isn't listed above gets `That command wasn't found.` A sender who holds none of a command's permission nodes is told which nodes it accepts (for example `In order to use this command, you need one of the following permission: '[kdrt.info, kdrt.view]'`), and the command does not run.
