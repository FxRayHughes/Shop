package ray.mintcat.shop.data

import org.bukkit.Material
import org.bukkit.entity.Player
import ray.mintcat.shop.Shop
import ray.mintcat.shop.UIReader
import ray.mintcat.shop.data.materials.MaterialFeed
import ray.mintcat.shop.utils.*
import taboolib.common.platform.function.submit
import taboolib.expansion.ioc.annotation.Component
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.nms.getName
import taboolib.module.nms.inputSign
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.module.ui.type.Linked
import taboolib.platform.util.*
import java.util.*

@Component(index = "name")
class ShopData(
    val name: String,
    val commodity: MutableList<ShopCommodityData> = mutableListOf(),
    var showName: String? = name,
) {

    fun getShowNameInfo(): String {
        return showName ?: name
    }

    fun openShop(player: Player, edit: Boolean = false) {
        val editName = player.asLangText("shopui-edit")
        val uiName = if (edit){
            editName + getShowNameInfo()
        }else{
            getShowNameInfo()
        }
        player.openMenu<Linked<ShopCommodityData>>(uiName) {
            val config = UIReader.getUIConfig(this@ShopData)
            inits(this@ShopData, player)
            elements {
                if (player.isOp) {
                    commodity
                } else {
                    commodity.filter { it.condition.check(player).get() }
                }
            }
            onGenerate { player, element, index, slot ->
                buildItem(element.item.create(player) ?: buildItem(Material.BARRIER) {
                    name = player.asLangText("shopui-noitem", element.item.form, element.item.id)
                    colored()
                }) {
                    name = element.showName
                    if (element.info.isNotEmpty()) {
                        lore.addAll(element.info.map { "&f${it}" })
                    }
                    lore.add(" ")
                    if (element.price > 0.0) {
                        lore.addAll(
                            player.asLangTextList(
                                "shopui-buy",
                                element.price,
                                Vault.getName(element.moneyType)
                            )
                        )
                    }
                    if (element.buyItems?.isNotEmpty() == true) {
                        lore.addAll(player.asLangTextList("shopui-buyItem-title"))
                        element.buyItems!!.forEach {
                            lore.add(player.asLangText("shopui-buyItem-info", it.getNameShow(player), it.amount))
                        }
                    }
                    if (element.buy > 0.0) {
                        val maxBuy = element.item.lib().amount(player.inventory, element.item.id)
                        lore.addAll(
                            player.asLangTextList(
                                "shopui-sell",
                                element.buy,
                                Vault.getName(element.moneyType),
                                maxBuy
                            )
                        )
                    }
                    if (player.isOp && edit) {
                        lore.addAll(
                            player.asLangTextList(
                                "shopui-edit-normal",
                                element.uuid,
                                element.id ?: element.uuid,
                            )
                        )
                        if (element.condition.isNotEmpty()) {
                            lore.addAll(
                                player.asLangTextList(
                                    "shopui-edit-condition-title",
                                )
                            )
                            lore.addAll(element.condition.map {
                                player.asLangText("shopui-edit-condition-info", it)
                            })
                        }


                    }
                    colored()
                }.apply {
                    set("RAYSHOPUUID", element.uuid)
                    set("RAYSHOPID", element.id)
                    set("RAYSHOPBUY", element.buy)
                    set("RAYSHOPSELL", element.price)
                    set("RAYSHOPTYPE", element.moneyType)
                    set("RAYSHOPTYPEINFO", Vault.getName(element.moneyType))
                }
            }

            if (player.isOp && edit) {
                val rn = config.getString("ReName")?.asChar() ?: 'E'
                set(getFirstSlot(rn), buildItem(XMaterial.NAME_TAG) {
                    name = player.asLangText("manageui-rename-name")
                    if (Shop.copy[player.uniqueId] != null) {
                        lore.addAll(player.asLangTextList("manageui-rename-lore"))
                    }
                    colored()
                }) {
                    isCancelled = true
                    player.closeInventory()
                    player.infoTitle(
                        player.asLangText("manageui-rename-title-main").colored(),
                        player.asLangText("manageui-rename-title-sub").colored(),
                    )
                    player.inputBook(
                        player.asLangText("manageui-rename-book-name").colored(),
                        true,
                        listOf(getShowNameInfo())
                    ) {
                        val new = it.getOrElse(0) { getShowNameInfo() }
                        showName = new
                        player.sendMessageAsLang("systemmessage-edit-success", new)
                        submit(delay = 1) {
                            openShop(player,edit)
                        }
                    }
                }
                val sl = config.getString("CreateItem")?.asChar() ?: 'A'
                set(getFirstSlot(sl), buildItem(XMaterial.MAP) {
                    name = player.asLangText("manageui-create-name")
                    if (Shop.copy[player.uniqueId] != null) {
                        lore.addAll(player.asLangTextList("manageui-create-lore"))
                    }
                    colored()
                }) {
                    val copy = Shop.copy[player.uniqueId]
                    if (clickEvent().isShiftClick && clickEvent().isRightClick && copy != null) {
                        val id = UUID.randomUUID()
                        commodity.add(
                            ShopCommodityData(
                                id,
                                id.toString(),
                                copy.item, copy.price, copy.buy,
                                copy.moneyType,
                                copy.give, copy.showName, copy.info, copy.condition,
                                copy.action, copy.actionBuy, copy.actionSell
                            )
                        )
                        submit(delay = 1) {
                            openShop(player,edit)
                        }
                        return@set
                    }
                    player.closeInventory()
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
                                    player.sendMessageAsLang("systemmessage-create-error")
                                    submit(delay = 1) {
                                        openShop(player,edit)
                                    }
                                }
                                val create = MaterialFeed.toMaterial(item)
                                if (create == null) {
                                    player.sendMessageAsLang("systemmessage-create-error")
                                    submit(delay = 1) {
                                        openShop(player,edit)
                                    }
                                    return@onClose
                                }
                                val id = UUID.randomUUID()
                                val commoditys =
                                    ShopCommodityData(id, id.toString(), create, 0.0, 0.0)
                                commodity.add(commoditys)
                                player.sendMessageAsLang("systemmessage-create-success", item.getName())
                                submit(delay = 1) {
                                    openShop(player,edit)
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
                    player.sendMessageAsLang("manageui-copy")
                    submit(delay = 1) {
                        openShop(player,edit)
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
                        player.asLangTextList("sing-sell", element.buy).colored().toTypedArray()
                    ) { len ->
                        val amount = len[0].replace("[^0-9]", "").toIntOrNull() ?: 0
                        if (amount <= 0) {
                            player.sendMessageAsLang("systemmessage-sing-number")
                            submit(delay = 1) {
                                openShop(player,edit)
                            }
                            return@inputSign
                        }
                        sell(player, amount, element)
                        submit(delay = 1) {
                            openShop(player,edit)
                        }
                    }
                    return@onClick
                }
                if ((event.clickEvent().isLeftClick && element.price > 0.0) || (event.clickEvent().isLeftClick && !element.buyItems.isNullOrEmpty())) {
                    //出售
                    player.closeInventory()
                    player.inputSign(
                        player.asLangTextList("sing-sell", element.price).colored().toTypedArray()
                    ) { len ->
                        val amount = len[0].replace("[^0-9]", "").toIntOrNull() ?: 0
                        if (amount <= 0) {
                            player.sendMessageAsLang("systemmessage-sing-number")
                            submit(delay = 1) {
                                openShop(player,edit)
                            }
                            return@inputSign
                        }
                        buy(player, amount, element)
                        submit(delay = 1) {
                            openShop(player,edit)
                        }
                    }
                    return@onClick
                }
            }

        }
    }

    fun sell(player: Player, amount: Int, element: ShopCommodityData): Boolean {
        if (element.item.amount(player) < amount) {
            player.sendMessageAsLang("systemmessage-item-nohave")
            return false
        }
        player.inventory.takeItem(amount) {
            element.item.lib().isItem(it, element.item.id)
        }
        val money = amount * element.buy
        Vault.addMoney(player, money, element.moneyType)
        (1..amount).forEach { _ ->
            element.actionSell.replace("=money=", money.toString()).toList().eval(player)
        }
        element.action.replace("=money=", money.toString()).toList().eval(player)
        player.sendMessageAsLang(
            "systemmessage-sell-success",
            element.showName,
            amount,
            money, Vault.getName(element.moneyType)
        )
        return true
    }

    fun buy(player: Player, amount: Int, element: ShopCommodityData): Boolean {
        if (amount <= 0) {
            player.sendMessageAsLang("systemmessage-sing-number")
            return false
        }
        val money = amount * element.price
        if (Vault.getMoney(player, element.moneyType) < money) {
            val need = Vault.getMoney(player, element.moneyType) - (amount * element.price)
            player.sendMessageAsLang(
                "systemmessage-buy-no_money",
                need, Vault.getName(element.moneyType)
            )
            return false
        }
        if (element.buyItems?.isNotEmpty() == true) {
            element.buyItems!!.forEach {
                if (!it.lib().hasItem(player.inventory, it.id, it.amount * amount)) {
                    val need = (it.amount * amount) - (it.lib().amount(player.inventory, it.id))
                    player.sendMessageAsLang(
                        "systemmessage-buy-noitem",
                        it.getNameShow(player), need
                    )
                    return false
                }
            }
            element.buyItems!!.forEach {
                it.lib().takeItem(player.inventory, it.id, it.amount * amount)
            }
        }
        if (Vault.takeMoney(player, money, element.moneyType)) {
            if (element.give) {
                element.item.create(player)?.let {
                    player.giveItem(it, amount)
                }
            }
            (1..amount).forEach { _ ->
                element.actionBuy.replace("=money=", money.toString()).toList().eval(player)
            }
            element.action.replace("=money=", money.toString()).toList().eval(player)
            player.sendMessageAsLang(
                "systemmessage-buy-success",
                element.showName, amount,
                amount * element.price,
                Vault.getName(element.moneyType)
            )
        }
        return true
    }
}
