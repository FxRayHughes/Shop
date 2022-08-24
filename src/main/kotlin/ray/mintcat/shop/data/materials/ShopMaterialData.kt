package ray.mintcat.shop.data.materials

import com.google.gson.annotations.Expose
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
        return lib.getItem(id, amount, player)
    }

    fun lib(): Material {
        return MaterialFeed.materials[form] ?: MaterialMinecraft
    }

    fun amount(player: Player): Int {
        return lib().amount(player.inventory, id)
    }
}