package ray.mintcat.shop.utils

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submit
import taboolib.common5.Coerce
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
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
import taboolib.platform.util.inventoryCenterSlots
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

val tpMap = HashMap<UUID, Location>()

//延迟传送 单位s
fun Player.tpDelay(mint: Int, locationTo: Location) {
    tpMap[this.uniqueId] = this.location
    this.info("${mint}s 后开始传送 请勿移动!")
    submit(delay = mint.toLong() * 20) {
        val a = this@tpDelay.location
        val b = tpMap[this@tpDelay.uniqueId] ?: return@submit
        if (a.x != b.x || a.y != b.y || a.z != b.z) {
            this@tpDelay.error("由于您的移动已取消传送!")
            tpMap.remove(this@tpDelay.uniqueId)
            return@submit
        }
        this@tpDelay.teleport(locationTo)
        tpMap.remove(this@tpDelay.uniqueId)
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
                it.isCancelled = true
            }
            onClick(lock = false)
            onClose {
                itemStack = it.inventory.getItem(4)
            }
        }
    }
    return itemStack
}

fun <T> Linked<T>.inits() {
    this.rows(6)
    this.slots(inventoryCenterSlots)
    this.setNextPage(51) { page, hasNextPage ->
        if (hasNextPage) {
            buildItem(XMaterial.SPECTRAL_ARROW) {
                name = "§f下一页"
            }
        } else {
            buildItem(XMaterial.ARROW) {
                name = "§7下一页"
            }
        }
    }
    this.setPreviousPage(47) { page, hasPreviousPage ->
        if (hasPreviousPage) {
            buildItem(XMaterial.SPECTRAL_ARROW) {
                name = "§f上一页"
            }
        } else {
            buildItem(XMaterial.ARROW) {
                name = "§7上一页"
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