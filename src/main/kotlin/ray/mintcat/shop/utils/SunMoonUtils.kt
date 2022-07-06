package ray.mintcat.shop.utils

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.takeItem


val Boolean.display: String
    get() = if (this) "§a允许" else "§c阻止"

fun getAmount(player: Player, item: ItemStack): Int {
    var i = 0
    player.inventory.takeItem(999) {
        if (it.isSimilar(item)) {
            i += it.amount
        }
        false
    }
    return i
}