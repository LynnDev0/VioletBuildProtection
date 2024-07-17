package org.undo.managers

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.undo.BuildProtectionPlugin

class ItemManager(private val plugin: BuildProtectionPlugin) {

    fun giveBuildZoneItem(player: Player) {
        val buildZoneItem = getBuildZoneItem()
        player.inventory.addItem(buildZoneItem)
        player.sendMessage(plugin.config.getString("messages.item_given", "건차 아이템을 지급받았습니다."))
    }

    fun setBuildZoneItem(player: Player) {
        val itemInHand = player.inventory.itemInMainHand

        if (itemInHand.type != Material.AIR) {
            plugin.config.set("buildzone.item.material", itemInHand.type.name)
            val meta = itemInHand.itemMeta
            if (meta != null) {
                plugin.config.set("buildzone.item.name", meta.displayName)
            }
            plugin.saveConfig()
            player.sendMessage(plugin.config.getString("messages.item_set", "건차 아이템이 설정되었습니다."))
        } else {
            player.sendMessage(plugin.config.getString("messages.item_set_error", "손에 아이템을 들고 있어야 합니다."))
        }
    }

    fun getBuildZoneItem(): ItemStack {
        val itemConfig = plugin.config.getConfigurationSection("buildzone.item") ?: return ItemStack(Material.STICK)
        val materialName = itemConfig.getString("material", "STICK") ?: "STICK"  // 기본값으로 "STICK"을 설정
        val material = Material.matchMaterial(materialName) ?: Material.STICK
        val item = ItemStack(material)
        val meta = item.itemMeta
        val itemName = itemConfig.getString("name")
        if (meta != null && itemName != null) {
            meta.setDisplayName(itemName)
            item.itemMeta = meta
        }
        return item
    }
}
