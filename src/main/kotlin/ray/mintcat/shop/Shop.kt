package ray.mintcat.shop

import kotlinx.serialization.json.Json
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.module.configuration.createLocal
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@RuntimeDependencies(
    RuntimeDependency(
        value = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2",
        relocate = ["!kotlin.", "!kotlin@kotlin_version_escape@."]
    ),
    RuntimeDependency(
        value = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2",
        relocate = ["!kotlin.", "!kotlin@kotlin_version_escape@."]
    ),
)
object Shop : Plugin() {

    val copy = HashMap<UUID, ShopCommodityData>()

    val data by lazy {
        createLocal("data.yml")
    }

    val datas = ArrayList<ShopData>()

    val json = Json {
        coerceInputValues = true
    }

    @Awake(LifeCycle.ENABLE)
    fun load() {
        datas.clear()
        data.getKeys(false).forEach { name ->
            datas.add(
                json.decodeFromString(ShopData.serializer(), data.getString(name)!!)
            )
        }
    }

    @Awake(LifeCycle.DISABLE)
    fun save() {
        data.clear()
        datas.forEach { value ->
            data[value.name] = json.encodeToString(ShopData.serializer(), value)
        }
    }
}