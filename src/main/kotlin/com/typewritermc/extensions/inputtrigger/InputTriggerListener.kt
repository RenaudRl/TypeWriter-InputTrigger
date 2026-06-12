package com.typewritermc.extensions.inputtrigger

import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.ref
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.TypewriterPaperPlugin
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.events.TypewriterUnloadEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.java.JavaPlugin
import io.papermc.paper.event.player.AsyncChatEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Unified listener that handles all input triggers.
 * Each input type is mapped to the appropriate Bukkit event.
 *
 * Standalone version — uses Bukkit scheduler (Paper-compatible, no Folia).
 */
class InputTriggerListener : Listener {

    private val plugin = JavaPlugin.getPlugin(TypewriterPaperPlugin::class.java)

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    /**
     * Per-player per-input-type last-trigger-tick map for cooldown.
     * Key format: "playerUuid:InputType"
     */
    private val cooldownTracker = ConcurrentHashMap<String, Long>()

    private fun config(): InputTriggerConfigEntry {
        return try {
            Query.find<InputTriggerConfigEntry>().firstOrNull()
        } catch (_: Exception) { null }
            ?: InputTriggerConfigEntry()
    }

    // ─── SWAP_HAND ──────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        handleTrigger(event.player, InputType.SWAP_HAND) {
            if (it.cancelAction) event.isCancelled = true
        }
    }

    // ─── DROP ───────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDropItem(event: PlayerDropItemEvent) {
        if (event.player.itemOnCursor.type != Material.AIR) return

        val def = findDefinition(InputType.DROP) ?: return
        if (!def.enabled) return

        val ctx = context()
        if (def.criteria.isNotEmpty() && !def.criteria.matches(event.player, ctx)) return

        when {
            def.dropCount == -1 -> {
                if (def.cancelAction) {
                    event.isCancelled = true
                    val hand = event.player.inventory.getItem(EquipmentSlot.HAND)
                    if (hand.type != Material.AIR) {
                        event.itemDrop.itemStack = hand.clone()
                        event.player.inventory.setItem(EquipmentSlot.HAND, null)
                    }
                }
            }
            def.dropCount > 1 -> {
                if (def.cancelAction) {
                    event.isCancelled = true
                    val hand = event.player.inventory.getItem(EquipmentSlot.HAND)
                    if (hand.type != Material.AIR) {
                        val toDrop = minOf(def.dropCount, hand.amount)
                        val clone = hand.clone()
                        clone.amount = toDrop
                        event.itemDrop.itemStack = clone
                        hand.amount -= toDrop
                    }
                }
            }
            else -> {
                if (def.cancelAction) event.isCancelled = true
            }
        }

        if (def.actions.isNotEmpty()) {
            executeActions(event.player, def)
        }
    }

    // ─── SNEAK ──────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onSneak(event: PlayerToggleSneakEvent) {
        if (!event.isSneaking) return
        handleTrigger(event.player, InputType.SNEAK) {
            if (it.cancelAction) event.isCancelled = true
        }
    }

    // ─── SPRINT ─────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onSprint(event: PlayerToggleSprintEvent) {
        if (!event.isSprinting) return
        handleTrigger(event.player, InputType.SPRINT) {
            if (it.cancelAction) event.isCancelled = true
        }
    }

    // ─── CHAT ───────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChat(event: AsyncChatEvent) {
        handleTrigger(event.player, InputType.CHAT) {
            if (it.cancelAction) event.isCancelled = true
        }
    }

    // ─── COMMAND ────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        handleTrigger(event.player, InputType.COMMAND) {
            if (it.cancelAction) event.isCancelled = true
        }
    }

    // ─── HOTBAR_SLOT ────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onHotbarChange(event: PlayerItemHeldEvent) {
        if (event.previousSlot == event.newSlot) return
        val def = findDefinition(InputType.HOTBAR_SLOT) ?: return
        if (!def.enabled) return
        if (def.hotbarSlot != event.newSlot) return

        handleTrigger(event.player, InputType.HOTBAR_SLOT) {
            // cancelAction does not apply meaningfully to hotbar changes
        }
    }

    // ─── Cooldown ─────────────────────────────────────────────────────────────

    private fun cooldownKey(playerId: UUID, type: InputType): String =
        "$playerId:$type"

    private fun checkAndUpdateCooldown(player: org.bukkit.entity.Player, def: InputDefinition): Boolean {
        if (def.cooldown <= 0L) return true
        val key = cooldownKey(player.uniqueId, def.type)
        val currentTick = Bukkit.getCurrentTick().toLong()
        val lastTick = cooldownTracker[key] ?: 0L
        if (currentTick - lastTick < def.cooldown) return false
        cooldownTracker[key] = currentTick
        return true
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private fun findDefinition(type: InputType): InputDefinition? {
        return config().keybinds.find { it.type == type }
    }

    private inline fun handleTrigger(
        player: org.bukkit.entity.Player,
        type: InputType,
        cancel: (InputDefinition) -> Unit
    ) {
        val def = findDefinition(type) ?: return
        if (!def.enabled) return

        val ctx = context()
        if (def.criteria.isNotEmpty() && !def.criteria.matches(player, ctx)) return

        cancel(def)

        if (def.actions.isNotEmpty()) {
            if (!checkAndUpdateCooldown(player, def)) return
            executeActions(player, def)
        }
    }

    private fun executeActions(player: org.bukkit.entity.Player, def: InputDefinition) {
        val ctx = context()
        val run = Runnable {
            def.actions.forEach { ref ->
                ref.triggerFor(player, ctx)
            }
        }

        if (def.delay > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, run, def.delay)
        } else {
            run.run()
        }
    }

    @EventHandler
    fun onUnload(event: TypewriterUnloadEvent) {
        HandlerList.unregisterAll(this)
    }
}
