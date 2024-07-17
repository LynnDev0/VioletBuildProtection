package org.undo.managers

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.metadata.FixedMetadataValue
import org.undo.BuildProtectionPlugin
import org.undo.models.BuildZone

class BorderManager(private val plugin: BuildProtectionPlugin) {

    fun createBorder(buildZone: BuildZone) {
        val center = buildZone.center
        val halfSize = buildZone.size / 2
        val height = buildZone.height

        val world = center.world ?: throw IllegalArgumentException("World cannot be null")
        val yStart = center.y.toInt()
        val yEnd = yStart + height

        val borderBlockName = plugin.config.getString("buildzone.border_block", "BEDROCK") ?: "BEDROCK"
        val borderBlock = Material.getMaterial(borderBlockName) ?: Material.BEDROCK

        val beaconBlock = world.getBlockAt(center)
        beaconBlock.setType(Material.BEACON)
        beaconBlock.setMetadata("buildZoneBlock", FixedMetadataValue(plugin, true))

        for (y in yStart..yEnd) {
            for (x in listOf((center.x - halfSize).toInt(), (center.x + halfSize).toInt())) {
                for (z in listOf((center.z - halfSize).toInt(), (center.z + halfSize).toInt())) {
                    setBlockAlways(world.getBlockAt(x, y, z), borderBlock)
                }
            }
        }

        for (x in (center.x - halfSize).toInt()..(center.x + halfSize).toInt()) {
            setBlockAlways(world.getBlockAt(x, yStart, (center.z - halfSize).toInt()), borderBlock)
            setBlockAlways(world.getBlockAt(x, yStart, (center.z + halfSize).toInt()), borderBlock)
        }
        for (z in (center.z - halfSize).toInt()..(center.z + halfSize).toInt()) {
            setBlockAlways(world.getBlockAt((center.x - halfSize).toInt(), yStart, z), borderBlock)
            setBlockAlways(world.getBlockAt((center.x + halfSize).toInt(), yStart, z), borderBlock)
        }

        for (x in (center.x - halfSize).toInt()..(center.x + halfSize).toInt()) {
            setBlockAlways(world.getBlockAt(x, yEnd, (center.z - halfSize).toInt()), borderBlock)
            setBlockAlways(world.getBlockAt(x, yEnd, (center.z + halfSize).toInt()), borderBlock)
        }
        for (z in (center.z - halfSize).toInt()..(center.z + halfSize).toInt()) {
            setBlockAlways(world.getBlockAt((center.x - halfSize).toInt(), yEnd, z), borderBlock)
            setBlockAlways(world.getBlockAt((center.x + halfSize).toInt(), yEnd, z), borderBlock)
        }
    }

    fun removeBorder(buildZone: BuildZone) {
        val center = buildZone.center
        val halfSize = buildZone.size / 2
        val height = buildZone.height

        val world = center.world ?: throw IllegalArgumentException("World cannot be null")
        val yStart = center.y.toInt()
        val yEnd = yStart + height

        world.getBlockAt(center).setType(Material.AIR)

        for (y in yStart..yEnd) {
            for (x in listOf((center.x - halfSize).toInt(), (center.x + halfSize).toInt())) {
                for (z in listOf((center.z - halfSize).toInt(), (center.z + halfSize).toInt())) {
                    removeBlockAlways(world.getBlockAt(x, y, z))
                }
            }
        }

        for (x in (center.x - halfSize).toInt()..(center.x + halfSize).toInt()) {
            removeBlockAlways(world.getBlockAt(x, yStart, (center.z - halfSize).toInt()))
            removeBlockAlways(world.getBlockAt(x, yStart, (center.z + halfSize).toInt()))
        }
        for (z in (center.z - halfSize).toInt()..(center.z + halfSize).toInt()) {
            removeBlockAlways(world.getBlockAt((center.x - halfSize).toInt(), yStart, z))
            removeBlockAlways(world.getBlockAt((center.x + halfSize).toInt(), yStart, z))
        }

        for (x in (center.x - halfSize).toInt()..(center.x + halfSize).toInt()) {
            removeBlockAlways(world.getBlockAt(x, yEnd, (center.z - halfSize).toInt()))
            removeBlockAlways(world.getBlockAt(x, yEnd, (center.z + halfSize).toInt()))
        }
        for (z in (center.z - halfSize).toInt()..(center.z + halfSize).toInt()) {
            removeBlockAlways(world.getBlockAt((center.x - halfSize).toInt(), yEnd, z))
            removeBlockAlways(world.getBlockAt((center.x + halfSize).toInt(), yEnd, z))
        }
    }

    private fun setBlockAlways(block: Block, material: Material) {
        block.setType(material)
        block.setMetadata("buildZoneBlock", FixedMetadataValue(plugin, true))
    }

    private fun removeBlockAlways(block: Block) {
        block.setType(Material.AIR)
    }
}
