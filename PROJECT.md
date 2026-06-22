# Cubeage Solar2D Singular Plugin

Cubeage Solar2D Singular Plugin is the self-hosted Solar2D native plugin for the Singular attribution SDK. It owns the Android native plugin source, Solar2D metadata, Android package workflow, and GitHub Release artifact consumed by Solar2D applications.

Lifecycle: `production`
Layer: `foundation`

## Goals

- Maintain the Solar2D `plugin.singular` integration for Singular attribution and event reporting.
- Package Android release artifacts that Solar2D apps can consume from GitHub Releases.
- Keep Singular SDK versioning, Solar2D metadata, and platform support limits explicit.

## Non-Goals

- This repository does not own game-specific attribution policy, marketing analytics decisions, or app runtime behavior.
- This repository does not own the upstream Singular SDK or Solar2D runtime.
- This repository does not own central CI runner, enterprise release bot, or platform preview behavior.

## Boundaries

The machine-readable source of truth is [.doctrine/project.json](.doctrine/project.json). Agents must keep this repository as a reusable native plugin package and route app-specific attribution or analytics behavior to consuming game repositories.

## Public Surfaces

- Lua plugin API documented in `README.md`.
- Android native plugin code under `android/`.
- Solar2D plugin metadata in `metadata.lua`.
- GitHub Release artifact `android.tgz` produced by `.github/workflows/release.yml`.

## Delivery

Release changes require Android build artifacts, package smoke evidence, GitHub Release readback, and consumer compatibility notes for affected apps. Published artifacts are externally consumable; recovery is normally a forward-fix release, release deletion/deprecation when safe, or staged channel halt rather than source revert alone.
