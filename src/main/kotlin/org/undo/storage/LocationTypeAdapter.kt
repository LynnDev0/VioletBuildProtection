package org.undo.storage

import com.google.gson.*
import org.bukkit.Bukkit
import org.bukkit.Location
import java.lang.reflect.Type

class LocationTypeAdapter : JsonSerializer<Location>, JsonDeserializer<Location> {

    override fun serialize(src: Location, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("world", src.world?.name)
        jsonObject.addProperty("x", src.x)
        jsonObject.addProperty("y", src.y)
        jsonObject.addProperty("z", src.z)
        jsonObject.addProperty("yaw", src.yaw)
        jsonObject.addProperty("pitch", src.pitch)
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Location {
        val jsonObject = json.asJsonObject
        val world = Bukkit.getWorld(jsonObject.get("world").asString)
        val x = jsonObject.get("x").asDouble
        val y = jsonObject.get("y").asDouble
        val z = jsonObject.get("z").asDouble
        val yaw = jsonObject.get("yaw").asFloat
        val pitch = jsonObject.get("pitch").asFloat
        return Location(world, x, y, z, yaw, pitch)
    }
}
