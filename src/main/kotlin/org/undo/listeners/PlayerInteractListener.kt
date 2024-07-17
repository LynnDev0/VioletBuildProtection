package org.undo.listeners

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.undo.BuildProtectionPlugin

class PlayerInteractListener(private val plugin: BuildProtectionPlugin) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val block = event.clickedBlock
        val item = event.item
        if (event.action != Action.RIGHT_CLICK_BLOCK || event.hand != org.bukkit.inventory.EquipmentSlot.HAND) {
            return
        }

        if (!plugin.worldManager.isAllowedWorld(player.world.name)) {
            player.sendMessage(plugin.config.getString("messages.zone_not_allowed", "이 월드에서는 건축 구역을 사용할 수 없습니다."))
            event.isCancelled = true
            return
        }

        if (block != null && block.type == Material.BEACON) {
            val buildZone = plugin.buildZoneManager.getBuildZoneAtLocation(block.location)
            if (buildZone != null && buildZone.ownerUUID != player.uniqueId) {
                event.isCancelled = true
                return
            }

            plugin.logger.info("Beacon right-click detected by ${player.name}")
            if (plugin.buildZoneManager.isInBuildZone(block)) {
                plugin.buildZoneManager.openBuildZoneGUI(player)
                event.isCancelled = true
                return
            }
        }

        try {
            val buildZoneItem = plugin.itemManager.getBuildZoneItem()
            plugin.logger.info("Player interacted with item: ${item?.type}, BuildZone item: ${buildZoneItem.type}")

            if (item != null && item.isSimilar(buildZoneItem)) {
                val location = player.location
                plugin.logger.info("Attempting to create build zone at location: ${location.x}, ${location.y}, ${location.z}")

                val ownerUUID = Bukkit.getOfflinePlayer(player.name)?.uniqueId ?: return
                val success = plugin.buildZoneManager.createBuildZone(location, ownerUUID, plugin.config.getInt("buildzone.default_size"), plugin.config.getInt("buildzone.default_height"))
                if (success) {
                    player.sendMessage(plugin.config.getString("messages.zone_created", "건차 구역이 성공적으로 생성되었습니다."))
                    if (item.amount > 1) {
                        item.amount -= 1
                    } else {
                        player.inventory.removeItem(item)
                    }
                } else {
                    player.sendMessage(plugin.config.getString("messages.zone_overlap", "건차 구역이 다른 구역과 겹칩니다."))
                }
                event.isCancelled = true
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error during PlayerInteractEvent: ${e.message}")
            e.printStackTrace()
        }
    }
}
