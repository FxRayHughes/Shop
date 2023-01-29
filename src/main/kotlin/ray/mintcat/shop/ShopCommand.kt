package ray.mintcat.shop

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ray.mintcat.shop.data.ShopData
import ray.mintcat.shop.utils.color
import ray.mintcat.shop.utils.error
import ray.mintcat.shop.utils.info
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.platform.BukkitAdapter

@CommandHeader(name = "rshop", aliases = ["sp", "rayshop", "shop"], permission = "shop.use")
object ShopCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val open = subCommand {
        dynamic("shop") {
            suggestion<CommandSender>(uncheck = true) { sender, context ->
                Shop.datas.keys().toList()
            }
            player("player") {
                suggestion<CommandSender> { sender, context ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                execute<CommandSender> { sender, context, argument ->
                    val target = Bukkit.getPlayer(context.player("player").uniqueId)
                    val data = Shop.datas.getOrPut(context["shop"]) {
                        ShopData(context["shop"])
                    }
                    (data as ShopData).openShop(target)
                }
            }
            execute<Player> { sender, context, argument ->
                val data = Shop.datas.getOrPut(context["shop"]) {
                    ShopData(context["shop"])
                }
                (data as ShopData).openShop(sender)
            }
        }
    }

    @CommandBody
    val list = subCommand {
        dynamic("shop") {
            suggestion<CommandSender>(uncheck = true) { sender, context ->
                Shop.datas.keys().toList()
            }
            execute<CommandSender> { sender, context, argument ->
                val data = Shop.datas.getOrPut(context["shop"]) {
                    ShopData(context["shop"])
                } as ShopData
                sender.info("${context["shop"]}商店包含的商品:")
                data.commodity.forEach {
                    sender.info("&f- ${it.id} &7(${it.showName})".color())
                }
            }
        }
    }

    @CommandBody
    val listShop = subCommand {
        execute<CommandSender> { sender, context, argument ->
            sender.info("商店列表:")
            Shop.datas.keys().toList().forEach {
                sender.info("&f- ${it}".color())
            }
        }
    }

    @CommandBody
    val buy = subCommand {
        dynamic("shop") {
            suggestion<CommandSender>(uncheck = true) { sender, context ->
                Shop.datas.keys().toList()
            }
            dynamic("commodity") {
                suggestion<CommandSender> { sender, context ->
                    val data = Shop.datas.getOrPut(context["shop"]) {
                        ShopData(context["shop"])
                    } as ShopData
                    data.commodity.mapNotNull { it.id }
                }
                int("amount") {
                    player("player") {
                        suggestion<CommandSender> { sender, context ->
                            Bukkit.getOnlinePlayers().map { it.name }
                        }
                        execute<CommandSender> { sender, context, argument ->
                            val target = Bukkit.getPlayer(context.player("player").uniqueId)
                            val data = Shop.datas.getOrPut(context["shop"]) {
                                ShopData(context["shop"])
                            } as ShopData
                            val element = data.commodity.firstOrNull { it.id == context["commodity"] }!!
                            data.buy(target, context.int("amount"), element)
                        }
                    }
                    execute<Player> { sender, context, argument ->
                        val data = Shop.datas.getOrPut(context["shop"]) {
                            ShopData(context["shop"])
                        } as ShopData
                        val element = data.commodity.firstOrNull { it.id == context["commodity"] }!!
                        data.buy(sender, context.int("amount"), element)
                    }
                }
            }
        }
    }

    @CommandBody
    val sell = subCommand {
        dynamic("shop") {
            suggestion<CommandSender>(uncheck = true) { sender, context ->
                Shop.datas.keys().toList()
            }
            dynamic("commodity") {
                suggestion<CommandSender> { sender, context ->
                    val data = Shop.datas.getOrPut(context["shop"]) {
                        ShopData(context["shop"])
                    } as ShopData
                    data.commodity.mapNotNull { it.id }
                }
                int("amount") {
                    player("player") {
                        suggestion<CommandSender> { sender, context ->
                            Bukkit.getOnlinePlayers().map { it.name }
                        }
                        execute<CommandSender> { sender, context, argument ->
                            val target = Bukkit.getPlayer(context.player("player").uniqueId)
                            val data = Shop.datas.getOrPut(context["shop"]) {
                                ShopData(context["shop"])
                            } as ShopData
                            val element = data.commodity.firstOrNull { it.id == context["commodity"] }!!
                            data.sell(target, context.int("amount"), element)
                        }
                    }
                    execute<Player> { sender, context, argument ->
                        val data = Shop.datas.getOrPut(context["shop"]) {
                            ShopData(context["shop"])
                        } as ShopData
                        val element = data.commodity.firstOrNull { it.id == context["commodity"] }!!
                        data.sell(sender, context.int("amount"), element)
                    }
                }
            }
        }
    }

    @CommandBody
    val remove = subCommand {
        dynamic(optional = false, comment = "ShopName") {
            suggestion<CommandSender> { sender, context ->
                Shop.datas.keys().toList()
            }
            execute<CommandSender> { sender, context, argument ->
                Shop.datas[context.argument(0)] ?: return@execute kotlin.run {
                    sender.error("商店不存在!")
                }
                Shop.datas.remove(context.argument(0))
                sender.info("商店以删除!")
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, context, argument ->
            Shop.config.reload()
            sender.info("配置文件重载完成")
        }
    }

}