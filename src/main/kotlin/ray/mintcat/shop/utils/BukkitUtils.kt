package ray.mintcat.shop.utils

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import ray.mintcat.shop.UIReader
import ray.mintcat.shop.data.ShopData
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submit
import taboolib.common5.Coerce
import taboolib.library.xseries.XMaterial
import taboolib.library.xseries.getItemStack
import taboolib.module.chat.uncolored
import taboolib.module.configuration.Configuration
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.nms.getI18nName
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.buildItem
import java.util.*
import java.util.concurrent.CompletableFuture


fun String.uncolor(): String {
    return this.uncolored().replace("#([a-zA-Z]+([0-9]+[a-zA-Z]+)+)".toRegex(), "")
}

/**
 * BukkitUtils
 * @author Ray_Hughes
 * @Time 2022/1/26
 * @since 1.0
 */

fun Entity.getNameTrue(): String {
    if (this is Player) {
        return this.name
    }
    if (this.customName != null) {
        return this.customName
    }
    if (this.name != null) {
        return this.name
    }
    return this.getI18nName()
}

/**
 * 判断坐标是否再两个点形成的矩形内
 *
 * @receiver 应判断的坐标
 * @param posA 向量点A
 * @param posB 向量点B
 * @return 在范围内为true 反之为false
 * @since 1.0
 */
fun Location.isInAABB(posA: Location, posB: Location): Boolean {
    val pA = Vector(posA.x.coerceAtLeast(posB.x), posA.y.coerceAtLeast(posB.y), posA.z.coerceAtLeast(posB.z))
    val pB = Vector(posA.x.coerceAtMost(posB.x), posA.y.coerceAtMost(posB.y), posA.z.coerceAtMost(posB.z))
    return this.toVector().isInAABB(pB, pA)
}

fun fromLocation(location: Location): String {
    return "${location.world?.name},${location.x},${location.y},${location.z}".replace(".", "__")
}

fun toLocation(source: String): Location {
    return source.replace("__", ".").split(",").run {
        Location(
            Bukkit.getWorld(getOrElse(0) { "world" }),
            getOrElse(1) { "0" }.asDouble(),
            getOrElse(2) { "0" }.asDouble(),
            getOrElse(3) { "0" }.asDouble()
        )
    }
}

fun String.asPapi(player: Player): String {
    return this.replacePlaceholder(player)
}

infix fun String.papi(player: Player): String {
    return this.replacePlaceholder(player)
}

fun Collection<String>.asPapi(player: Player): List<String> {
    return this.map { it.asPapi(player) }
}

private fun getRandom(location: Location): Location {
    var locations = getRandomLocation(location)
    while (locations.block.type != Material.AIR) {
        locations = getRandomLocation(location)
    }
    return locations
}

private fun getRandomLocation(location: Location): Location {
    val radius = 1.0
    val radians = Math.toRadians((0..360).random().toDouble())
    val x = kotlin.math.cos(radians) * radius
    val z = kotlin.math.sin(radians) * radius
    return location.add(x, 1.0, z)
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun Configuration.clear() {
    this.getKeys(false).forEach {
        this[it] = null
    }
}

fun Player.inputItem(): ItemStack {
    var itemStack = ItemStack(Material.STONE)
    submit(delay = 1) {
        this@inputItem.openMenu<Basic>("请放入物品") {
            map("####@####")
            handLocked(false)
            set('#', buildItem(XMaterial.BLACK_STAINED_GLASS_PANE) {
                name = " "
                colored()
            }) {
                isCancelled = true
            }
            onClick(lock = false)
            onClose {
                itemStack = it.inventory.getItem(4)
            }
        }
    }
    return itemStack
}

fun <T> Linked<T>.inits(data: ShopData, player: Player) {
    val config = UIReader.getUIConfig(data)
    map(*config.getStringList("Layout").toTypedArray())
    config.getString("Commodity")?.asChar()?.let { slotsBy(it) } ?: slotsBy('@')
    val nextChar = config.getString("NextItem.slot")?.asChar() ?: 'B'
    this.setNextPage(getFirstSlot(nextChar)) { page, hasNextPage ->
        if (hasNextPage) {
            config.getItemStack("NextItem.has").papi(player) ?: buildItem(XMaterial.SPECTRAL_ARROW) {
                name = "§f下一页"
            }
        } else {
            config.getItemStack("NextItem.normal").papi(player) ?: buildItem(XMaterial.ARROW) {
                name = "§7下一页"
            }
        }
    }
    val previoustChar = config.getString("PreviousItem.slot")?.asChar() ?: 'C'
    this.setPreviousPage(getFirstSlot(previoustChar)) { page, hasPreviousPage ->
        if (hasPreviousPage) {
            config.getItemStack("PreviousItem.has").papi(player) ?: buildItem(XMaterial.SPECTRAL_ARROW) {
                name = "§f上一页"
            }
        } else {
            config.getItemStack("PreviousItem.normal").papi(player) ?: buildItem(XMaterial.ARROW) {
                name = "§7上一页"
            }
        }
    }

    config.getConfigurationSection("OtherItem")?.getKeys(false)?.forEach { key ->
        config.getItemStack("OtherItem.${key}.item")?.let {
            set(key.asChar(), it) {
                isCancelled = true
                if (clickEvent().isLeftClick) {
                    if (clickEvent().isShiftClick) {
                        config.getStringList("OtherItem.${key}.action.left_shift").eval(player)
                        return@set
                    }
                    config.getStringList("OtherItem.${key}.action.left").eval(player)
                    return@set
                }
                if (clickEvent().isRightClick) {
                    if (clickEvent().isShiftClick) {
                        config.getStringList("OtherItem.${key}.action.right_shift").eval(player)
                        return@set
                    }
                    config.getStringList("OtherItem.${key}.action.right").eval(player)
                    return@set
                }
            }
        }
    }

}

fun List<String>.eval(player: Player) {
    try {
        KetherShell.eval(this, sender = adaptPlayer(player))
    } catch (e: Throwable) {
        e.printKetherErrorMessage()
    }
}

fun List<String>.check(player: Player): CompletableFuture<Boolean> {
    return if (this.isEmpty()) {
        CompletableFuture.completedFuture(true)
    } else {
        try {
            KetherShell.eval(this, sender = adaptPlayer(player)).thenApply {
                Coerce.toBoolean(it)
            }
        } catch (e: Throwable) {
            e.printKetherErrorMessage()
            CompletableFuture.completedFuture(false)
        }
    }
}