package ray.mintcat.shop

import org.bukkit.Location
import org.bukkit.block.CreatureSpawner
import org.bukkit.entity.EntityType
import ray.mintcat.shop.data.ShopCommodityData
import ray.mintcat.shop.data.ShopData
import taboolib.common.LifeCycle
import taboolib.common.io.runningClasses
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.common.platform.event.SubscribeEvent
import taboolib.expansion.ioc.IOCReader
import taboolib.expansion.ioc.linker.linkedIOCMap
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.lang.event.PlayerSelectLocaleEvent
import taboolib.module.lang.event.SystemSelectLocaleEvent
import java.util.*
import kotlin.collections.HashMap

object Shop : Plugin() {

    val copy = HashMap<UUID, ShopCommodityData>()

    val amount = HashMap<UUID, Int>()

    val datas = linkedIOCMap<ShopData>()

    @Awake(LifeCycle.INIT)
    fun init() {
        IOCReader.readRegister(runningClasses)
    }

    @Config
    lateinit var config: ConfigFile
        private set

    @SubscribeEvent
    fun lang(event: PlayerSelectLocaleEvent) {
        event.locale = config.getString("Lang", "zh_CN")!!
    }

    @SubscribeEvent
    fun lang(event: SystemSelectLocaleEvent) {
        event.locale = config.getString("Lang", "zh_CN")!!
    }
}