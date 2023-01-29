package ray.mintcat.shop.data

import org.bukkit.Material
import org.bukkit.entity.Player
import ray.mintcat.shop.data.materials.MaterialFeed
import ray.mintcat.shop.data.materials.ShopMaterialData
import ray.mintcat.shop.utils.color
import ray.mintcat.shop.utils.display
import ray.mintcat.shop.utils.infoTitle
import ray.mintcat.shop.utils.sendMessageAsLang
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.nms.getName
import taboolib.module.nms.inputSign
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.*
import java.util.*

class ShopCommodityData(
    val uuid: UUID,
    var id: String? = "未命名商品",
    var item: ShopMaterialData,
    var price: Double,
    var buy: Double,
    var moneyType: String = "Vault",
    var give: Boolean = true,
    var showName: String = item.create(null)?.getName() ?: item.id,
    var info: List<String> = listOf(),
    var condition: List<String> = listOf(),
    var action: List<String> = listOf(),
    var actionBuy: List<String> = listOf(),
    var actionSell: List<String> = listOf(),
    var buyItems: List<ShopMaterialData>? = listOf(),
) {
    init {
        if (buyItems == null) {
            buyItems = listOf()
        }
    }


    fun openEdit(player: Player, father: ShopData) {
        player.openMenu<Basic>(
            player.asLangText(
                "manageui-edit-title",
                uuid, id ?: uuid.toString(), showName
            )
        ) {
            map(
                "O#######L",
                "###KEN###",
                "#A#DBCMJ#",
                "###IHFG##",
                "#########",
            )
            set('A', buildItem(item.create(player) ?: buildItem(Material.BARRIER) {
                name = player.asLangText("manageui-edit-main-noitem", item.form, item.id)
                colored()
            }) {
                lore.addAll(
                    player.asLangTextList("manageui-edit-main-lore")
                )
                colored()
            }) {
                submit(delay = 1) {
                    player.openMenu<Basic>(player.asLangText("manageui-import-title").colored()) {
                        map("####@####")
                        handLocked(false)
                        set('#', buildItem(XMaterial.BLACK_STAINED_GLASS_PANE) {
                            name = " "
                            colored()
                        }) {
                            isCancelled = true
                        }
                        onClick(lock = false)
                        onClose {
                            val item = it.inventory.getItem(4) ?: return@onClose kotlin.run {
                                player.sendMessageAsLang("systemmessage-edit-error")
                                submit(delay = 1) {
                                    openEdit(player, father)
                                }
                            }
                            if (item.isAir) {
                                player.sendMessageAsLang("systemmessage-edit-error")
                                submit(delay = 1) {
                                    openEdit(player, father)
                                }
                                return@onClose
                            }
                            this@ShopCommodityData.item = MaterialFeed.toMaterial(item)!!
                            player.sendMessageAsLang("systemmessage-edit-success", item.getName())
                            submit(delay = 1) {
                                openEdit(player, father)
                            }
                        }
                    }
                }
            }

            set('M', buildItem(XMaterial.CHEST) {
                this.name = player.asLangText("manageui-edit-itemneed")
                lore.add(" ")
                lore.addAll(
                    buyItems!!.map { "&f- ${it.form}_${it.getNameId(player)} &fX${it.amount}" }
                )
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-edit"))
                colored()
            }) {
                submit(delay = 1) {
                    player.openMenu<Basic>(player.asLangText("manageui-import-title").colored()) {
                        rows(6)
                        onBuild { player, inventory ->
                            buyItems!!.forEach {
                                inventory.addItem(it.create(player))
                            }
                        }
                        handLocked(false)
                        onClick(lock = false)
                        onClose {
                            val items = it.inventory.contents.filter { z -> z != null && z.isNotAir() }
                            if (items.isEmpty()) {
                                player.sendMessageAsLang("systemmessage-edit-error")
                                submit(delay = 1) {
                                    openEdit(player, father)
                                }
                                return@onClose
                            }
                            val datas = items.mapNotNull { z -> MaterialFeed.toMaterial(z) }
                            this@ShopCommodityData.buyItems = datas
                            player.sendMessageAsLang("systemmessage-edit-success")
                            submit(delay = 1) {
                                openEdit(player, father)
                            }
                        }
                    }
                }
            }

            set('B', buildItem(XMaterial.IRON_INGOT) {
                name = player.asLangText("manageui-edit-buy", price)
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-edit"))
                colored()
            }) {
                player.closeInventory()
                player.inputSign(player.asLangTextList("sing-buyedit", price).toTypedArray()) { lens ->
                    val new = lens[0].toDoubleOrNull() ?: 0.0
                    if (new < 0.0 || new == price) {
                        player.sendMessageAsLang("systemmessage-sing-nonumber")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputSign
                    }
                    price = new
                    player.sendMessageAsLang("systemmessage-edit-success")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputSign
                }
            }

            set('C', buildItem(XMaterial.GOLD_INGOT) {
                name = player.asLangText("manageui-edit-sell", buy)
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-edit"))
                colored()
            }) {
                player.closeInventory()
                player.inputSign(player.asLangTextList("sing-selledit", buy).toTypedArray()) { lens ->
                    val new = lens[0].toDoubleOrNull() ?: 0.0
                    if (new < 0.0 || new == buy) {
                        player.sendMessageAsLang("systemmessage-sing-nonumber")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputSign
                    }
                    buy = new
                    player.sendMessageAsLang("systemmessage-edit-success")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputSign
                }
            }

            set('D', buildItem(XMaterial.EMERALD) {
                name = player.asLangText("manageui-edit-moneytype", moneyType)
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-edit"))
                colored()
            }) {
                player.closeInventory()
                player.inputSign(player.asLangTextList("sing-moneytype", moneyType).toTypedArray()) { lens ->
                    val new = lens[0]
                    if (new.isEmpty() || new == moneyType) {
                        player.sendMessageAsLang("systemmessage-edit-giveup")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputSign
                    }
                    moneyType = new
                    player.sendMessageAsLang("systemmessage-edit-success")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputSign
                }
            }

            set('E', buildItem(XMaterial.PAPER) {
                name = player.asLangText("manageui-edit-addinfo")
                lore.addAll(info.map { "&f${it.color()}" })
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-edit"))
                colored()
            }) {
                player.closeInventory()
                player.infoTitle(
                    player.asLangText("manageui-edit-addinfo-main"),
                    player.asLangText("manageui-edit-addinfo-sub")
                )
                player.inputBook(player.asLangText("manageui-edit-addinfo-book"), true, info) { lens ->
                    info = lens
                    player.sendMessageAsLang("systemmessage-edit-success")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputBook
                }
            }

            set('F', buildItem(XMaterial.COMPARATOR) {
                name = player.asLangText("manageui-edit-action-buy")
                lore.addAll(actionSell.map { "&f- ${it.color()}" })
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-action-buy-info"))
                lore.add(player.asLangText("manageui-edit-edit"))
                colored()
            }) {
                player.closeInventory()
                player.infoTitle(
                    player.asLangText("manageui-edit-action-buy-book"),
                    player.asLangText("manageui-edit-action-buy-info"),
                )
                player.inputBook(player.asLangText("manageui-edit-action-buy-book"), true, actionSell) { lens ->
                    actionSell = lens
                    player.sendMessageAsLang("systemmessage-edit-success")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputBook
                }
            }
            set('G', buildItem(XMaterial.REPEATER) {
                name = player.asLangText("manageui-edit-action-sell")
                lore.addAll(actionBuy.map { "&f- ${it.color()}" })
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-action-sell-info"))
                lore.add(player.asLangText("manageui-edit-edit"))
                colored()
            }) {
                player.closeInventory()
                player.infoTitle(
                    player.asLangText("manageui-edit-action-sell-book"),
                    player.asLangText("manageui-edit-action-sell-info"),
                )
                player.inputBook(player.asLangText("manageui-edit-action-sell-book"), true, actionBuy) { lens ->
                    actionBuy = lens
                    player.sendMessageAsLang("systemmessage-edit-success")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputBook
                }
            }
            set('H', buildItem(XMaterial.REDSTONE) {
                name = player.asLangText("manageui-edit-action")
                lore.addAll(action.map { "&f- ${it.color()}" })
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-action-info"))
                lore.add(player.asLangText("manageui-edit-edit"))
                colored()
            }) {
                player.closeInventory()
                player.infoTitle(
                    player.asLangText("manageui-edit-action-book"),
                    player.asLangText("manageui-edit-action-info"),
                )
                player.inputBook(player.asLangText("manageui-edit-action-book"), true, action) { lens ->
                    action = lens
                    player.sendMessageAsLang("systemmessage-edit-success")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputBook
                }
            }
            set('I', buildItem(XMaterial.OBSERVER) {
                name = player.asLangText("manageui-edit-condition")
                lore.addAll(condition.map { "&f- ${it.color()}" })
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-condition-info"))
                lore.add(player.asLangText("manageui-edit-edit"))
                colored()
            }) {
                player.closeInventory()
                player.infoTitle(
                    player.asLangText("manageui-edit-condition-book"),
                    player.asLangText("manageui-edit-condition-info"),
                )
                player.inputBook(player.asLangText("manageui-edit-condition-book"), true, condition) { lens ->
                    condition = lens
                    player.sendMessageAsLang("systemmessage-edit-success")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputBook
                }
            }

            set('J', buildItem(XMaterial.HOPPER) {
                name = player.asLangText("manageui-edit-giveitem", give.display)
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-edit"))
                colored()
            }) {
                player.closeInventory()
                player.sendMessageAsLang("systemmessage-edit-success", give.display)
                give = !give
                submit(delay = 1) {
                    openEdit(player, father)
                }
            }

            set('K', buildItem(XMaterial.NAME_TAG) {
                name = player.asLangText("manageui-edit-showname", showName)
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-edit"))
                colored()
            }) {
                player.closeInventory()
                player.inputSign(player.asLangTextList("sing-showname", showName).toTypedArray()) { lens ->
                    val new = lens[0]
                    if (new.isEmpty() || new == showName) {
                        player.sendMessageAsLang("systemmessage-edit-error")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputSign
                    }
                    showName = new
                    player.sendMessageAsLang("systemmessage-edit-success")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputSign
                }
            }

            set('N', buildItem(XMaterial.IRON_BARS) {
                name = player.asLangText("manageui-edit-indexname", id ?: "null")
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-edit"))
                colored()
            }) {
                player.closeInventory()
                player.inputSign(player.asLangTextList("sing-indexname", showName).toTypedArray()) { lens ->
                    val new = lens[0]
                    if (new.isEmpty() || new == id) {
                        player.sendMessageAsLang("systemmessage-edit-error")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputSign
                    }
                    id = new
                    player.sendMessageAsLang("systemmessage-edit-success")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputSign
                }
            }

            set('L', buildItem(XMaterial.LAVA_BUCKET) {
                name = player.asLangText("manageui-edit-remove-name")
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-remove-info"))
                colored()
            }) {
                player.closeInventory()
                player.inputSign(player.asLangTextList("sing-remove").toTypedArray()) { lens ->
                    val new = lens[0]
                    if (new.isEmpty() || new != "Y") {
                        player.sendMessageAsLang("systemmessage-edit-remove-error")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputSign
                    }
                    father.commodity.remove(this@ShopCommodityData)
                    player.sendMessageAsLang("systemmessage-edit-remove-success")
                    submit(delay = 1) {
                        father.openShop(player)
                    }
                    return@inputSign
                }
            }

            set('O', buildItem(XMaterial.SLIME_BALL) {
                name = player.asLangText("manageui-edit-back-name")
                lore.add(" ")
                lore.add(player.asLangText("manageui-edit-back-info"))
                colored()
            }) {
                player.closeInventory()
                submit(delay = 1) {
                    father.openShop(player)
                }
            }
        }
    }

}