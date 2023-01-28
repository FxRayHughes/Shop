package ray.mintcat.shop.data.materials

import dev.lone.itemsadder.api.CustomStack
import github.saukiya.sxitem.SXItem
import github.saukiya.sxitem.data.item.ItemManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.pluginId
import taboolib.module.nms.getName
import taboolib.platform.util.countItem
import taboolib.platform.util.takeItem

object MaterialSXItem : Material {

    override val from: String
        get() = "SXItem"


    @Awake(LifeCycle.ACTIVE)
    fun onload() {
        if (Bukkit.getPluginManager().getPlugin("SX-Item") != null) {
            try {
                register()
            } catch (_: Exception) {
            }
        }
    }

    override fun getId(itemStack: ItemStack): String? {
        return SXItem.getItemManager().getGenerator(itemStack)?.name
    }

    override val itemList: List<ItemStack>
        get() = listOf()

    override fun isItem(itemStack: ItemStack, id: String): Boolean {
        return (getId(itemStack) ?: return false) == id
    }

    override fun amount(inventory: Inventory, id: String): Int {
        return inventory.countItem {
            isItem(it, id)
        }
    }

    override fun getItem(id: String, amount: Int, user: Player?): ItemStack? {
        return SXItem.getItemManager().getItem(id, user!!)?.apply {
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
            isItem(it, id)
        }
    }
}