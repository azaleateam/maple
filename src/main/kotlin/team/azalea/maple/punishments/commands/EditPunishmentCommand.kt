@file:Command(
    name = "editpunishment",
    description = "Edits a punishment",
    usage = "<id>",
    permission = "maple.staff.punishments.edit",
)

package team.azalea.maple.punishments.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import me.honkling.commando.common.annotations.Command
import me.honkling.pocket.GUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import team.azalea.maple.maplePlugin
import team.azalea.maple.messageUtil
import team.azalea.maple.punishments.*
import team.azalea.maple.punishments.commands.Items.generateDurationItem
import team.azalea.maple.punishments.commands.Items.generateEditItem
import team.azalea.maple.punishments.commands.Items.generateNotesItem
import team.azalea.maple.punishments.commands.Items.generateRevertItem
import team.azalea.maple.util.*

private object Items {
    fun GUI.generateRevertItem(punishment: PunishmentData, player: Player) {
        val revertItem = ItemStack(Material.BARRIER).apply {
            val newItemMeta = itemMeta.clone()
            newItemMeta.displayName(messageUtil.translate("punishments.commands.edit.items.revert.text").mm())

            newItemMeta.lore(
                messageUtil.getLore("punishments.commands.edit.items.revert.${
                    if(punishment.type == PunishmentTypes.KICK.ordinal) "noReversion" else "lore"
                }")
            )

            this.itemMeta = newItemMeta
        }

        this.put('a', revertItem) {
            if(punishment.type == PunishmentTypes.KICK.ordinal) return@put

            if(punishment.revertedAt != null) {
                player.sendKey("punishments.commands.edit.items.revert.alreadyReverted")
                return@put
            }

            player.closeInventory()
            player.performCommand("revertpun ${punishment.id}")
        }
    }

    fun GUI.generateEditItem(punishment: PunishmentData, player: Player) {
        val reasonInput = fetchInput(player, bypassFilter = true) {
            val reason = it
            maplePlugin.launch(maplePlugin.asyncDispatcher) {
                Punishments.updateReason(punishment.id, reason)
                player.sendKey("punishments.commands.edit.items.reason.success")
            }
        }

        val editReasonItem = ItemStack(Material.PAPER).apply {
            val newItemMeta = itemMeta.clone()
            newItemMeta.displayName(
                messageUtil.translate("punishments.commands.edit.items.reason.text").mm()
            )
            newItemMeta.lore(
                messageUtil.getLore("punishments.commands.edit.items.reason.lore")
            )
            this.itemMeta = newItemMeta
        }

        this.put('b', editReasonItem) {
            player.sendMessage("<s>Enter the new reason below:".mm())
            player.closeInventory()
            reasonInput.prompt()
        }
    }

    fun GUI.generateDurationItem(punishment: PunishmentData, player: Player) {
        val durationInput = fetchInput(player, bypassFilter = true) {
            maplePlugin.launch(maplePlugin.asyncDispatcher) {
                val duration = convertDate(it)
                Punishments.updateDuration(punishment.id, duration)
                player.sendKey("punishments.commands.edit.items.duration.success")
            }
        }

        val durationItem = ItemStack(Material.CLOCK).apply {
            val newItemMeta = itemMeta.clone()
            newItemMeta.displayName(
                messageUtil.translate("punishments.commands.edit.items.duration.text").mm()
            )

            newItemMeta.lore(
                messageUtil.getLore("punishments.commands.edit.items.duration.${
                    if(punishment.type == PunishmentTypes.KICK.ordinal) "noEditDuration" else "lore"
                }")
            )

            this.itemMeta = newItemMeta
        }
        this.put('c', durationItem) {
            if(punishment.type == PunishmentTypes.KICK.ordinal) return@put

            player.sendKey("punishments.commands.edit.items.duration.input")
            player.closeInventory()
            durationInput.prompt()
        }
    }

    fun GUI.generateNotesItem(punishment: PunishmentData, player: Player) {
        val notesInput = fetchInput(player, bypassFilter = true) {
            val inputText = it

            val newNotes = if(punishment.notes.isNullOrEmpty()) inputText else "${punishment.notes}\n\n$inputText"

            maplePlugin.launch(maplePlugin.asyncDispatcher) {
                Punishments.updateNotes(punishment.id, newNotes)
                player.sendKey("punishments.commands.edit.items.notes.success")
            }
        }

        val confirmNoteWipeInput = fetchInput(player) {
            if(!player.hasPermission("maple.staff.punishments.edit.notes.clear")) {
                player.sendKey("punishments.commands.edit.items.notes.noClearPermission")
                return@fetchInput
            }

            if (!isConfirmed(it)) {
                player.sendKey("punishments.commands.edit.items.notes.clearCancelled")
                return@fetchInput
            }

            maplePlugin.launch(maplePlugin.asyncDispatcher) {
                Punishments.updateNotes(punishment.id, null)
            }

            player.sendKey("punishments.commands.edit.items.notes.cleared")
        }

        val notesItem = ItemStack(Material.BOOK).apply {
            val newItemMeta = itemMeta.clone()
            newItemMeta.displayName(
                messageUtil.translate("punishments.commands.edit.items.notes.text").mm()
            )
            newItemMeta.lore(
                messageUtil.getLore("punishments.commands.edit.items.notes.lore")
            )
            this.itemMeta = newItemMeta
        }

        this.put('d', notesItem) {
            if(it.click == ClickType.LEFT) {
                player.sendKey("punishments.commands.edit.items.notes.input")
                player.closeInventory()
                notesInput.prompt()
                return@put
            }

            if(it.click == ClickType.RIGHT) {
                player.closeInventory()
                player.sendKey("punishments.commands.edit.items.notes.confirmClear")
                confirmNoteWipeInput.prompt()
            }
        }
    }
}

fun editPunishment(
    player: Player, id: String
) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val punishment = Punishments.get(id)

    if(punishment === null) {
        player.sendKey("punishments.invalidId", "id" to id)
        return@launch
    }

    val template = """
            xxxxxxxxx
            xaxbxcxdx
            xxxxxxxxx
    """.trimIndent()
    val gui = GUI(maplePlugin, template, "Edit Punishment")

    val borderItem = ItemStack(Material.PURPLE_STAINED_GLASS_PANE).apply {
        val newItemMeta = itemMeta.clone()
        newItemMeta.displayName("<reset>".mm())
        this.itemMeta = newItemMeta
    }
    gui.put('x', borderItem)

    gui.generateRevertItem(punishment, player)
    gui.generateEditItem(punishment, player)
    gui.generateDurationItem(punishment, player)
    gui.generateNotesItem(punishment, player)

    maplePlugin.launch(maplePlugin.minecraftDispatcher) {
        gui.open(player)
    }
}