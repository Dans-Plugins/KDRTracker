# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

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
