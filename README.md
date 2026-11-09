# Recruits Upgrades

**Short notice:** This mod was "vibe-coded" for personal use and will most likely **not** be actively supported going forward. Use at your own risk.

## Overview

**Recruits Upgrades** adds an upgrade system for claims from the **Recruits** mod.  
You can now upgrade your faction claims using emeralds to significantly increase the maximum number of chunks they can control.

## Features

- 5 upgrade levels
- Increases maximum claim size from 50 up to 100 chunks
- Complete customization through config:
  - Change the name of each upgrade level
  - Modify emerald cost per level
  - Adjust maximum chunks per level

## Dependencies

- **Recruits** (required)
- **Cloth Config** (recommended — provides in-game configuration menu)
- NeoForge 1.21.1

## Installation

1. Build the mod (see _Building from source_ below).
2. Copy the resulting `.jar` file into your Minecraft `mods/` folder.
3. Launch the game with the correct NeoForge version and the required mods installed.

## Configuration

The mod uses **Cloth Config**, so settings are available in-game:

- Go to Mod Options / Config screen (or Mod Menu)
- You can edit:
  - Names of each upgrade level
  - Emerald cost for each upgrade
  - Maximum number of chunks per level

Default upgrade levels:

- Level 0 → 50 chunks (0 emeralds)
- Level 1 → 60 chunks (32 emeralds)
- Level 2 → 70 chunks (64 emeralds)
- Level 3 → 80 chunks (128 emeralds)
- Level 4 → 90 chunks (256 emeralds)
- Level 5 → 100 chunks (512 emeralds)

## Building from source

From the project root:

```bash
# Build the release jar
./gradlew build
```
