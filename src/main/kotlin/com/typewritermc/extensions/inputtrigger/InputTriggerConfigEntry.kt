package com.typewritermc.extensions.inputtrigger

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.ManifestEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.utils.item.Item

/**
 * Enum of all supported input types.
 * Each maps to a Bukkit event that the listener handles.
 */
enum class InputType {
    /** F key — swap main/off hand */
    SWAP_HAND,
    /** Q key — drop item(s) from hand */
    DROP,
    /** Shift key */
    SNEAK,
    /** Ctrl key */
    SPRINT,
    /** T key — chat message */
    CHAT,
    /** / key — command */
    COMMAND,
    /** Hotbar slot selection (0-8) */
    HOTBAR_SLOT,
}

/**
 * Supported click interaction types for inventory buttons.
 */
enum class ClickType {
    /** Matches ANY click type — acts as fallback if no exact match */
    ALL,
    /** Left click (inventory + world left-click) */
    LEFT,
    /** Right click (inventory + world right-click) */
    RIGHT,
    /** Shift + left click */
    SHIFT_LEFT,
    /** Shift + right click */
    SHIFT_RIGHT,
    /** Q key drop */
    DROP,
    /** Double click */
    DOUBLE_CLICK,
}

/**
 * Binds a set of [actions] to a specific [clickType] with optional [criteria].
 */
data class ClickActionBinding(
    @Help("The click type that triggers these actions")
    val clickType: ClickType = ClickType.LEFT,
    @Help("Actions to execute when this click type is used")
    val actions: List<Ref<ActionEntry>> = emptyList(),
    @Help("Criteria that must be met for this binding to fire")
    val criteria: List<Criteria> = emptyList(),
)

/**
 * Configuration entry for InputTrigger extension.
 * Contains a list of input definitions and inventory buttons.
 */
@Entry("input_trigger_config", "Player Input Triggers Configuration", Colors.GREEN, "mdi:keyboard")
class InputTriggerConfigEntry(
    override val id: String = "default",
    override val name: String = "input_trigger_config",
    @Help("Input trigger definitions for triggering actions on player input")
    val keybinds: List<InputDefinition> = emptyList(),
    @Help("Clickable buttons in the player inventory or crafting grid")
    val inventoryButtons: List<InventoryButton> = emptyList(),
) : ManifestEntry

/**
 * Defines a single input trigger with its actions and conditions.
 */
data class InputDefinition(
    @Help("The input type to listen for")
    val type: InputType = InputType.SWAP_HAND,
    @Help("Enable or disable this input handler")
    val enabled: Boolean = false,
    @Help("If true, the original game action is cancelled")
    val cancelAction: Boolean = false,
    @Help("Actions to execute when the input is triggered")
    val actions: List<Ref<ActionEntry>> = emptyList(),
    @Help("Delay in ticks before executing actions (0 = instant)")
    val delay: Long = 0,
    @Help("Cooldown in ticks between consecutive triggers of this same binding (0 = no cooldown)")
    val cooldown: Long = 0,
    @Help("For DROP: number of items to drop. 1 = single item, -1 = full stack, >1 = custom count")
    val dropCount: Int = 1,
    @Help("For HOTBAR_SLOT: the slot index (0-8) that triggers this definition")
    val hotbarSlot: Int = 0,
    @Help("Criteria that must be met for this input trigger to fire")
    val criteria: List<Criteria> = emptyList(),
)

/**
 * Defines a clickable button in the player inventory or crafting grid.
 */
data class InventoryButton(
    @Help("Slot in the inventory (0-40 for player inventory, 0-4 for crafting grid)")
    val slot: Int = 0,
    @Help("The item to display as the button")
    val item: Var<Item> = ConstVar(Item.Empty),
    @Help("Actions mapped by click type")
    val clickActions: List<ClickActionBinding> = emptyList(),
    @Help("Delay in ticks before executing actions")
    val delay: Long = 0,
    @Help("If true, this button is placed in the 2x2 crafting grid")
    val isCraftingGrid: Boolean = false,
    @Help("Global criteria that must be met for this button to appear and be clickable")
    val criteria: List<Criteria> = emptyList(),
)
