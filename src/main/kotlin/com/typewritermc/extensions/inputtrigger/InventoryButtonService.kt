package com.typewritermc.extensions.inputtrigger

import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.ref
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.TypewriterPaperPlugin
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.events.TypewriterUnloadEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.*
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

/**
 * Manages custom inventory buttons — items placed in player inventory
 * or crafting grid that trigger actions when clicked.
 *
 * Standalone version — uses Bukkit scheduler (Paper-compatible, no Folia).
 */
class InventoryButtonService : Listener {

    private val plugin = JavaPlugin.getPlugin(TypewriterPaperPlugin::class.java)
    private val buttonKey = NamespacedKey(plugin, "input_trigger_button_id")

    private val processingPlayers = java.util.concurrent.ConcurrentHashMap.newKeySet<java.util.UUID>()

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private fun config(): InputTriggerConfigEntry {
        return try {
            Query.find<InputTriggerConfigEntry>().firstOrNull()
        } catch (_: Exception) { null }
            ?: InputTriggerConfigEntry()
    }

    // ─── Bukkit ClickType → our ClickType mapping ──────────────────────────

    private fun mapBukkitClick(click: org.bukkit.event.inventory.ClickType): ClickType = when (click) {
        org.bukkit.event.inventory.ClickType.LEFT -> ClickType.LEFT
        org.bukkit.event.inventory.ClickType.RIGHT -> ClickType.RIGHT
        org.bukkit.event.inventory.ClickType.SHIFT_LEFT -> ClickType.SHIFT_LEFT
        org.bukkit.event.inventory.ClickType.SHIFT_RIGHT -> ClickType.SHIFT_RIGHT
        org.bukkit.event.inventory.ClickType.DROP, org.bukkit.event.inventory.ClickType.CONTROL_DROP -> ClickType.DROP
        org.bukkit.event.inventory.ClickType.DOUBLE_CLICK -> ClickType.DOUBLE_CLICK
        else -> ClickType.LEFT
    }

    /**
     * Resolves actions for a given click type from a button's binding list.
     * Priority: exact match with criteria, then ALL fallback.
     */
    private fun resolveClickActions(
        bindings: List<ClickActionBinding>,
        targetType: ClickType,
        player: Player,
    ): List<com.typewritermc.core.entries.Ref<ActionEntry>> {
        val ctx = context()
        val candidates = listOfNotNull(
            bindings.firstOrNull { it.clickType == targetType },
            bindings.firstOrNull { it.clickType == ClickType.ALL },
        )
        for (binding in candidates) {
            if (binding.criteria.isNotEmpty() && !binding.criteria.matches(player, ctx)) continue
            if (binding.actions.isNotEmpty()) return binding.actions
        }
        return emptyList()
    }

