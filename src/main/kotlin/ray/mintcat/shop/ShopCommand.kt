package ray.mintcat.shop

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ray.mintcat.shop.data.ShopData
import ray.mintcat.shop.utils.error
import ray.mintcat.shop.utils.info
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper

@CommandHeader(name = "shop", aliases = ["sp"], permission = "shop.use")
object ShopCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val open = subCommand {
        dynamic(optional = false, commit = "ShopName") {
            suggestion<CommandSender>(uncheck = true) { sender, context ->
                Shop.datas.map { it.name }
            }
            dynamic {
                suggestion<CommandSender> { sender, context ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                execute<CommandSender> { sender, context, argument ->
                    val target = Bukkit.getPlayerExact(context.argument(0)) ?: return@execute kotlin.run {
                        sender.error("玩家 &f${context.argument(0)}&7 不存在!")
                    }
                    Shop.datas.firstOrNull { it.name == context.argument(-1) } ?: kotlin.run {
                        Shop.datas.add(ShopData(context.argument(-1)))
                    }
                    val data = Shop.datas.first { it.name == context.argument(-1) }
                    data.openShop(target)
                }
            }
            execute<Player> { sender, context, argument ->
                Shop.datas.firstOrNull { it.name == context.argument(0) } ?: kotlin.run {
                    Shop.datas.add(ShopData(context.argument(0)))
                }
                val data = Shop.datas.first { it.name == context.argument(0) }
                data.openShop(sender)
            }
        }
    }

    @CommandBody
    val remove = subCommand {
        dynamic(optional = false, commit = "ShopName") {
            suggestion<CommandSender> { sender, context ->
                Shop.datas.map { it.name }
            }
            execute<CommandSender> { sender, context, argument ->
                val data = Shop.datas.firstOrNull { it.name == context.argument(0) } ?: return@execute kotlin.run {
                    sender.error("商店不存在!")
                }
                Shop.datas.remove(data)
                Shop.save()
                sender.info("商店以删除!")
            }
        }
    }

}