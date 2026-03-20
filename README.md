# FluxForge

An industrial tech plugin for Minecraft servers. Build power networks, automate your world, and fly with a jetpack — no mods required.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1+-green)](https://www.minecraft.net)
[![Java](https://img.shields.io/badge/Java-21+-orange)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

---

## What is FluxForge?

FluxForge brings the industrial tech mod experience to vanilla Minecraft servers. Build generators, connect machines through conduit networks, automate mining and mob grinding, sort your chests, and eventually craft a jetpack to fly around your setup.

Everything runs on **FE (Flux Energy)** — a custom power unit that flows between machines through copper grate conduits. Machines have their own energy buffers, batteries store excess power, and breaking a conduit splits the network cleanly without losing any stored energy.

---

## Features

- **Energy network system** with per-machine buffers, conduit routing, and automatic network split/merge
- **8 machines:** Generator, Solar Panel, Battery, Electric Furnace, Miner, Mob Grinder, Item Sorter, Charging Station
- **Flux Jetpack** with a dedicated charging system
- **Crafting progression** across 3 tiers of components
- **In-game recipe viewer** via `/ff recipes`
- **Wrench tool** to open GUIs for every machine
- **Toggleable machines** — miners and mob grinders are off by default
- **Full localization** with English and Czech out of the box, easy to add more
- **SQLite** by default, **MySQL/MariaDB** supported with a built-in migration command
- **Fully configurable** energy values, machine costs, and more

---

## Requirements

| Requirement | Version |
|-------------|---------|
| Minecraft (Paper/Spigot) | 1.21.1+ |
| Java | 21+ |

---

## Installation

1. Download the latest `FluxForge.jar` from the [Releases](https://github.com/DogeTennant/FluxForge/releases) page
2. Drop it into your server's `plugins/` folder
3. Restart the server
4. Done — FluxForge uses SQLite by default with no extra setup required

---

## Quick Start

Once installed, use the following commands to get started:

| Command | Description |
|---------|-------------|
| `/ff recipes` | Browse all crafting recipes in-game |
| `/ff give <machine>` | Give yourself a machine item (admin) |
| `/ff givecomponent <component>` | Give yourself a component (admin) |
| `/ff givewrench` | Give yourself a Flux Wrench (admin) |
| `/ff setenergy <amount>` | Set energy on a machine you're looking at (admin) |
| `/ff language <lang>` | Switch language (admin) |
| `/ff reload` | Reload config and translations (admin) |
| `/ff migratedb confirm` | Migrate SQLite data to MySQL (admin) |

All admin commands require the `fluxforge.admin` permission (default: op).

The `/ff recipes` command is available to all players.

---

## Configuration

The main config file is at `plugins/FluxForge/config.yml`. Key settings:

```yaml
language: en_us  # Active language file

database:
  type: sqlite   # Change to 'mysql' for MySQL/MariaDB

machines:
  generator:
    production-per-tick: 10
    max-buffer: 1000
    coal-ticks: 80
  # ... and more
```

See the [Wiki](https://github.com/DogeTennant/FluxForge/wiki) for full configuration documentation.

---

## MySQL Setup

To use MySQL or MariaDB instead of SQLite:

1. Configure your database credentials in `config.yml` under `database.mysql`
2. If you have existing SQLite data, run `/ff migratedb confirm` first
3. Change `database.type` to `mysql`
4. Restart the server

---

## Translations

FluxForge ships with English (`en_us.yml`) and Czech (`cs_cz.yml`). To add a new language:

1. Copy `en_us.yml` from `plugins/FluxForge/translations/`
2. Rename it to your language code (e.g. `de_de.yml`)
3. Translate the values
4. Set `language: de_de` in `config.yml`
5. Run `/ff reload`

---

## Building from Source

```bash
git clone https://github.com/DogeTennant/FluxForge.git
cd FluxForge
mvn package
```

The built jar will be in `target/`.

---

## Wiki

Full documentation is available on the [GitHub Wiki](https://github.com/DogeTennant/FluxForge/wiki):

- [Getting Started](https://github.com/DogeTennant/FluxForge/wiki/Getting-Started)
- [Energy System](https://github.com/DogeTennant/FluxForge/wiki/Energy-System)
- [Machines](https://github.com/DogeTennant/FluxForge/wiki/Machines)
- [Crafting Progression](https://github.com/DogeTennant/FluxForge/wiki/Crafting-Progression)
- [Configuration](https://github.com/DogeTennant/FluxForge/wiki/Configuration)
- [Commands and Permissions](https://github.com/DogeTennant/FluxForge/wiki/Commands-and-Permissions)
- [Database Setup](https://github.com/DogeTennant/FluxForge/wiki/Database-Setup)
- [Translations](https://github.com/DogeTennant/FluxForge/wiki/Translations)

---

## License

MIT License. See [LICENSE](LICENSE) for details.

---

*Made by [DogeTennant](https://github.com/DogeTennant)*
