package ray.mintcat.shop.data.materials

import com.skillw.itemsystem.api.ItemAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import ray.mintcat.shop.utils.getString
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.nms.getName
import taboolib.platform.util.countItem
import taboolib.platform.util.takeItem

object MaterialItemSystem : Material {

    override val from: String
        get() = "ItemSystem"

    @Awake(LifeCycle.ACTIVE)
    fun onload() {
        if (Bukkit.getPluginManager().getPlugin("ItemSystem") != null) {
            register()
        }
    }

    override fun getId(itemStack: ItemStack): String? {
        return if (itemStack.getString("ITEM_SYSTEM.key") == "null") {
            null
        } else {
            itemStack.getString("ITEM_SYSTEM.key")
        }
    }

    override val itemList: List<ItemStack>
        get() = listOf()

    override fun isItem(itemStack: ItemStack, id: String): Boolean {
        return getId(itemStack)?.let {
            it == id
        } ?: false
    }

    override fun amount(inventory: Inventory, id: String): Int {
        return inventory.countItem {
            isItem(it, id) && MaterialFeed.canUse(it)
        }
    }

    override fun getItem(id: String, amount: Int, user: Player?): ItemStack? {
        return ItemAPI.productItem(id, user)?.apply {
            this.amount = amount
        }
    }

    override fun getShowName(id: String, user: Player?): String {
        return getItem(id, 1, user)?.getName() ?: id
    }

    override fun getNameId(id: String, user: Player?): String {
        return id
    }

    override fun hasItem(inventory: Inventory, id: String, amount: Int): Boolean {
        return amount(inventory, id) >= amount
    }

    override fun takeItem(inventory: Inventory, id: String, amount: Int): Boolean {
        return inventory.takeItem(amount) {
            isItem(it, id) && MaterialFeed.canUse(it)
        }
    }

}