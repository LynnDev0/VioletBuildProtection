package org.undo.managers

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.undo.BuildProtectionPlugin
import org.undo.models.BuildZone
import org.undo.models.CustomInventoryHolder
import java.util.*

class BuildZoneManager(private val plugin: BuildProtectionPlugin) {

    private val buildZones = mutableListOf<BuildZone>()
    private val borderManager = BorderManager(plugin)
    private val itemManager = ItemManager(plugin)
    private val configManager = ConfigManager(plugin)
    private val worldManager = WorldManager(plugin)

    init {
        buildZones.addAll(plugin.storage.loadBuildZones())
    }

    fun createBuildZone(center: Location, ownerUUID: UUID, size: Int, height: Int): Boolean {
        val worldName = center.world?.name ?: throw IllegalArgumentException("World cannot be null")

        if (buildZones.any { isOverlapping(it, center, size, height) }) {
            val player = Bukkit.getPlayer(ownerUUID)
            player?.sendMessage(plugin.config.getString("messages.zone_overlap", "건축 구역이 다른 구역과 겹칩니다."))
            return false
        }

        val buildZone = BuildZone(
            0,
            ownerUUID,
            center.world?.uid ?: throw IllegalArgumentException("World UUID cannot be null"),
            center,
            size,
            height,
            emptyList()
        )
        plugin.storage.saveBuildZone(buildZone)
        buildZones.add(buildZone)
        borderManager.createBorder(buildZone)
        return true
    }

    fun deleteBuildZone(buildZone: BuildZone) {
        plugin.storage.deleteBuildZone(buildZone)
        buildZones.remove(buildZone)
        borderManager.removeBorder(buildZone)
        val player = Bukkit.getPlayer(buildZone.ownerUUID)
        player?.sendMessage(plugin.config.getString("messages.zone_deleted", "건축 구역이 삭제되었습니다."))
    }

    fun addAdmin(owner: Player, target: Player) {
        val buildZone = getBuildZoneByOwner(owner.uniqueId)
        buildZone?.let {
            plugin.storage.addAdminToZone(it.id, target.uniqueId)
            buildZones.clear()
            buildZones.addAll(plugin.storage.loadBuildZones())
            owner.sendMessage(
                plugin.config.getString("messages.permission_granted", "{player}에게 관리자 권한을 부여했습니다.")
                    ?.replace("{player}", target.name) ?: "{player}에게 관리자 권한을 부여했습니다.".replace("{player}", target.name)
            )
        }
    }

    fun removeAdmin(ownerUUID: UUID, targetUUID: UUID) {
        val buildZone = getBuildZoneByOwner(ownerUUID)
        buildZone?.let {
            plugin.storage.removeAdminFromZone(it.id, targetUUID)
            buildZones.clear()
            buildZones.addAll(plugin.storage.loadBuildZones())
            val owner = Bukkit.getPlayer(ownerUUID)
            val targetPlayer = Bukkit.getOfflinePlayer(targetUUID)
            owner?.sendMessage(
                plugin.config.getString("messages.permission_revoked", "{player}의 관리자 권한을 제거했습니다.")
                    ?.replace("{player}", targetPlayer.name ?: "Unknown") ?: "{player}의 관리자 권한을 제거했습니다.".replace(
                    "{player}",
                    targetPlayer.name ?: "Unknown"
                )
            )
        }
    }

    fun getBuildZoneByOwner(ownerUUID: UUID): BuildZone? {
        return buildZones.find { it.ownerUUID == ownerUUID }
    }

    fun giveBuildZoneItem(player: Player) {
        itemManager.giveBuildZoneItem(player)
    }

    fun setBuildZoneItem(player: Player) {
        itemManager.setBuildZoneItem(player)
    }

    fun reloadConfig() {
        configManager.reloadConfig()
    }

    fun getBuildZoneOwners(): List<UUID> {
        return buildZones.map { it.ownerUUID }
    }

    fun getBuildZoneAtLocation(location: Location): BuildZone? {
        for (buildZone in buildZones) {
            val zoneLocation = buildZone.center
            if (isWithinZone(buildZone, location)) {
                return buildZone
            }
        }
        return null
    }

    fun isInBuildZone(block: Block): Boolean {
        return buildZones.any { isWithinZone(it, block.location) }
    }

