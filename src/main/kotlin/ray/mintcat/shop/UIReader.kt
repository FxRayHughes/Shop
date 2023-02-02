package ray.mintcat.shop

import ray.mintcat.shop.data.ShopData
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object UIReader {

    val uiConfig = ConcurrentHashMap<String, Configuration>()

    val scriptConfig = ConcurrentHashMap<String, List<String>>()

    val files = ArrayList<File>()

    @Config("ui/default.yml")
    lateinit var default: ConfigFile

    @Config("script/default.yml")
    lateinit var defaultscript: ConfigFile

    fun getUIConfig(data: ShopData): Configuration {
        return uiConfig.getOrDefault(data.name, default)
    }

    @Awake(LifeCycle.ACTIVE)
    fun load() {
        default.reload()
        defaultscript.reload()
        files.clear()
        scriptConfig.clear()
        uiConfig.clear()
        loadFile(File(getDataFolder(), "ui/"))
        loadConfig()
        loadFile(File(getDataFolder(), "script/"))
        loadScript()
    }

    fun loadConfig() {
        files.forEach {
            Configuration.loadFromFile(it, type = Type.YAML).let { cf ->
                cf.getString("Shop")?.let { name ->
                    uiConfig[name] = cf
                }
            }
        }
    }

    fun loadScript() {
        files.forEach {
            Configuration.loadFromFile(it, type = Type.YAML).let { cf ->
                cf.getKeys(false).forEach z@{ key ->
                    scriptConfig[key] = cf.getStringList(key)
                }
            }
        }
    }

    fun loadFile(file: File) {
        if (file.isFile) {
            files.add(file)
        } else {
            file.listFiles()?.forEach {
                loadFile(it)
            }
        }
    }


}