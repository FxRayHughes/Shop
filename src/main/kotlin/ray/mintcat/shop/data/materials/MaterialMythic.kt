package ray.mintcat.shop.data.materials

import ink.ptms.um.Mythic
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.platform.util.countItem

object MaterialMythic : Material {

    override val from: String
        get() = "Mythic"

    @Awake(LifeCycle.ACTIVE)
    fun onload() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            register()
        }
    }

    override fun getId(itemStack: ItemStack): String? {
        return Mythic.API.getItemId(itemStack)
    }

    override val itemList: List<ItemStack>
        get() = Mythic.API.getItemList().map { it.generateItemStack(1) }

    override fun isItem(itemStack: ItemStack, id: String): Boolean {
        return (Mythic.API.getItemId(itemStack) ?: return false) == id
    }

    override fun amount(inventory: Inventory, id: String): Int {
        return inventory.countItem {
            isItem(it, id)
        }
    }

    override fun getItem(id: String, amount: Int, user: Player?): ItemStack? {
        return Mythic.API.getItemStack(id)?.apply {
            this.amount = amount
        }
    }

    override fun hasItem(inventory: Inventory, id: String, amount: Int): Boolean {
        return amount(inventory, id) >= amount
    }

}