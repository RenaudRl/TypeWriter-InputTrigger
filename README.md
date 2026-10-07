# InputTrigger Extension

![Java Version](https://img.shields.io/badge/Java-21-orange)
![Target](https://img.shields.io/badge/Target-Paper-blue)
![Typewriter](https://img.shields.io/badge/Typewriter-0.9.0--beta--177-purple)

**InputTrigger Extension** is a key interception and inventory button system for **TypeWriter**, built for **BTC Studio** infrastructure. It runs actions when a player presses a vanilla key, and pins clickable buttons in the player's inventory or 2x2 crafting grid.

---

## 🚀 Key Features

### ⌨️ Input interception
- **7 input types**: `SWAP_HAND` (F), `DROP` (Q), `SNEAK` (Shift), `SPRINT` (Ctrl), `CHAT` (T), `COMMAND` (/), `HOTBAR_SLOT` (1-9).
- **Per-definition fields**: `enabled` (off by default), `cancelAction`, `actions`, `delay`, `cooldown`, `dropCount`, `hotbarSlot`, `criteria`.
- **One definition per input type**: when several share a type, the first one is used.
- **Cooldown**: `cooldown` is in ticks, per player and per input type (0 = none).

### 🖱️ Inventory buttons
- **Persistent items**: buttons pinned to a slot, which cannot be moved, dropped or picked up.
- **Click types**: `LEFT`, `RIGHT`, `SHIFT_LEFT`, `SHIFT_RIGHT`, `DROP`, `DOUBLE_CLICK`, and `ALL` as a fallback.
- **Criteria**: on the button, and on each click binding.
- **World use**: a button held in the hand runs its LEFT or RIGHT actions when used in the world.

### 🔧 Crafting grid
- `isCraftingGrid` places the button in the 2x2 crafting grid (slots 0-4); slot 0 is the result slot.
- Crafting is blocked while a button occupies the grid, so items cannot be duplicated.

---

## 🧩 Available Entries

| Type | Entry | Role |
|---|---|---|
| Manifest | `input_trigger_config` | `keybinds` (input definitions) and `inventoryButtons` (buttons: `slot`, `item`, `clickActions`, `delay`, `isCraftingGrid`, `criteria`) |

The extension declares no command and no permission.

---

## 📋 Requirements

- **Typewriter** `0.9.0-beta-177`
- **Paper** 1.21.x (no Folia support is promised)

## ⚙️ Configuration

Everything is configured from the Typewriter web panel: add an `input_trigger_config` entry, then add its `keybinds` and `inventoryButtons`. Field reference: [BTC Studio Docs](https://docs.borntocraftstudio.net/extensions/free/inputtracker/).

## 🛠 Building & Deployment

Requires **Java 21**.

```bash
./gradlew clean build
```

Output: `build/libs/InputTriggerExtension-<version>.jar`

---

## 🤝 Credits & Inspiration
- **[TypeWriter](https://github.com/gabber235/Typewriter)** - The engine this extension is built for.
- **[BTC Studio](https://github.com/RenaudRl)** - Maintenance and specialized optimizations.

---

## 📜 Licence

**GNU General Public License v3.0 or later** — [LICENSE](LICENSE) — with a
**linking exception** for the Typewriter engine — [LICENSE-EXCEPTION.md](LICENSE-EXCEPTION.md).

| | |
|---|---|
| You may | Run it anywhere, **including on a monetised server**. Study it, modify it, use it as a base, and redistribute it — **even for a fee**. GPLv3 §4 explicitly allows charging for a copy. |
| You must | Publish the complete corresponding source of your version under GPLv3, preserve the copyright notices, and **state that you modified it and when** (§5(a)). |
| You may not | Ship a closed-source or proprietary version, relicense under stricter terms, or strip the attribution and present this work as your own — §8 terminates your rights automatically. |
| Marks | **"Born To Craft"** and **"BTC Studio"** are **not** covered by the GPL. Fork it freely, sell your fork if you like — but **rebrand it**. |

> Reselling this code is legally allowed and practically pointless: whoever buys a
> copy from you receives, under the GPL, the right to redistribute it for free.
> That is the protection — not a clause forbidding sale, which the GPL does not
> permit us to add.

### About Typewriter

This is a **third-party extension**. It uses the public extension API of the
[Typewriter](https://github.com/gabber235/Typewriter) engine by gabber235 and
contains none of its source. Born To Craft Studio is not affiliated with or
endorsed by the Typewriter project.

The engine itself is **not** free software — its licence forbids redistributing
it. **Get it from the Typewriter project, and never redistribute it**, including
inside a fork of this repository.

Full attribution, the statement of modifications required by §5(a), and the
trademark reservation are in **[NOTICE.md](NOTICE.md)**. Read it before
redistributing.

© 2026 Born To Craft Studio.
