# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Changed

- The usage-reporting "Details" link (startup notice, `config.yml` and the docs) now points at https://danielstephenson.dev/usage-reporting, a public page; the previous link led to a private repository and returned 404 for everyone. The vendored trace client is now 0.6.1, which carries the same link in the `plugins/trace/config.yml` header it writes. Details: https://github.com/Stephenson-Software/trace-client-java/releases/tag/0.6.1.

## [0.3.0] – 2026-10-06

### Changed

- The vendored trace client is now 0.4.0, and every usage event now carries the plugin version, `command` events included; before, only `startup` did.
- The K/D ratio shown by `/kdrt info` is now rounded to two decimal places instead of being printed at full floating-point precision, so a player with 1 kill and 3 deaths sees `K/D Ratio: 0.33` rather than `K/D Ratio: 0.3333333333333333`. The stored kill and death counts and the underlying ratio calculation are unchanged.
- The vendored trace client is now 0.3.0. `plugins/trace/config.yml` can now carry a `tags:` block whose entries are added to every usage event the plugin sends, so a test server can mark its own events (the release gates write `ci: "true"`) and be left out of the figures for real installations. Nothing changes for a server whose `plugins/trace/config.yml` has no `tags:` block.

### Fixed

- The messages sent after a kill or a death now use the singular for a count of 1: "You now have 1 kill." and "You now have 1 death." instead of "1 kills" and "1 deaths". Other counts are unchanged ("You now have 2 kills.").
- `debugMode: true` in `config.yml` now does something. The option had been written into every generated config but nothing logged through it, so turning it on produced no output. With it on, the plugin logs `[DEBUG]` lines through its own console logger when player records are loaded or saved, when a record is created for a joining player, and when a kill or death is recorded or skipped because the player has no record. The debug logger previously wrote to standard output under the prefix `[ExamplePonderPlugin]`, left over from the template the plugin was started from; it now uses the plugin's logger and the `[KDRTracker]` prefix that comes with it.

- The `kdrt.view` permission node is now declared in `plugin.yml` with `default: true`, matching its `kdrt.info` sibling. `InfoCommand` passes both nodes to ponder, whose `PermissionChecker` grants access when the sender holds any one of them, so on a server that leaves the defaults alone nothing changes — `kdrt.info` is granted to everyone and is checked first. On a server that denies or negates `kdrt.info`, however, the undeclared `kdrt.view` fell back to op-only instead of to a declared default, so granting it could not restore access to `/kdrt info` or its `/kdrt view` alias for a non-op. `kdrt.default` is left undeclared on purpose and is now commented as vestigial: `KDRTracker#onCommand` constructs `DefaultCommand` and executes it directly rather than routing it through ponder, so that node is never consulted.

- JUnit, Hamcrest and the JetBrains/IntelliJ annotations are no longer bundled into the plugin JAR. They reached the shaded artifact two ways: `ponder` declares them as compile-scope dependencies, and `ponder-1.1.jar` is itself an uber JAR that embeds its own copies of the same classes. Excluding the transitive dependencies alone would have silenced the shade plugin's overlap warnings without removing anything, so ponder's contribution is now filtered as well. None of these libraries are used by KDR Tracker at runtime. The shipped JAR drops from 506 class files (534 KB) to 51 (74 KB), and the duplicate classes that the shade plugin previously resolved arbitrarily — a class-loading hazard on servers running other plugins that shade JUnit — are gone. All 13 plugin classes are retained, as are ponder's own classes apart from the test class it ships (`preponderous.ponder.tests.TestArgumentParser`), which is dropped because it references `org.junit.Assert`.
- The `Dev Release` workflow now retries publishing the `dev` prerelease before giving up. The release and its tag have to be deleted and recreated for the tag to move to the new commit, and a transient API failure inside that window previously left the repository with no `dev` release at all until the workflow was re-run by hand. Each attempt now starts from a clean slate, and an exhausted retry fails loudly.

### Added

- Player records are now saved to `playerRecords.json` every 5 minutes while the server runs, as well as when the plugin is disabled. Before, they were saved only on disable, so a crash, a killed process or a failed shutdown lost every kill and death since the last start; now at most the last interval is lost. The interval is the new `saveInterval` option in `config.yml`, in minutes; `0` restores the old save-on-disable-only behaviour. A `config.yml` that predates the option uses the bundled 5 minutes. The save runs on the main thread, where kills and deaths are recorded.
- The plugin now reports usage events — `startup` on enable, `command` on each of its commands — to the author's trace server so it is known which plugins are in use. Events carry the plugin name, the event name, and the plugin version or command name; nothing about players or the server. Reporting runs off the main thread, never delays a tick, drops silently when the server is unreachable, and is turned off with `usage-reporting.enabled: false` in `config.yml`. The default config carries the plugin's key, so reporting is active out of the box unless turned off — including on servers upgraded from a version before the `usage-reporting` block existed, whose `config.yml` is not rewritten until the plugin version changes: the plugin reads the bundled defaults for any key the file lacks. A bundled `config.yml` is new with this change; a fresh install has it written out before the generated `version` and `debugMode` options are added.
- The plugin says on the console at every start whether usage reporting is on and how to turn it off. A server-wide switch, `plugins/trace/config.yml`, is created on first start and honoured by every plugin that reports to trace; the environment variables `TRACE_USAGE_REPORTING=off` and `DO_NOT_TRACK=1` turn it off for the whole process. A `config.yml` that predates the `usage-reporting` block is now rewritten with it on the next start, not only when the plugin version changes, so the opt-out is visible in the file.

- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get kdrtracker --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

## [0.2.0-SNAPSHOT-8-8-2026] – 2026-08-08

### Changed
- KDRTracker is now developed AI-first. Day-to-day feature work, grooming, review and maintenance run through AI agents working directly against this repository, with the maintainers setting direction and approving what lands. The version bump marks that change in how the project is built — it is not a break in behaviour, configuration or stored data, and existing installations can upgrade in place. Released as `0.2.0-SNAPSHOT-8-8-2026`: the AI-first line has not yet been verified in live operation, and the dated snapshot designation stays until it has.

### Fixed
- Player records loaded from storage on plugin enable are now found correctly on rejoin. Previously, `PersistentData` compared player UUIDs with `==` instead of `.equals()`, so a UUID re-parsed from disk (or from a new `Player` object) never matched the loaded record, silently orphaning existing kill/death data.

## [Initial Release]

### Added
- Automatic kill and death tracking per player
- `/kdrt info` to view kills, deaths, and K/D ratio
