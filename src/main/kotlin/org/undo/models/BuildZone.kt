package org.undo.models

import org.bukkit.Location
import java.util.*

data class BuildZone(
    val id: Int,
    val ownerUUID: UUID,
    val worldUUID: UUID,
    val center: Location,
    val size: Int,
    val height: Int,
    val admins: List<UUID>
)

