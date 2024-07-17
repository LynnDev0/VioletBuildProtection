package org.undo.managers

import org.bukkit.Material
import org.bukkit.block.Block
import org.undo.BuildProtectionPlugin

class WorldManager(private val plugin: BuildProtectionPlugin) {

    fun isAllowedWorld(worldName: String): Boolean {
        val allowedWorlds = plugin.config.getStringList("buildzone.allowed_worlds")
        return allowedWorlds.contains(worldName)
    }

    fun isFarmItem(block: Block): Boolean {
        val farmMaterials = plugin.config.getStringList("buildzone.farm_items").mapNotNull { Material.getMaterial(it) }
        return farmMaterials.contains(block.type)
    }
}
