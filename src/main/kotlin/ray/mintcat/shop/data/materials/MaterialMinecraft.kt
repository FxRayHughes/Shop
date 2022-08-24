package ray.mintcat.shop.data.materials

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import ray.mintcat.shop.serializable.GsonUtils
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.xseries.parseToMaterial
import taboolib.platform.util.buildItem
import taboolib.platform.util.countItem
import taboolib.platform.util.isAir

object MaterialMinecraft : Material {

    override val from: String
        get() = "Minecraft"

    @Awake(LifeCycle.ACTIVE)
    fun onload() {
        register()
    }

    override fun getId(itemStack: ItemStack): String {
        return itemStack.toStringSave()
    }

    override val itemList: List<ItemStack>
        get() = org.bukkit.Material.values().asSequence().mapNotNull {
            if (it.isAir()) {
                null
            } else {
                try {
                    buildItem(it)
                } catch (_: Exception) {
                    null
                }
            }
        }.toList()

    override fun isItem(itemStack: ItemStack, id: String): Boolean {
        return itemStack.isSimilar(id.toItemStack())
    }

    override fun amount(inventory: Inventory, id: String): Int {
        return inventory.countItem {
            isItem(it, id)
        }
    }

    override fun getItem(id: String, amount: Int, user: Player?): ItemStack {
        return id.toItemStack().apply {
            this.amount = amount
        }
    }

    override fun hasItem(inventory: Inventory, id: String, amount: Int): Boolean {
        return amount(inventory, id) >= amount
    }

    fun ItemStack.toStringSave(): String {
        return GsonUtils.gson.toJson(this, ItemStack::class.javaObjectType)
    }

    fun String.toItemStack(): ItemStack {
        return GsonUtils.gson.fromJson(this, ItemStack::class.javaObjectType)
    }

}