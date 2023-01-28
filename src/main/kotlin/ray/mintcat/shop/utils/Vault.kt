package ray.mintcat.shop.utils

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ray.mintcat.shop.Shop
import taboolib.module.configuration.util.getStringColored
import taboolib.platform.compat.replacePlaceholder

object Vault {

    private val economy = Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider

    fun takeMoney(player: Player, amount: Double, type: String): Boolean {
        return if (getMoney(player, type) < amount) {
            false
        } else {
            if (type == "Vault") {
                val eco = economy ?: return false
                eco.withdrawPlayer(player, amount)
            } else {
                Shop.config.getStringList("Type.${type}.take")
                    .replace("<value>", amount.toString())
                    .replace("<player>", player.name).forEach {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it)
                    }
            }
            true
        }
    }

    fun addMoney(player: Player, amount: Double, type: String) {
        if (type == "Vault") {
            val eco = economy ?: return
            eco.depositPlayer(player, amount)
        } else {
            Shop.config.getStringList("Type.${type}.add")
                .replace("<value>", amount.toString())
                .replace("<player>", player.name).forEach {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it)
                }
        }
    }

    fun getMoney(player: Player, type: String): Double {
        return if (type == "Vault") {
            val eco = economy ?: return 0.0
            eco.getBalance(player)
        } else {
            Shop.config.getString("Type.${type}.get")?.replace("<player>", player.name)
                ?.replacePlaceholder(player)
                ?.toDoubleOrNull() ?: 0.0
        }
    }

    fun getName(type: String): String {
        return Shop.config.getStringColored("Type.${type}.name")?.let {
            "${it}&f".color()
        } ?: type
    }
}