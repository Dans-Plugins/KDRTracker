# KDR Tracker Configuration

A `config.yml` is generated in `plugins/KDRTracker/` on first run.

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `version` | String | *(plugin version, prefixed with `v`, e.g. `v0.3.1`)* | Plugin version that last wrote the file. Do not edit manually: whenever it differs from the running version, the next start overwrites it and adds any options the file is missing. |
| `debugMode` | Boolean | `false` | Logs `[DEBUG]` lines to the console, under the plugin's own prefix, when player records are loaded or saved, when a record is created for a joining player, and when a kill or death is recorded — or skipped because the player has no record. Takes effect on the next server start. |
| `saveInterval` | Integer | `5` | Minutes between saves of player records to `playerRecords.json`, on top of the save when the plugin is disabled. Bounds how many kills and deaths a crash or a killed server can lose. `0` or less saves only on disable. Takes effect on the next server start. |
| `usage-reporting.enabled` | Boolean | `true` | Whether the plugin reports usage events (see below). Set to `false` to turn it off. Other spellings of the block or the switch (`usage_reporting:`, `usageReporting:`, `enable: false`, `disabled: true`), or `usage-reporting: false`, also count as off. |
| `usage-reporting.endpoint` | String | `https://trace.danielstephenson.dev` | The trace server events are sent to. |
| `usage-reporting.key` | String | the plugin's key | Identifies this plugin to the trace server so reports are attributed to it. Not a secret: it ships in the default config and can only report as KDRTracker. Empty means reporting is off regardless of `enabled`. |

## Usage reporting

When the plugin is enabled, and each time a player uses one of its commands, a small event is sent to the
author's [trace](https://github.com/Stephenson-Software/trace-client-java) server so it is known which
plugins are actually in use. An event carries the plugin's name, the event name (`startup` or
`command`), the plugin version, and for a command the base command name (`kdrt` or `kdrtracker`,
never the sub-command or anything typed after it), plus a random server ID (the
`server-id` line in `plugins/trace/config.yml`, which identifies no person, account or IP address;
deleting the line gets a new one) — nothing about players or the world. Repeats of the same event
within a minute are counted rather than sent one by one: the first goes out at once, and the rest
follow as a single event carrying a `coalesced` tag with how many there were (`startup` is never
held back). Sending happens off the main
thread, never delays a tick, and is dropped silently if the
server cannot be reached. The plugin says on the console at every start whether reporting is on.

To turn it off, in order of precedence:

- the environment variable `TRACE_USAGE_REPORTING=off` (or `DO_NOT_TRACK=1`) turns it off for every
  program in the server process. Both fail closed: `TRACE_USAGE_REPORTING` set to anything but blank or
  `true`/`on`/`yes`/`1`, or `DO_NOT_TRACK` set to anything but blank or `0`/`false`/`off`/`no`, turns it off
  (matched case-insensitively and ignoring surrounding spaces);
- `enabled: false` in `plugins/trace/config.yml` turns it off for every plugin on the server that
  reports to trace — the file is created with `enabled: true` by the first such plugin to start and
  is never turned back on by a plugin;
- `usage-reporting.enabled: false` in this plugin's `config.yml` turns it off for KDRTracker alone.

Only a value that clearly means on (`true`, or the string `true`, `on`, `yes` or `1`) turns reporting on: a quoted
`"false"`, a typo or an empty value turns it off. The plugin never writes over a `config.yml` it cannot
parse; it logs the error, and reporting stays off until the file is fixed. An `enabled: false` written by
hand while the server runs is kept by every later save of the config. The environment variables fail
closed too: `TRACE_USAGE_REPORTING` set to anything but blank or `true`/`on`/`yes`/`1` turns reporting
off, as does `DO_NOT_TRACK` set to anything but blank or `0`/`false`/`off`/`no`.

A `config.yml` written by a version before the `usage-reporting` block existed is rewritten with the
block, using the bundled values, the next time the plugin starts, so the switch is visible in the
file; until then the plugin reads the bundled defaults for any option the file lacks. Details:
https://danielstephenson.dev/usage-reporting
