package ray.mintcat.shop.data.materials

import com.google.gson.annotations.Expose
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ShopMaterialData(
    @Expose
    val form: String,
    @Expose
    val id: String,
    @Expose
    val amount: Int,
) {
    fun create(player: Player?): ItemStack? {
        val lib = MaterialFeed.materials[form] ?: return null
        player?.let {
            return lib.getItem(id, amount, it)
        }
        val rand = Bukkit.getOnlinePlayers().random()
        return lib.getItem(id, amount, rand)
    }

    fun lib(): Material {
        return MaterialFeed.materials[form] ?: MaterialMinecraft
    }

    fun amount(player: Player): Int {
        return lib().amount(player.inventory, id)
    }

    fun getNameId(player: Player): String {
        return lib().getNameId(id, player)
    }

    fun getNameShow(player: Player): String {
        return lib().getShowName(id, player)
    }
}