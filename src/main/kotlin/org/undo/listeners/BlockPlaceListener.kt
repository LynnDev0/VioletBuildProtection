package org.undo.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.undo.BuildProtectionPlugin

class BlockPlaceListener(private val plugin: BuildProtectionPlugin) : Listener {

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val block = event.block

        // 월드가 사용 가능한지 확인
        if (!plugin.buildZoneManager.isAllowedWorld(player.world.name)) {
            player.sendMessage(plugin.config.getString("messages.zone_not_allowed"))
            event.isCancelled = true
            return
        }

        // 건차 외부에서 농사 제한
        val restrictFarmingOutsideBuildZone = plugin.config.getBoolean("buildzone.restrict_farming_outside", true)
        if (restrictFarmingOutsideBuildZone && !plugin.buildZoneManager.isInBuildZone(block) && plugin.worldManager.isFarmItem(block)) {
            player.sendMessage(plugin.config.getString("messages.cannot_farm_outside", "건차 구역 외부에서는 농사를 지을 수 없습니다."))
            event.isCancelled = true
        }

        // 건차 내에서 블럭 설치 제한
        if (plugin.buildZoneManager.isInBuildZone(block) && !plugin.buildZoneManager.isOwnerOrAdmin(player, block)) {
            player.sendMessage(plugin.config.getString("messages.no_permission"))
            event.isCancelled = true
        }

        // 테두리 블럭은 설치할 수 없음
        val borderBlockName = plugin.config.getString("buildzone.border_block") ?: "BEDROCK"
        val borderBlock = Material.getMaterial(borderBlockName) ?: Material.BEDROCK
        if (block.type == borderBlock) {
            event.isCancelled = true
        }
    }
}