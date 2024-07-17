package org.undo.listeners

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.meta.SkullMeta
import org.undo.BuildProtectionPlugin
import org.undo.models.CustomInventoryHolder
import java.util.concurrent.ConcurrentHashMap

class GUIListener(private val plugin: BuildProtectionPlugin) : Listener {

    private val lastMessageTimes: MutableMap<String, Long> = ConcurrentHashMap()

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val inventory = event.inventory

        if (inventory.holder is CustomInventoryHolder) {
            val holder = inventory.holder as CustomInventoryHolder
            when (holder.title) {
                plugin.config.getString("gui.title", "건차 관리") -> {
                    event.isCancelled = true
                    when (event.slot) {
                        plugin.config.getInt("gui.give_permission_slot", 0) -> {
                            sendMessageWithCooldown(player, "enter_give_permission", plugin.config.getString("messages.enter_give_permission", "관리자 권한을 줄 플레이어의 닉네임을 입력하세요 (30초 제한)."))
                            plugin.waitingForPermission[player.uniqueId] = Pair("give", System.currentTimeMillis())
                            player.closeInventory()
                        }
                        plugin.config.getInt("gui.remove_permission_slot", 1) -> {
                            plugin.buildZoneManager.openRemoveAdminGUI(player)
                        }
                        plugin.config.getInt("gui.delete_zone_slot", 8) -> {
                            val buildZone = plugin.buildZoneManager.getBuildZoneByOwner(player.uniqueId)
                            if (buildZone != null) {
                                plugin.buildZoneManager.deleteBuildZone(buildZone)
                            } else {
                                sendMessageWithCooldown(player, "no_zone_to_delete", plugin.config.getString("messages.no_zone_to_delete", "삭제할 건축 구역이 없습니다."))
                            }
                            player.closeInventory()
                        }
                    }
                }
                "권한 뺏기" -> {
                    event.isCancelled = true
                    val clickedItem = event.currentItem
                    if (clickedItem != null && clickedItem.type == Material.PLAYER_HEAD) {
                        val meta = clickedItem.itemMeta as? SkullMeta
                        val targetUUID = meta?.owningPlayer?.uniqueId
                        if (targetUUID != null) {
                            val buildZone = plugin.buildZoneManager.getBuildZoneByOwner(player.uniqueId)
                            if (buildZone != null && buildZone.admins.contains(targetUUID)) {
                                plugin.buildZoneManager.removeAdmin(player.uniqueId, targetUUID)
                            } else {
                                val message = plugin.config.getString("messages.no_permission", "{player}은(는) 관리자 권한이 없습니다.")?.replace("{player}", meta?.displayName ?: "Unknown")
                                sendMessageWithCooldown(player, "no_permission", message)
                            }
                        }
                    }
                    player.closeInventory()
                }
            }
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val inventory = event.inventory

        if (inventory.holder is CustomInventoryHolder) {
            val holder = inventory.holder as CustomInventoryHolder
            if (holder.title == plugin.config.getString("gui.title", "건차 관리") ||
                holder.title == "권한 뺏기") {
                event.isCancelled = true
            }
        }
    }

    private fun sendMessageWithCooldown(player: Player, messageType: String, message: String?) {
        val uuid = player.uniqueId
        val currentTime = System.currentTimeMillis()
        val lastMessageTime = lastMessageTimes["$uuid-$messageType"]

        if (lastMessageTime == null || currentTime - lastMessageTime > 5000) {
            lastMessageTimes["$uuid-$messageType"] = currentTime
            message?.let { player.sendMessage(it) }
        }
    }
}
