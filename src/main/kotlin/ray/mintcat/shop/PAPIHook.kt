package ray.mintcat.shop

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import ray.mintcat.shop.data.ShopCommodityData
import ray.mintcat.shop.data.ShopData
import ray.mintcat.shop.utils.Vault
import taboolib.platform.compat.PlaceholderExpansion
import java.util.UUID

object PAPIHook : PlaceholderExpansion {
    override val identifier: String
        get() = "rayshop"

    val map = HashMap<UUID, Pair<ShopData, ShopCommodityData>>()

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        val maped = map[player?.uniqueId ?: return "noplayer"] ?: return "nodata"
        val shop = maped.first
        val data = maped.second
        return when (args) {
            "now_buy" -> {
                data.price.toString()
            }

            "now_buy_discount" -> {
                data.getPriceNew(player, shop).toString()
            }

            "now_sell" -> {
                data.buy.toString()
            }

            "money_type" -> {
                data.moneyType
            }

            "money_type_show" -> {
                Vault.getName(data.moneyType)
            }

            else -> {
                "null"
            }
        }
    }

    override fun onPlaceholderRequest(player: OfflinePlayer?, args: String): String {
        return onPlaceholderRequest(player as? Player, args)
    }
}