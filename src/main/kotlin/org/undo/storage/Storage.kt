package org.undo.storage

import org.undo.models.BuildZone
import java.util.*

interface Storage {
    fun loadBuildZones(): List<BuildZone>
    fun saveBuildZone(buildZone: BuildZone)
    fun deleteBuildZone(buildZone: BuildZone)
    fun addAdminToZone(zoneId: Int, adminUUID: UUID)
    fun removeAdminFromZone(zoneId: Int, adminUUID: UUID)
}
