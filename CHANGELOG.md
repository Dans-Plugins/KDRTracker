# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed
- Player records loaded from storage on plugin enable are now found correctly on rejoin. Previously, `PersistentData` compared player UUIDs with `==` instead of `.equals()`, so a UUID re-parsed from disk (or from a new `Player` object) never matched the loaded record, silently orphaning existing kill/death data.

## [Initial Release]

### Added
- Automatic kill and death tracking per player
- `/kdrt info` to view kills, deaths, and K/D ratio
