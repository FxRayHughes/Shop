package ray.mintcat.shop.data

import com.google.gson.annotations.Expose
import org.bukkit.Material
import org.bukkit.entity.Player
import ray.mintcat.shop.Shop
import ray.mintcat.shop.data.materials.MaterialFeed
import ray.mintcat.shop.utils.*
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.module.nms.getName
import taboolib.module.nms.inputSign
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.giveItem
import taboolib.platform.util.takeItem
import java.util.*

class ShopData(
    @Expose
    val name: String,
    @Expose
    val commodity: MutableList<ShopCommodityData> = mutableListOf()
) {

    fun openShop(player: Player) {
        player.openMenu<Linked<ShopCommodityData>>(name) {
            inits()
            elements {
                if (player.isOp) {
                    commodity
                } else {
                    commodity.filter { it.condition.check(player).get() }
                }
            }
            onGenerate { player, element, index, slot ->
                buildItem(element.item.create(player) ?: buildItem(Material.BARRIER) {
                    name = "&4物品不存在&e ${element.item.form}:${element.item.id}"
                    colored()
                }) {
                    name = element.showName
                    if (element.info.isNotEmpty()) {
                        lore.addAll(element.info.map { "&f${it.color()}".color() })
                    }
                    lore.add(" ")
                    if (element.price > 0.0) {
                        lore.add("&7出售价: &f${element.price}/个".color())
                        lore.add("&8左键购买".color())
                    }
                    if (element.buy > 0.0) {
                        lore.add("&7回收价: &f${element.buy}/个".color())
                        lore.add(
                            "&8右键出售 (&7最大${
                                element.item.lib().amount(player.inventory, element.item.id)
                            }&8)".color()
                        )
                    }
                    if (player.isOp) {
                        lore.add("&4以下内容仅管理员可见".color())
                        lore.add("&cUUID: &f${element.uuid}".color())
                        if (element.action.isNotEmpty()) {
                            lore.add("&c动作:".color())
                            lore.addAll(element.action.map { "&f-> $it" })
                        }
                        if (element.condition.isNotEmpty()) {
                            lore.add("&c条件:".color())
                            lore.addAll(element.condition.map { "&f-> $it" })
                        }
                        lore.add(" ")
                        lore.add("&4&lShift+右键打开编辑模式")
                        lore.add("&e&lShift+左键复制到粘贴板")
                        colored()
                    }
                }
            }

            if (player.isOp) {
                set(4, buildItem(XMaterial.MAP) {
                    name = "&6创建新商品 +"
                    if (Shop.copy[player.uniqueId] != null) {
                        lore.add("")
                        lore.add("&eShift+右键粘贴")
                    }
                    colored()
                }) {
                    val copy = Shop.copy[player.uniqueId]
                    if (clickEvent().isShiftClick && clickEvent().isRightClick && copy != null) {
                        commodity.add(
                            ShopCommodityData(
                                UUID.randomUUID(),
                                copy.item, copy.price, copy.buy,
                                copy.give, copy.showName, copy.info, copy.condition,
                                copy.action, copy.actionBuy, copy.actionSell
                            )
                        )
                        Shop.save()
                        submit(delay = 1) {
                            openShop(player)
                        }
                        return@set
                    }
                    player.closeInventory()
                    submit(delay = 1) {
                        player.openMenu<Basic>("请放入物品") {
                            map("####@####")
                            handLocked(false)
                            set('#', buildItem(XMaterial.BLACK_STAINED_GLASS_PANE) {
                                name = " "
                                colored()
                            }) {
                                it.isCancelled = true
                            }
                            onClick(lock = false)
                            onClose {
                                val item = it.inventory.getItem(4) ?: return@onClose kotlin.run {
                                    player.error("创建失败")
                                    submit(delay = 1) {
                                        openShop(player)
                                    }
                                }
                                val create = MaterialFeed.toMaterial(item)
                                if (create == null) {
                                    player.error("创建失败")
                                    submit(delay = 1) {
                                        openShop(player)
                                    }
                                    return@onClose
                                }
                                val commoditys = ShopCommodityData(UUID.randomUUID(), create, 0.0, 0.0)
                                commodity.add(commoditys)
                                player.info("创建成功! ${item.getName()}")
                                submit(delay = 1) {
                                    openShop(player)
                                }
                            }
                        }
                    }
                }
            }

            onClick { event, element ->
                if (event.clickEvent().isLeftClick && event.clickEvent().isShiftClick && player.isOp) {
                    player.closeInventory()
                    Shop.copy[player.uniqueId] = element
                    player.info("已复制到粘贴板!")
                    submit(delay = 1) {
                        openShop(player)
                    }
                    return@onClick
                }
                if (event.clickEvent().isRightClick && event.clickEvent().isShiftClick && player.isOp) {
                    player.closeInventory()
                    submit(delay = 1) {
                        element.openEdit(player, this@ShopData)
                    }
                    return@onClick
                }
                if (event.clickEvent().isRightClick && element.buy > 0) {
                    //回收
                    player.closeInventory()
                    player.inputSign(
                        arrayOf(
                            "",
                            "单价: ${element.buy}/个",
                            "第一行输入出售数量",
                            "点击确认进行出售"
                        )
                    ) { len ->
                        val amount = len[0].replace("[^0-9]", "").toIntOrNull() ?: 0
                        if (amount <= 0) {
                            player.error("请输入正确的数量!")
                            submit(delay = 1) {
                                openShop(player)
                            }
                            return@inputSign
                        }
                        if (element.item.amount < amount) {
                            player.error("你所拥有的物品不足!")
                            return@inputSign
                        }
                        player.inventory.takeItem(amount) {
                            element.item.lib().isItem(it, element.item.id)
                        }
                        val money = amount * element.buy
                        Vault.addMoney(player, money)
                        (1..amount).forEach { _ ->
                            element.actionSell.replace("=money=", money.toString()).toList().eval(player)
                        }
                        element.action.replace("=money=", money.toString()).toList().eval(player)
                        player.info("出售成功 出售${element.showName}X${amount} &7共获得&f ${money}元")
                        submit(delay = 1) {
                            openShop(player)
                        }
                    }
                    return@onClick
                }
                if (event.clickEvent().isLeftClick && element.price > 0.0) {
                    //出售
                    player.closeInventory()
                    player.inputSign(
                        arrayOf(
                            "",
                            "单价: ${element.price}/个",
                            "第一行输入购买数量",
                            "点击确认进行购买"
                        )
                    ) { len ->
                        val amount = len[0].replace("[^0-9]", "").toIntOrNull() ?: 0
                        if (amount <= 0) {
                            player.error("请输入正确的数量!")
                            submit(delay = 1) {
                                openShop(player)
                            }
                            return@inputSign
                        }
                        val money = amount * element.price
                        if (Vault.takeMoney(player, money)) {
                            if (element.give) {
                                player.giveItem(element.item.create(player) ?: return@inputSign, amount)
                            }
                            (1..amount).forEach { _ ->
                                element.actionBuy.replace("=money=", money.toString()).toList().eval(player)
                            }
                            element.action.replace("=money=", money.toString()).toList().eval(player)
                            player.info(
                                "购买成功 购买&f${element.showName}X${amount} &7共花费&f ${amount * element.price}元"
                            )
                        } else {
                            player.error(
                                "你缺少 &f${Vault.getMoney(player) - (amount * element.price)}元"
                            )
                        }
                        submit(delay = 1) {
                            openShop(player)
                        }
                    }
                    return@onClick
                }
            }

        }
    }
}
