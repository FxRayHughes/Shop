package ray.mintcat.shop

import ray.mintcat.shop.data.DiscountData
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.util.getMap

object Discount {

    val datas = ArrayList<DiscountData>()

    @Config(value = "discount.yml")
    lateinit var config: ConfigFile
        private set

    @Awake(LifeCycle.ENABLE)
    fun load() {
        datas.clear()
        config.getKeys(false).forEach {
            datas.add(
                DiscountData(
                    it,
                    config.getString("${it}.permissions") ?: return@forEach,
                    config.getStringList("${it}.shop"),
                    config.getMap("${it}.data")
                )
            )
        }
    }

}