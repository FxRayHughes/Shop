package ray.mintcat.shop.serializable

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Bukkit
import org.bukkit.Location

object LocationSerializable : KSerializer<Location> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("org.bukkit.Location")

    override fun deserialize(decoder: Decoder): Location {
        return decoder.decodeString().toLocation()
    }

    override fun serialize(encoder: Encoder, value: Location) {
        encoder.encodeString(value.toStringSave())
    }

    private fun Location.toStringSave(): String {
        return "${world?.name ?: "World"}__${x}__${y}__${z}__${yaw}__${pitch}"
    }

    private fun String.toLocation(): Location {
        val list = split("__")
        return Location(
            Bukkit.getWorld(list[0]),
            list[1].toDouble(),
            list[2].toDouble(),
            list[3].toDouble(),
            list[4].toFloat(),
            list[5].toFloat()
        )
    }

}