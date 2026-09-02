# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed

- The `kdrt.view` permission node is now declared in `plugin.yml` with `default: true`, matching its `kdrt.info` sibling. `InfoCommand` passes both nodes to ponder, whose `PermissionChecker` grants access when the sender holds any one of them, so on a server that leaves the defaults alone nothing changes — `kdrt.info` is granted to everyone and is checked first. On a server that denies or negates `kdrt.info`, however, the undeclared `kdrt.view` fell back to op-only instead of to a declared default, so granting it could not restore access to `/kdrt info` or its `/kdrt view` alias for a non-op. `kdrt.default` is left undeclared on purpose and is now commented as vestigial: `KDRTracker#onCommand` constructs `DefaultCommand` and executes it directly rather than routing it through ponder, so that node is never consulted.

- JUnit, Hamcrest and the JetBrains/IntelliJ annotations are no longer bundled into the plugin JAR. They reached the shaded artifact two ways: `ponder` declares them as compile-scope dependencies, and `ponder-1.1.jar` is itself an uber JAR that embeds its own copies of the same classes. Excluding the transitive dependencies alone would have silenced the shade plugin's overlap warnings without removing anything, so ponder's contribution is now filtered as well. None of these libraries are used by KDR Tracker at runtime. The shipped JAR drops from 506 class files (534 KB) to 51 (74 KB), and the duplicate classes that the shade plugin previously resolved arbitrarily — a class-loading hazard on servers running other plugins that shade JUnit — are gone. All 13 plugin classes are retained, as are ponder's own classes apart from the test class it ships (`preponderous.ponder.tests.TestArgumentParser`), which is dropped because it references `org.junit.Assert`.
- The `Dev Release` workflow now retries publishing the `dev` prerelease before giving up. The release and its tag have to be deleted and recreated for the tag to move to the new commit, and a transient API failure inside that window previously left the repository with no `dev` release at all until the workflow was re-run by hand. Each attempt now starts from a clean slate, and an exhausted retry fails loudly.

### Added

- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get kdrtracker --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

### Changed

- The K/D ratio shown by `/kdrt info` is now rounded to two decimal places instead of being printed at full floating-point precision, so a player with 1 kill and 3 deaths sees `K/D Ratio: 0.33` rather than `K/D Ratio: 0.3333333333333333`. The stored kill and death counts and the underlying ratio calculation are unchanged.

## [0.2.0-SNAPSHOT-8-8-2026] – 2026-08-08

### Changed
- KDRTracker is now developed AI-first. Day-to-day feature work, grooming, review and maintenance run through AI agents working directly against this repository, with the maintainers setting direction and approving what lands. The version bump marks that change in how the project is built — it is not a break in behaviour, configuration or stored data, and existing installations can upgrade in place. Released as `0.2.0-SNAPSHOT-8-8-2026`: the AI-first line has not yet been verified in live operation, and the dated snapshot designation stays until it has.

### Fixed
- Player records loaded from storage on plugin enable are now found correctly on rejoin. Previously, `PersistentData` compared player UUIDs with `==` instead of `.equals()`, so a UUID re-parsed from disk (or from a new `Player` object) never matched the loaded record, silently orphaning existing kill/death data.

## [Initial Release]

### Added
- Automatic kill and death tracking per player
- `/kdrt info` to view kills, deaths, and K/D ratio
