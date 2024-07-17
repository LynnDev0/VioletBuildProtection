package org.undo.managers

import org.undo.BuildProtectionPlugin

class ConfigManager(private val plugin: BuildProtectionPlugin) {

    fun reloadConfig() {
        plugin.reloadConfig()
        plugin.loadStorage()
    }
}
