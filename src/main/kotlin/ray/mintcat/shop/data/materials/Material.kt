package ray.mintcat.shop.data.materials

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.info

interface Material {

    val from: String

    val itemList: List<ItemStack>

    fun isItem(itemStack: ItemStack, id: String): Boolean

    fun getId(itemStack: ItemStack): String?

    fun amount(inventory: Inventory, id: String): Int

    fun hasItem(inventory: Inventory, id: String, amount: Int): Boolean

    fun takeItem(inventory: Inventory, id: String, amount: Int): Boolean

    fun getItem(id: String, amount: Int, user: Player? = null): ItemStack?

    fun getNameId(id: String, user: Player? = null): String

    fun getShowName(id: String, user: Player? = null): String

    fun register() {
        info("成功注册 ${from}")
        MaterialFeed.materials[from] = this
    }
}