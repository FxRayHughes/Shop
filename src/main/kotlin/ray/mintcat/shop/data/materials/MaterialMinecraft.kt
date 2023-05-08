package ray.mintcat.shop.data.materials

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.nms.getName
import taboolib.platform.util.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

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
            isItem(it, id) && MaterialFeed.canUse(it)
        }
    }

    override fun getItem(id: String, amount: Int, user: Player?): ItemStack {
        return id.toItemStack().apply {
            this.amount = amount
        }
    }

    override fun getNameId(id: String, user: Player?): String {
        return getItem(id, 1, user).getName()
    }

    override fun getShowName(id: String, user: Player?): String {
        return getItem(id, 1, user).getName()
    }

    override fun hasItem(inventory: Inventory, id: String, amount: Int): Boolean {
        return amount(inventory, id) >= amount
    }

    fun ItemStack.toStringSave(): String {
        return this.serializeToString()
    }

    fun String.toItemStack(): ItemStack {
        return this.deserializeToItemStack()
    }

    fun ItemStack.serializeToString(): String {
        val outputStream = ByteArrayOutputStream()
        val bukkitOutputStream = BukkitObjectOutputStream(outputStream)

        bukkitOutputStream.writeObject(this)
        bukkitOutputStream.flush()

        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }

    fun String.deserializeToItemStack(): ItemStack {
        val inputStream = ByteArrayInputStream(Base64.getDecoder().decode(this))
        val bukkitInputStream = BukkitObjectInputStream(inputStream)

        return bukkitInputStream.readObject() as ItemStack
    }

    override fun takeItem(inventory: Inventory, id: String, amount: Int): Boolean {
        return inventory.takeItem(amount) {
            isItem(it, id) && MaterialFeed.canUse(it)
        }
    }


}