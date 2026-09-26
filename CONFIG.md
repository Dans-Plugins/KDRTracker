# KDR Tracker Configuration

A `config.yml` is generated in `plugins/KDRTracker/` on first run.

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `version` | String | *(plugin version)* | Plugin version. Do not edit manually. |
| `debugMode` | Boolean | `false` | Logs `[DEBUG]` lines to the console, under the plugin's own prefix, when player records are loaded or saved, when a record is created for a joining player, and when a kill or death is recorded — or skipped because the player has no record. Takes effect on the next server start. |
| `usage-reporting.enabled` | Boolean | `true` | Whether the plugin reports usage events (see below). Set to `false` to turn it off. |
| `usage-reporting.endpoint` | String | `https://trace.danielstephenson.dev` | The trace server events are sent to. |
| `usage-reporting.key` | String | the plugin's key | Identifies this plugin to the trace server so reports are attributed to it. Not a secret: it ships in the default config and can only report as KDRTracker. Empty means reporting is off regardless of `enabled`. |

## Usage reporting

When the plugin is enabled, and each time one of its commands is used, a small event is sent to the
author's [trace](https://github.com/Stephenson-Software/trace-client-java) server so it is known which
plugins are actually in use. An event carries the plugin's name, the event name (`startup` or
`command`), and either the plugin version or the command name — nothing about players, the world, or
the server. Sending happens off the main thread, never delays a tick, and is dropped silently if the
server cannot be reached. The plugin says on the console at every start whether reporting is on.

To turn it off, in order of precedence:

- the environment variable `TRACE_USAGE_REPORTING=off` (or `DO_NOT_TRACK=1`) turns it off for every
  program in the server process;
- `enabled: false` in `plugins/trace/config.yml` turns it off for every plugin on the server that
  reports to trace — the file is created with `enabled: true` by the first such plugin to start and
  is never turned back on by a plugin;
- `usage-reporting.enabled: false` in this plugin's `config.yml` turns it off for KDRTracker alone.

A `config.yml` written by a version before the `usage-reporting` block existed is rewritten with the
block, using the bundled values, the next time the plugin starts, so the switch is visible in the
file; until then the plugin reads the bundled defaults for any option the file lacks. Details:
https://github.com/Stephenson-Software/trace#usage-reporting
