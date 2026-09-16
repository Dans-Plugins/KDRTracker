# KDR Tracker
This open source minecraft plugin is intended to keep track of players' kill/death ratios. 

## Usage reporting

Usage reporting is on by default: when the plugin is enabled, and each time one of its commands is used, it sends its name, version and the command name to the author's [trace](https://trace.danielstephenson.dev) server so it is known which plugins are actually in use. Nothing about players, worlds, IPs or the server is sent, and nothing typed after a command. The plugin says on the console at every start whether reporting is on. To turn it off:

- for this plugin: set `usage-reporting.enabled` to `false` in `plugins/KDRTracker/config.yml`;
- for every plugin on the server that reports to trace: set `enabled` to `false` in `plugins/trace/config.yml` (created on first start);
- for the whole server process: set the environment variable `TRACE_USAGE_REPORTING=off` or `DO_NOT_TRACK=1`.

Details: https://github.com/Stephenson-Software/trace#usage-reporting
