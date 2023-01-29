package ray.mintcat.shop

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ray.mintcat.shop.data.ShopData
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.expansion.sendMessageAsLang
import taboolib.module.lang.Language

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
                    (data as ShopData).openShop(target,false)
                }
            }
            execute<Player> { sender, context, argument ->
                val data = Shop.datas.getOrPut(context["shop"]) {
                    ShopData(context["shop"])
                }
                (data as ShopData).openShop(sender,false)
            }
        }
    }

    @CommandBody
    val edit = subCommand {
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
                    (data as ShopData).openShop(target,true)
                }
            }
            execute<Player> { sender, context, argument ->
                val data = Shop.datas.getOrPut(context["shop"]) {
                    ShopData(context["shop"])
                }
                (data as ShopData).openShop(sender,true)
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
                sender.sendMessageAsLang("command-list", context["shop"])
                data.commodity.forEach {
                    sender.sendMessageAsLang("command-list-info", it.id ?: it.uuid, it.showName)
                }
            }
        }
    }

    @CommandBody
    val listShop = subCommand {
        execute<CommandSender> { sender, context, argument ->
            sender.sendMessageAsLang("command-listshop")
            Shop.datas.keys().toList().forEach {
                sender.sendMessageAsLang("command-listshop-info", it)
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
                    sender.sendMessageAsLang("command-remove-error")
                }
                Shop.datas.remove(context.argument(0))
                sender.sendMessageAsLang("command-remove-success")
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, context, argument ->
            Shop.config.reload()
            UIReader.load()
            Language.reload()
            sender.sendMessageAsLang("command-reload")
        }
    }

}