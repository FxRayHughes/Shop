package ray.mintcat.shop.data.materials

import dev.lone.itemsadder.api.CustomStack
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.platform.util.countItem

object MaterialItemsAdder : Material {

    override val from: String
        get() = "ItemsAdder"

    @Awake(LifeCycle.ACTIVE)
    fun onload() {
        if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
            register()
        }
    }

    override fun getId(itemStack: ItemStack): String? {
        return CustomStack.byItemStack(itemStack)?.namespacedID
    }

    val nameList = CustomStack.getNamespacedIdsInRegistry()

    override val itemList: List<ItemStack>
        get() = nameList.mapNotNull { CustomStack.getInstance(it)?.itemStack }

    override fun isItem(itemStack: ItemStack, id: String): Boolean {
        return (getId(itemStack) ?: return false) == id
    }

    override fun amount(inventory: Inventory, id: String): Int {
        return inventory.countItem {
            isItem(it, id)
        }
    }

    override fun getItem(id: String, amount: Int, user: Player?): ItemStack? {
        return CustomStack.getInstance(id)?.itemStack?.apply {
            this.amount = amount
        }
    }

    override fun hasItem(inventory: Inventory, id: String, amount: Int): Boolean {
        return amount(inventory, id) >= amount
    }
}