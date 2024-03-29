package ray.mintcat.shop.data.materials

import dev.lone.itemsadder.api.CustomStack
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.nms.getName
import taboolib.platform.util.countItem
import taboolib.platform.util.hasLore
import taboolib.platform.util.takeItem

object MaterialItemsAdder : Material {

    override val from: String
        get() = "ItemsAdder"


    @Awake(LifeCycle.ACTIVE)
    fun onload() {
        if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
            try {
                register()
            } catch (_: Exception) {
            }
        }
    }

    override fun getId(itemStack: ItemStack): String? {
        return CustomStack.byItemStack(itemStack)?.namespacedID
    }

    val nameList by lazy {
        CustomStack.getNamespacedIdsInRegistry()
    }

    override val itemList: List<ItemStack>
        get() = nameList.mapNotNull { CustomStack.getInstance(it)?.itemStack }

    override fun isItem(itemStack: ItemStack, id: String): Boolean {
        return (getId(itemStack) ?: return false) == id
    }

    override fun amount(inventory: Inventory, id: String): Int {
        return inventory.countItem {
            isItem(it, id) && MaterialFeed.canUse(it)
        }
    }

    override fun getItem(id: String, amount: Int, user: Player?): ItemStack? {
        return CustomStack.getInstance(id)?.itemStack?.apply {
            this.amount = amount
        }
    }

    override fun getNameId(id: String, user: Player?): String {
        return id
    }

    override fun getShowName(id: String, user: Player?): String {
        return getItem(id, 1, user)?.getName() ?: id
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