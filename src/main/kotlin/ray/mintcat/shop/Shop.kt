package ray.mintcat.shop

import ray.mintcat.shop.data.ShopCommodityData
import ray.mintcat.shop.data.ShopData
import ray.mintcat.shop.serializable.GsonUtils
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.module.configuration.createLocal
import java.util.*

@RuntimeDependencies(
    RuntimeDependency(
        value = "com.google.code.gson:gson:2.9.0",
    )
)
object Shop : Plugin() {

    val copy = HashMap<UUID, ShopCommodityData>()

    val data by lazy {
        createLocal("ShopData.yml")
    }

    val datas = ArrayList<ShopData>()

    @Awake(LifeCycle.ENABLE)
    fun load() {
        datas.clear()
        data.getKeys(false).forEach { name ->
            datas.add(
                GsonUtils.gson.fromJson(data.getString(name) ?: return@forEach, ShopData::class.javaObjectType)
            )
        }
    }

    @Awake(LifeCycle.DISABLE)
    fun save() {
        data.clear()
        datas.forEach { value ->
            data[value.name] = GsonUtils.gson.toJson(value, ShopData::class.javaObjectType)
        }
    }
}