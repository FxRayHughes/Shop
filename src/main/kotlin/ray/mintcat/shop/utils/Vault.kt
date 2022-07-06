package ray.mintcat.shop.utils

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object Vault {

    private val economy = Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider

    fun takeMoney(player: Player, amount: Double): Boolean {
        val eco = economy ?: return false
        return if (getMoney(player) < amount) {
            false
        } else {
            eco.withdrawPlayer(player, amount)
            true
        }
    }

    fun addMoney(player: Player, amount: Double) {
        val eco = economy ?: return
        eco.depositPlayer(player, amount)
    }

    fun getMoney(player: Player): Double {
        val eco = economy ?: return 0.0
        return eco.getBalance(player)
    }
}