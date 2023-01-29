package ray.mintcat.shop.utils

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common5.Baffle
import taboolib.expansion.sendMessageAsLang
import taboolib.module.chat.colored
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.type.Basic
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.asLangText
import taboolib.platform.util.buildItem

/**
 * LoggerUtils
 * @author Ray_Hughes
 * @Time 2022/1/26
 * @since 1.0
 */

fun Player.sendMessageAsLang(node: String) {
    this.sendMessage(this.asLangText(node).replacePlaceholder(player).colored())
}

fun Player.sendMessageAsLang(node: String, vararg args: Any) {
    this.sendMessage(this.asLangText(node, *args).replacePlaceholder(player).colored())
}

fun String.color(): String {
    return this.colored()
}

/**
 * 发送中中央屏幕提示
 */
fun Player.infoTitle(info: String, sub: String) {
    this.sendTitle(info.replace("&", "§"), sub.replace("&", "§"), 10, 25, 10)
}
