package org.undo.storage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.bukkit.Location
import org.undo.models.BuildZone
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.*

class JSONStorage(private val file: File) : Storage {

    private val gson: Gson

    init {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(Location::class.java, LocationTypeAdapter())
        gson = gsonBuilder.create()

        if (!file.exists()) {
            try {
                file.createNewFile()
                saveToFile(emptyList())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveToFile(buildZones: List<BuildZone>) {
        try {
            FileWriter(file).use { writer ->
                gson.toJson(buildZones, writer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun loadBuildZones(): List<BuildZone> {
        if (!file.exists()) {
            return emptyList()
        }

        try {
            FileReader(file).use { reader ->
                val type = object : TypeToken<List<BuildZone>>() {}.type
                return gson.fromJson(reader, type)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return emptyList()
        }
    }

    override fun saveBuildZone(buildZone: BuildZone) {
        val buildZones = loadBuildZones().toMutableList()
        buildZones.removeIf { it.id == buildZone.id }
        buildZones.add(buildZone)
        saveToFile(buildZones)
    }

    override fun deleteBuildZone(buildZone: BuildZone) {
        val buildZones = loadBuildZones().toMutableList()
        buildZones.removeIf { it.id == buildZone.id }
        saveToFile(buildZones)
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
