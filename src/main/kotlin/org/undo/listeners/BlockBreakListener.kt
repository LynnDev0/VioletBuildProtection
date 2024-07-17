package org.undo.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.undo.BuildProtectionPlugin

class BlockBreakListener(private val plugin: BuildProtectionPlugin) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block

        if (!plugin.buildZoneManager.isAllowedWorld(player.world.name)) {
            player.sendMessage(plugin.config.getString("messages.zone_not_allowed"))
            event.isCancelled = true
            return
        }

        if (plugin.buildZoneManager.isInBuildZone(block) && !plugin.buildZoneManager.isOwnerOrAdmin(player, block)) {
            player.sendMessage(plugin.config.getString("messages.no_permission"))
            event.isCancelled = true
            return
        }

        if (block.hasMetadata("buildZoneBlock")) {
            event.isCancelled = true
            player.sendMessage(plugin.config.getString("messages.cannot_break", "이 블럭은 부술 수 없습니다."))
        }
    }
}
