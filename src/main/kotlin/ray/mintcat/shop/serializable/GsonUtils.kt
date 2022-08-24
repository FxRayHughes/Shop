package ray.mintcat.shop.serializable

import com.comphenix.protocol.utility.StreamSerializer
import com.google.common.base.Enums
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import taboolib.library.xseries.XMaterial
import taboolib.library.xseries.getItemStack
import taboolib.library.xseries.parseToMaterial
import taboolib.library.xseries.setItemStack
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.platform.util.buildItem

object GsonUtils {

    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().apply {
        registerTypeAdapter(
            ItemStack::class.java,
            JsonSerializer<ItemStack> { src, _, _ ->
                try {
                    JsonPrimitive(StreamSerializer().serializeItemStack(src))
                } catch (_: Exception) {
                    JsonPrimitive(Configuration.empty(type = Type.FAST_JSON).apply {
                        setItemStack("value", src)
                    }.saveToString())
                }


            }
        )
        registerTypeAdapter(
            ItemStack::class.java,
            JsonDeserializer { src, _, _ ->
                try {
                    StreamSerializer().deserializeItemStack(src.asString)
                } catch (_: Exception) {
                    Configuration.loadFromString(src.asString, Type.FAST_JSON)
                        .getItemStack("value") ?: buildItem(XMaterial.STONE)
                }
            }
        )
        registerTypeAdapter(
            Vector::class.java,
            JsonSerializer<Vector> { a, _, _ ->
                JsonPrimitive("${a.x},${a.y},${a.z}")
            }
        )
        registerTypeAdapter(
            Vector::class.java,
            JsonDeserializer { a, _, _ ->
                a.asString.split(",").run { Vector(this[0].toDouble(), this[1].toDouble(), this[2].toDouble()) }
            }
        )
        registerTypeAdapter(
            Material::class.java,
            JsonSerializer<Material> { a, _, _ ->
                JsonPrimitive(a.name)
            }
        )
        registerTypeAdapter(
            Material::class.java,
            JsonDeserializer { a, _, _ ->
                a.asString.parseToMaterial()
            }
        )
        registerTypeAdapter(
            BlockFace::class.java,
            JsonSerializer<BlockFace> { a, _, _ ->
                JsonPrimitive(a.name)
            }
        )
        registerTypeAdapter(
            BlockFace::class.java,
            JsonDeserializer { a, _, _ ->
                Enums.getIfPresent(BlockFace::class.java, a.asString).or(BlockFace.SELF)
            }
        )
    }.create()!!

}