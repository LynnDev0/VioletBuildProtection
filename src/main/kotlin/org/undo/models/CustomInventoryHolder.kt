package org.undo.models

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class CustomInventoryHolder(val title: String) : InventoryHolder {

    private lateinit var inventory: Inventory

    override fun getInventory(): Inventory {
        return inventory
    }

    fun setInventory(inventory: Inventory) {
        this.inventory = inventory
    }
}