    fun isAllowedWorld(worldName: String): Boolean {
        return worldManager.isAllowedWorld(worldName)
    }

    fun isOwnerOrAdmin(player: Player, block: Block): Boolean {
        val buildZone = buildZones.find { isWithinZone(it, block.location) } ?: return false
        return buildZone.ownerUUID == player.uniqueId || isAdmin(buildZone, player)
    }

    fun isAdmin(buildZone: BuildZone, player: Player): Boolean {
        return buildZone.admins.contains(player.uniqueId)
    }


    private fun isWithinZone(buildZone: BuildZone, location: Location): Boolean {
        val center = buildZone.center
        val halfSize = buildZone.size / 2
        val height = buildZone.height
        return location.world?.uid == center.world?.uid &&
                location.x in (center.x - halfSize)..(center.x + halfSize) &&
                location.z in (center.z - halfSize)..(center.z + halfSize) &&
                location.y.toInt() in center.y.toInt()..(center.y.toInt() + height)
    }

    private fun isOverlapping(buildZone: BuildZone, center: Location, size: Int, height: Int): Boolean {
        val halfSize = size / 2
        val minX = center.x - halfSize
        val maxX = center.x + halfSize
        val minZ = center.z - halfSize
        val maxZ = center.z + halfSize
        val minY = center.y
        val maxY = center.y + height

        val zoneCenter = buildZone.center
        val zoneHalfSize = buildZone.size / 2
        val zoneMinX = zoneCenter.x - zoneHalfSize
        val zoneMaxX = zoneCenter.x + zoneHalfSize
        val zoneMinZ = zoneCenter.z - zoneHalfSize
        val zoneMaxZ = zoneCenter.z + zoneHalfSize
        val zoneMinY = zoneCenter.y
        val zoneMaxY = zoneCenter.y + buildZone.height

        return (minX <= zoneMaxX && maxX >= zoneMinX) &&
                (minZ <= zoneMaxZ && maxZ >= zoneMinZ) &&
                (minY <= zoneMaxY && maxY >= zoneMinY)
    }

    fun openBuildZoneGUI(player: Player) {
        val menuConfig = plugin.config.getConfigurationSection("menu") ?: return
        val title = menuConfig.getString("title", "건차 관리") ?: "건차 관리"
        val size = menuConfig.getInt("size", 9)

        val holder = CustomInventoryHolder(title)
        val inventory: Inventory = Bukkit.createInventory(holder, size, title)
        holder.setInventory(inventory)

        val itemsConfig = menuConfig.getConfigurationSection("items") ?: return

        for (key in itemsConfig.getKeys(false)) {
            val itemConfig = itemsConfig.getConfigurationSection(key) ?: continue
            val materialName = itemConfig.getString("material") ?: continue
            val material = Material.matchMaterial(materialName) ?: continue
            val name = itemConfig.getString("name") ?: continue
            val slot = itemConfig.getInt("slot")

            val item = ItemStack(material)
            val meta: ItemMeta? = item.itemMeta
            if (meta != null) {
                meta.setDisplayName(name)
                item.itemMeta = meta
            }

            inventory.setItem(slot, item)
        }

        player.openInventory(inventory)
    }

    fun openRemoveAdminGUI(player: Player) {
        buildZones.clear()
        buildZones.addAll(plugin.storage.loadBuildZones())

        val buildZone = getBuildZoneByOwner(player.uniqueId)
        if (buildZone != null) {
            val inventoryTitle = "권한 뺏기"
            val inventorySize = 54
            val holder = CustomInventoryHolder(inventoryTitle)
            val inventory: Inventory = Bukkit.createInventory(holder, inventorySize, inventoryTitle)

            buildZone.admins.forEach { adminUUID ->
                val admin = Bukkit.getOfflinePlayer(adminUUID)
                val skullItem = ItemStack(Material.PLAYER_HEAD, 1)
                val meta = skullItem.itemMeta as SkullMeta
                meta.owningPlayer = admin
                meta.setDisplayName(admin.name ?: "Unknown")
                skullItem.itemMeta = meta
                inventory.addItem(skullItem)
            }

            player.openInventory(inventory)
        } else {
            player.sendMessage(plugin.config.getString("messages.no_zone_found", "해당 건차 구역을 찾을 수 없습니다."))
        }
    }
}
