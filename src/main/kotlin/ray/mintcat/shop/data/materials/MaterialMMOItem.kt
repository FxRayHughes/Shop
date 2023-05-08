package ray.mintcat.shop.data.materials

import ink.ptms.um.Mythic
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.api.MMOItemsAPI
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


object MaterialMMOItem : Material {

    override val from: String
        get() = "MMOItems"

    @Awake(LifeCycle.ACTIVE)
    fun onload() {
        if (Bukkit.getPluginManager().getPlugin("MMOItems") != null) {
            register()
        }
    }

    //
    override fun getId(itemStack: ItemStack): String? {
        return if (itemStack.getString("MMOITEMS_ITEM_ID") == "null") {
            null
        } else {
            "${itemStack.getString("MMOITEMS_ITEM_TYPE")}::${itemStack.getString("MMOITEMS_ITEM_ID")}"
        }
    }

    override val itemList: List<ItemStack>
        get() = listOf()

    override fun isItem(itemStack: ItemStack, id: String): Boolean {
        return getId(itemStack)?.let {
            it.split("::")[1] == id
        } ?: false
    }

    override fun amount(inventory: Inventory, id: String): Int {
        return inventory.countItem {
            isItem(it, id) && MaterialFeed.canUse(it)
        }
    }

    override fun getItem(id: String, amount: Int, user: Player?): ItemStack? {
        val sub = id.split("::")
        val type = sub.getOrNull(0) ?: return null
        val item = sub.getOrNull(1) ?: return null
        val mmiType = MMOItems.plugin.types.get(type) ?: return null
        val mmoitem = MMOItems.plugin.getItem(mmiType,item)
        return mmoitem?.apply {
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