    // ─── Button population ──────────────────────────────────────────────────

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        ensureButtons(event.player)
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            if (event.player.isOnline) ensureButtons(event.player)
        })
    }

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val top = event.view.topInventory
        if (top.type == InventoryType.CRAFTING) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                ensureButtons(event.player as Player)
            })
        }
    }

    // ─── Button protection ──────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        if (isButton(event.currentItem)) {
            event.isCancelled = true
            handleButtonClick(player, event.currentItem!!, event.click)
            return
        }

        if (isButton(event.cursor)) {
            event.isCancelled = true
            player.updateInventory()
            return
        }

        if (event.click == org.bukkit.event.inventory.ClickType.NUMBER_KEY) {
            val item = player.inventory.getItem(event.hotbarButton)
            if (isButton(item)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDrag(event: InventoryDragEvent) {
        if (isButton(event.oldCursor)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDrop(event: PlayerDropItemEvent) {
        val stack = event.itemDrop.itemStack
        if (!isButton(stack)) return
        event.isCancelled = true
        handleButtonClick(event.player, stack, org.bukkit.event.inventory.ClickType.DROP)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onSwap(event: PlayerSwapHandItemsEvent) {
        if (isButton(event.mainHandItem) || isButton(event.offHandItem)) {
            event.isCancelled = true
        }
    }

    // ─── World interaction (hotbar button clicks) ───────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        if (!isButton(item)) {
            val off = player.inventory.itemInOffHand
            if (!isButton(off)) return
            event.isCancelled = true
            handleButtonClick(player, off, org.bukkit.event.inventory.ClickType.RIGHT)
            return
        }

        event.isCancelled = true
        val ourClick = when (event.action) {
            org.bukkit.event.block.Action.LEFT_CLICK_AIR,
            org.bukkit.event.block.Action.LEFT_CLICK_BLOCK -> ClickType.LEFT
            org.bukkit.event.block.Action.RIGHT_CLICK_AIR,
            org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK -> ClickType.RIGHT
            else -> ClickType.LEFT
        }
        handleButtonClick(player, item, when (ourClick) {
            ClickType.LEFT, ClickType.ALL -> org.bukkit.event.inventory.ClickType.LEFT
            ClickType.RIGHT -> org.bukkit.event.inventory.ClickType.RIGHT
            ClickType.SHIFT_LEFT -> org.bukkit.event.inventory.ClickType.SHIFT_LEFT
            ClickType.SHIFT_RIGHT -> org.bukkit.event.inventory.ClickType.SHIFT_RIGHT
            ClickType.DROP -> org.bukkit.event.inventory.ClickType.DROP
            ClickType.DOUBLE_CLICK -> org.bukkit.event.inventory.ClickType.DOUBLE_CLICK
        })
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        if (isButton(item)) {
            event.isCancelled = true
            handleButtonClick(player, item, org.bukkit.event.inventory.ClickType.RIGHT)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (isButton(event.player.inventory.itemInMainHand)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onConsume(event: PlayerItemConsumeEvent) {
        if (isButton(event.item)) event.isCancelled = true
    }

    // ─── Pickup prevention ──────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPickup(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        val targetSlot = player.inventory.firstEmpty()
        if (targetSlot == -1) return
        if (isButton(player.inventory.getItem(targetSlot))) {
            event.isCancelled = true
        }
    }

    // ─── Death prevention ───────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        if (!playerHasButtons(player)) return
        val finalHealth = player.health - event.finalDamage
        if (finalHealth <= 0.0) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (playerHasButtons(event.entity)) event.isCancelled = true
    }

    // ─── Death cleanup ─────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity
        event.drops.removeIf { isButton(it) }
        var removed = false
        player.inventory.contents.forEachIndexed { index, item ->
            if (isButton(item)) {
                player.inventory.setItem(index, null)
                removed = true
            }
        }
        if (removed) player.updateInventory()
    }

    // ─── Crafting grid safety ────────────────────────────────────────────────

    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        val top = event.view.topInventory
        if (top.type != InventoryType.CRAFTING) return
        val player = event.view.player as? Player ?: return
        val cfg = config()

        // Safety: prevent real crafting if a button is in the matrix
        var hasButtonInMatrix = false
        for (slot in 1..4) {
            val matrixItem = event.inventory.getItem(slot)
            if (isButton(matrixItem)) {
                event.inventory.result = null
                hasButtonInMatrix = true
                break
            }
        }

        if (!hasButtonInMatrix) {
            // Handle crafting result button (slot 0)
            val outputButton = cfg.inventoryButtons.find { it.isCraftingGrid && it.slot == 0 }
            if (outputButton != null) {
                val itemEntry = outputButton.item.get(player, null)
                val stack = itemEntry.build(player, null)
                val meta = stack.itemMeta
                meta?.persistentDataContainer?.set(buttonKey, PersistentDataType.INTEGER, 0)
                stack.itemMeta = meta
                event.inventory.result = stack
            }
        }

        // Always re-ensure matrix buttons
        ensureButtons(player)
    }

    // ─── Crafting grid lifecycle ─────────────────────────────────────────────

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val top = event.view.topInventory
        if (top.type != InventoryType.CRAFTING) return

        // Phase 1: Best-effort clear of crafting grid buttons
        val cfg = config()
        val gridButtons = cfg.inventoryButtons.filter { it.isCraftingGrid }
        gridButtons.forEach { button ->
            val item = top.getItem(button.slot)
            if (isButton(item)) top.setItem(button.slot, null)
        }

        // Phase 2: Deferred cleanup — catches refunded buttons, re-places all
        Bukkit.getScheduler().runTask(plugin, Runnable {
            clearButtonItems(player)
            ensureButtons(player)
        })
    }

    // ─── Core logic ─────────────────────────────────────────────────────────

    private fun clearButtonItems(player: Player) {
        player.inventory.contents.forEachIndexed { index, item ->
            if (isButton(item)) player.inventory.setItem(index, null)
        }
    }

    private fun ensureButtons(player: Player) {
        if (!processingPlayers.add(player.uniqueId)) return
        try {
            val buttons = config().inventoryButtons

            // Player inventory buttons
            buttons.filter { !it.isCraftingGrid }.forEach { button ->
                val itemEntry = button.item.get(player, null)
                val stack = itemEntry.build(player, null)
                tagButton(stack, button.slot)
                player.inventory.setItem(button.slot, stack)
            }

            // Crafting grid buttons
            val top = player.openInventory.topInventory
            if (top.type == InventoryType.CRAFTING) {
                buttons.filter { it.isCraftingGrid && it.slot in 0..4 }.forEach { button ->
                    val itemEntry = button.item.get(player, null)
                    val stack = itemEntry.build(player, null)
                    tagButton(stack, button.slot)
                    top.setItem(button.slot, stack)
                }
            }
        } finally {
            processingPlayers.remove(player.uniqueId)
        }
    }

    private fun tagButton(stack: org.bukkit.inventory.ItemStack, slot: Int) {
        val meta = stack.itemMeta ?: return
        meta.persistentDataContainer.set(buttonKey, PersistentDataType.INTEGER, slot)
        stack.itemMeta = meta
    }

    private fun isButton(item: org.bukkit.inventory.ItemStack?): Boolean {
        if (item == null || item.type == Material.AIR) return false
        return item.itemMeta?.persistentDataContainer?.has(buttonKey, PersistentDataType.INTEGER) == true
    }

    private fun playerHasButtons(player: Player): Boolean =
        player.inventory.contents.any { isButton(it) }

    private fun handleButtonClick(player: Player, item: org.bukkit.inventory.ItemStack, click: org.bukkit.event.inventory.ClickType) {
        val slot = item.itemMeta?.persistentDataContainer?.get(buttonKey, PersistentDataType.INTEGER) ?: return
        val cfg = config()
        val button = cfg.inventoryButtons.find { it.slot == slot } ?: return

        val ctx = context()
        if (button.criteria.isNotEmpty() && !button.criteria.matches(player, ctx)) return

        val ourClick = mapBukkitClick(click)
        val actions = resolveClickActions(button.clickActions, ourClick, player)
        if (actions.isEmpty()) return

        val execCtx = context()
        val run = Runnable {
            actions.forEach { ref ->
                ref.triggerFor(player, execCtx)
            }
        }

        if (button.delay > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, run, button.delay)
        } else {
            run.run()
        }
    }

    @EventHandler
    fun onUnload(event: TypewriterUnloadEvent) {
        HandlerList.unregisterAll(this)
    }
}
