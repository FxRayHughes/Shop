package ray.mintcat.shop.data.materials

import org.bukkit.inventory.ItemStack
import ray.mintcat.shop.Shop
import taboolib.platform.util.hasLore
import java.util.concurrent.ConcurrentHashMap

object MaterialFeed {

    val materials = ConcurrentHashMap<String, Material>()

    fun toMaterial(itemStack: ItemStack): ShopMaterialData? {
        val data = materials.filter { it.key != "Minecraft" }.values.firstOrNull { it.getId(itemStack) != null }
            ?: materials["Minecraft"]!!
        val id = data.getId(itemStack) ?: return null
        return ShopMaterialData(data.from, id, itemStack.amount)
    }

    fun canUse(itemStack: ItemStack): Boolean {
        if (!itemStack.hasLore()) {
            return true
        }
        Shop.config.getStringList("BlackLore").forEach {
            if (itemStack.hasLore(it)) {
                return false
            }
        }
        return true
    }

}