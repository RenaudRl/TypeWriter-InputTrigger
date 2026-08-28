# InputTrigger Extension

![Java Version](https://img.shields.io/badge/Java-25-orange)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Target](https://img.shields.io/badge/Target-Paper%201.21%2B-blue)

**InputTrigger Extension** is a keybind interception and inventory button system for **TypeWriter**, engineered for **BTC Studio** infrastructure. It allows you to intercept vanilla Minecraft key presses and place persistent clickable buttons in the player's inventory or crafting grid — turning the keyboard itself into a dynamic UI surface.

---

## 🚀 Key Features

### ⌨️ Keybind Interception
- **7 Input Types**: SWAP_HAND (F), DROP (Q), SNEAK (Shift), SPRINT (Ctrl), CHAT (T), COMMAND (/), HOTBAR_SLOT (1-9).
- **Per-Binding Controls**: `enabled`, `cancelAction`, `delay`, `cooldown`, `dropCount`, `hotbarSlot`, and `criteria`.
- **Criteria Filtering**: Each keybind supports TypeWriter criteria — fire only when conditions are met.
- **Spam Protection**: Configurable cooldown in ticks between consecutive triggers.

### 🖱️ Inventory Buttons
- **Persistent Items**: Non-removable, non-droppable buttons pinned to specific inventory slots.
- **7 Click Types**: LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT, DROP, DOUBLE_CLICK, and ALL fallback.
- **Per-Binding Criteria**: Different click actions can have their own criteria conditions.
- **World Interaction**: Buttons in the hotbar trigger LEFT/RIGHT actions when used in the world.

### 🔧 Crafting Grid Integration
- Place buttons in the 2×2 crafting grid slots (0-4).
- **Slot 0** acts as a "Submit" or "Process" button.
- **Slots 1-4** serve as status indicators or quick-access controls.
- **Crafting Safety**: If a button occupies a matrix slot, the crafting result is nullified to prevent item duplication.

### ⚡ Performance
- **Standard Bukkit Events** — no packet library required.
- **Paper 1.21+ Compatible** — uses Paper's `AsyncChatEvent` and modern API.
- **Clean Lifecycle**: Two-phase crafting grid cleanup prevents refund flicker.

---

## ⚙️ Configuration

Configuration is managed via a single manifest entry `input_trigger_config`:

```toml
[my_config]
type = "input_trigger_config"
id = "default"
keybinds = [
  { type = "SWAP_HAND", enabled = true, cancelAction = true, actions = ["open_menu_action"], cooldown = 10 },
  { type = "DROP", enabled = true, cancelAction = true, dropCount = -1, actions = ["drop_all_action"] }
]
inventoryButtons = [
  { slot = 8, item = { type = "clock", name = "<gold>Menu" }, clickActions = [
    { clickType = "LEFT", actions = ["open_server_menu"] }
  ]}
]
```

### Field Reference

See the full documentation on the [Wiki](https://docs.borntocraftstudio.net/extensions/free/inputtracker/).

---

## 🛠 Building & Deployment

Requires **Java 25**.

```bash
./gradlew build
```

Output JAR is placed in `build/libs/`.

---

## 📦 Dependencies

- **TypeWriter Engine** `0.9.0-beta-173`
- **Paper API** `1.21.4+`

---

## 📖 Wiki

Full documentation: [docs.borntocraftstudio.net](https://docs.borntocraftstudio.net/extensions/free/inputtracker/)

---

## 📄 License

MIT

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
