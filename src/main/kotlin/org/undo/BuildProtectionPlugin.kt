package org.undo

import org.bukkit.plugin.java.JavaPlugin
import org.undo.commands.BPCommand
import org.undo.listeners.*
import org.undo.managers.BuildZoneManager
import org.undo.managers.ItemManager
import org.undo.managers.WorldManager
import org.undo.storage.JSONStorage
import org.undo.storage.MySQLStorage
import org.undo.storage.SQLiteStorage
import org.undo.storage.Storage
import java.io.File
import java.util.*

class BuildProtectionPlugin : JavaPlugin() {

    lateinit var storage: Storage
        private set
    lateinit var buildZoneManager: BuildZoneManager
        private set
    lateinit var itemManager: ItemManager
        private set
    lateinit var worldManager: WorldManager
        private set

    val waitingForPermission = mutableMapOf<UUID, Pair<String, Long>>() // 초기화

    override fun onEnable() {

        saveDefaultConfig()
        reloadConfig()


        loadStorage()

        buildZoneManager = BuildZoneManager(this)
        itemManager = ItemManager(this)
        worldManager = WorldManager(this)


        server.pluginManager.registerEvents(BlockBreakListener(this), this)
        server.pluginManager.registerEvents(BlockPlaceListener(this), this)
        server.pluginManager.registerEvents(PlayerInteractListener(this), this)
        server.pluginManager.registerEvents(GUIListener(this), this)
        server.pluginManager.registerEvents(ChatListener(this), this) // ChatListener 등록


        val bpCommand = BPCommand(this)
        getCommand("bp")?.setExecutor(bpCommand)
        getCommand("bp")?.tabCompleter = bpCommand

        logger.info("BuildProtectionPlugin has been enabled.")
    }

    override fun onDisable() {
        logger.info("BuildProtectionPlugin has been disabled.")
    }

    fun loadStorage() {
        val dbType = config.getString("database.type", "json") ?: "json"

        storage = when (dbType) {
            "mysql" -> {
                val host = config.getString("database.mysql.host") ?: "localhost"
                val port = config.getInt("database.mysql.port", 3306)
                val database = config.getString("database.mysql.database") ?: "minecraft"
                val username = config.getString("database.mysql.username") ?: "root"
                val password = config.getString("database.mysql.password") ?: "password"
                MySQLStorage(host, port, database, username, password)
            }
            "sqlite" -> {
                val file = config.getString("database.sqlite.file") ?: "data.sqlite"
                SQLiteStorage(file)
            }
            else -> {
                val file = File(dataFolder, "build_zones.json")
                JSONStorage(file)
            }
        }
    }
}
