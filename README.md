# TypeWriter InputTrigger Extension

Trigger actions via keybinds and custom inventory buttons — a TypeWriter engine extension.

## Features

- **KeyBind Triggers**: Execute actions when players press specific keys
  - Swap Hand (F), Drop Item (Q), Drop Stack (Ctrl+Q)
  - Sneak (Shift), Sprint (Ctrl)
  - Hotbar slots 1-9
  - Chat (T), Command (/)
- **Inventory Buttons**: Place clickable buttons in player inventory or 2x2 crafting grid
- **Fact Conditions**: Each keybind and button supports optional fact conditions
- **Full TypeWriter Integration**: Uses standard `ActionEntry`, `FactEntry`, and `Item` systems

## Installation

1. Download the latest JAR from [Releases](../../releases)
2. Place in `plugins/TypeWriter/extensions/`
3. Reload TypeWriter: `/tw reload`

## Usage

### KeyBind Configuration

Create a `keybind_config` entry with a list of `keybind_definitions`:

```toml
[my_keybinds]
type = "keybind_config"
keybinds = [
  {
    type = "SWAP_HAND"
    enabled = true
    cancelAction = true
    actions = ["my_action"]
  },
  {
    type = "SNEAK"
    enabled = true
    cancelAction = false
    fact = "is_sneaking_allowed"
    actions = ["sneak_action"]
  }
]
```

### Inventory Buttons

```toml
[my_buttons]
type = "keybind_config"
inventoryButtons = [
  {
    slot = 8
    item = { type = "compass", name = "&bMenu" }
    clickActions = {
      RIGHT = ["open_menu_action"]
    }
  }
]
```

## Building

```bash
./gradlew build
```

Requires JDK 21+.

## Dependencies

- TypeWriter Engine `0.9.0-beta-172`
- Paper 1.21.8+

## License

MIT
