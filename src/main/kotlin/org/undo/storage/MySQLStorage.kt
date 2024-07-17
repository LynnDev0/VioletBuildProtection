package org.undo.storage

import org.bukkit.Bukkit
import org.bukkit.Location
import org.undo.models.BuildZone
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class MySQLStorage(
    host: String,
    port: Int,
    database: String,
    username: String,
    password: String
) : Storage {

    private val connection: Connection

    init {
        connection = DriverManager.getConnection("jdbc:mysql://$host:$port/$database", username, password)
        createTable()
    }

    private fun createTable() {
        val statement = connection.createStatement()
        statement.execute(
            """CREATE TABLE IF NOT EXISTS build_zones (
                id INT AUTO_INCREMENT PRIMARY KEY,
                ownerUUID CHAR(36),
                worldUUID CHAR(36),
                centerX DOUBLE,
                centerY DOUBLE,
                centerZ DOUBLE,
                size INT,
                height INT,
                admins TEXT
            )"""
        )
    }

    override fun loadBuildZones(): List<BuildZone> {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM build_zones")

        val buildZones = mutableListOf<BuildZone>()
        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val ownerUUID = UUID.fromString(resultSet.getString("ownerUUID"))
            val worldUUID = UUID.fromString(resultSet.getString("worldUUID"))
            val centerX = resultSet.getDouble("centerX")
            val centerY = resultSet.getDouble("centerY")
            val centerZ = resultSet.getDouble("centerZ")
            val size = resultSet.getInt("size")
            val height = resultSet.getInt("height")
            val adminsString = resultSet.getString("admins")
            val admins = if (adminsString.isNullOrEmpty()) {
                emptyList()
            } else {
                adminsString.split(",").mapNotNull { UUID.fromString(it.trim()) }
            }

            val location = Location(Bukkit.getWorld(worldUUID), centerX, centerY, centerZ)
            val buildZone = BuildZone(id, ownerUUID, worldUUID, location, size, height, admins)
            buildZones.add(buildZone)
        }
        return buildZones
    }

    override fun saveBuildZone(buildZone: BuildZone) {
        val statement = connection.prepareStatement(
            "INSERT INTO build_zones (id, ownerUUID, worldUUID, centerX, centerY, centerZ, size, height, admins) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE ownerUUID = VALUES(ownerUUID), worldUUID = VALUES(worldUUID), centerX = VALUES(centerX), centerY = VALUES(centerY), centerZ = VALUES(centerZ), size = VALUES(size), height = VALUES(height), admins = VALUES(admins)"
        )
        statement.setInt(1, buildZone.id)
        statement.setString(2, buildZone.ownerUUID.toString())
        statement.setString(3, buildZone.worldUUID.toString())
        statement.setDouble(4, buildZone.center.x)
        statement.setDouble(5, buildZone.center.y)
        statement.setDouble(6, buildZone.center.z)
        statement.setInt(7, buildZone.size)
        statement.setInt(8, buildZone.height)
        statement.setString(9, buildZone.admins.joinToString(",") { it.toString() })
        statement.executeUpdate()
    }

    override fun deleteBuildZone(buildZone: BuildZone) {
        val statement = connection.prepareStatement(
            "DELETE FROM build_zones WHERE id = ?"
        )
        statement.setInt(1, buildZone.id)
        statement.executeUpdate()
    }

    override fun addAdminToZone(zoneId: Int, adminUUID: UUID) {
        val buildZones = loadBuildZones().toMutableList()
        val zone = buildZones.find { it.id == zoneId }
        if (zone != null) {
            val updatedAdmins = zone.admins.toMutableList()
            if (!updatedAdmins.contains(adminUUID)) {
                updatedAdmins.add(adminUUID)
                val updatedZone = zone.copy(admins = updatedAdmins)
                saveBuildZone(updatedZone)
            }
        }
    }

    override fun removeAdminFromZone(zoneId: Int, adminUUID: UUID) {
        val buildZones = loadBuildZones().toMutableList()
        val zone = buildZones.find { it.id == zoneId }
        if (zone != null) {
            val updatedAdmins = zone.admins.toMutableList()
            if (updatedAdmins.contains(adminUUID)) {
                updatedAdmins.remove(adminUUID)
                val updatedZone = zone.copy(admins = updatedAdmins)
                saveBuildZone(updatedZone)
            }
        }
    }
}

