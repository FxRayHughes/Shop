package ray.mintcat.shop.data

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import ray.mintcat.shop.PAPIHook
import ray.mintcat.shop.Shop
import ray.mintcat.shop.UIReader
import ray.mintcat.shop.data.materials.MaterialFeed
import ray.mintcat.shop.data.materials.ShopMaterialData
import ray.mintcat.shop.utils.*
import taboolib.common.platform.function.submit
import taboolib.expansion.ioc.annotation.Component
import taboolib.library.xseries.XMaterial
import taboolib.library.xseries.getItemStack
import taboolib.module.chat.colored
import taboolib.module.nms.getName
import taboolib.module.nms.inputSign
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.replacePlaceholder
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
        val uiName = if (edit) {
            editName + getShowNameInfo()
        } else {
            getShowNameInfo()
        }
        player.openMenu<Linked<ShopCommodityData>>(uiName) {
            val config = UIReader.getUIConfig(this@ShopData)
            inits(this@ShopData, player,edit)
            virtualize()
            elements {
                if (player.isOp) {
                    commodity
                } else {
                    commodity.filter { it.condition.check(player, this@ShopData).get() }
                }
            }
            onGenerate { player, element, index, slot ->
                buildItem(element.item.create(player).papi(player) ?: buildItem(Material.BARRIER) {
                    name = player.asLangText("shopui-noitem", element.item.form, element.item.id)
                    colored()
                }) {
                    name = element.showName
                    if (element.info.isNotEmpty()) {
                        lore.addAll(element.info.map { "&f${it}".replacePlaceholder(player) })
                    }
                    lore.add(" ")
                    if (element.price > 0.0) {
                        if (element.getPriceNew(player, this@ShopData) != element.price) {
                            lore.addAll(
                                player.asLangTextList(
                                    "shopui-buy-discount",
                                    element.price,
                                    Vault.getName(element.moneyType)
                                )
                            )
                        }
                        lore.addAll(
                            player.asLangTextList(
                                "shopui-buy",
                                element.getPriceNew(player, this@ShopData),
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
                    set("RAYSHOPSELLDISCOUNT", element.getPriceNew(player, this@ShopData))
                    set("RAYSHOPSELL", element.price)
                    set("RAYSHOPTYPE", element.moneyType)
                    set("RAYSHOPTYPEINFO", Vault.getName(element.moneyType))
                }
            }

            if (player.isOp && edit) {
                val rn = config.getString("ReName")?.asChar()
                if (rn != null){
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
                                openShop(player, edit)
                            }
                        }
                    }
                }
                val sl = config.getString("CreateItem")?.asChar()
                if (sl != null){
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
                                openShop(player, edit)
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
                                            openShop(player, edit)
                                        }
                                    }
                                    val create = MaterialFeed.toMaterial(item)
                                    if (create == null) {
                                        player.sendMessageAsLang("systemmessage-create-error")
                                        submit(delay = 1) {
                                            openShop(player, edit)
                                        }
                                        return@onClose
                                    }
                                    val id = UUID.randomUUID()
                                    val commoditys =
                                        ShopCommodityData(id, id.toString(), create, 0.0, 0.0)
                                    commodity.add(commoditys)
                                    player.sendMessageAsLang("systemmessage-create-success", item.getName())
                                    submit(delay = 1) {
                                        openShop(player, edit)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            onClick { event, element ->
                if (event.clickEvent().click == ClickType.DROP && player.isOp && edit) {
                    player.inputSign(player.asLangTextList("sign-remove").toTypedArray()) { lens ->
                        val new = lens[0]
                        if (new.isEmpty() || !new.contains("Y", true)) {
                            player.sendMessageAsLang("systemmessage-edit-remove-error")
                            submit(delay = 1) {
                                openShop(player, edit)
                            }
                            return@inputSign
                        }
                        commodity.remove(element)
                        player.sendMessageAsLang("systemmessage-edit-remove-success")
                        submit(delay = 1) {
                            openShop(player, edit)
                        }
                        return@inputSign
                    }
                    return@onClick
                }
                if (event.clickEvent().isLeftClick && event.clickEvent().isShiftClick && player.isOp && edit) {
                    player.closeInventory()
                    Shop.copy[player.uniqueId] = element
                    player.sendMessageAsLang("manageui-copy")
                    submit(delay = 1) {
                        openShop(player, edit)
                    }
                    return@onClick
                }
                if (event.clickEvent().isRightClick && event.clickEvent().isShiftClick && player.isOp && edit) {
                    player.closeInventory()
                    submit(delay = 1) {
                        element.openEdit(player, this@ShopData)
                    }
                    return@onClick
                }
                if (event.clickEvent().isRightClick && element.buy > 0) {
                    //回收
                    player.closeInventory()
                    when (config.getString("InteractiveMode.type")) {
                        "SIGN", "sign" -> {
                            sellSign(player, element, edit)
                        }

                        "CHEST", "chest" -> {
                            openInteractiveUI(player, element, edit)
                        }
                    }
                    return@onClick
                }
                if ((event.clickEvent().isLeftClick && element.price > 0.0) || (event.clickEvent().isLeftClick && !element.buyItems.isNullOrEmpty())) {
                    //出售
                    player.closeInventory()
                    when (config.getString("InteractiveMode.type")) {
                        "SIGN", "sign" -> {
                            buySign(player, element, edit)
                        }

                        "CHEST", "chest" -> {
                            openInteractiveUI(player, element, edit)
                        }
                    }
                    return@onClick
                }
            }

        }
    }

    fun sell(player: Player, amount: Int, element: ShopCommodityData): Boolean {
        if (!element.condition.check(player, this@ShopData).get()) {
            player.sendMessageAsLang("systemmessage-sell-codition")
            return false
        }
        if (element.item.amount(player) < amount) {
            player.sendMessageAsLang("systemmessage-item-nohave")
            return false
        }
        player.inventory.takeItem(amount) {
            element.item.lib().isItem(it, element.item.id)
        }
        val money = amount * element.buy
        Vault.addMoney(player, money, element.moneyType)
        (1..amount).forEach {
            element.actionSell.replace("=money=", money.toString()).toList().eval(player, this, element, amount)
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
            player.sendMessageAsLang("systemmessage-sign-number")
            return false
        }
        if (!element.condition.check(player, this@ShopData).get()) {
            player.sendMessageAsLang("systemmessage-buy-codition")
            return false
        }
        val money = amount * element.getPriceNew(player, this@ShopData)
        if (Vault.getMoney(player, element.moneyType) < money) {
            val need = Vault.getMoney(player, element.moneyType) - (money)
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
            (1..amount).forEach {
                element.actionBuy.replace("=money=", money.toString()).toList().eval(player, this, element, amount)
            }
            element.action.replace("=money=", money.toString()).toList().eval(player)
            player.sendMessageAsLang(
                "systemmessage-buy-success",
                element.showName, amount,
                money,
                Vault.getName(element.moneyType)
            )
        }
        return true
    }

    fun buySign(player: Player, element: ShopCommodityData, edit: Boolean) {
        player.inputSign(
            player.asLangTextList("sign-sell", element.price).colored().toTypedArray()
        ) { len ->
            val amount = len[0].replace("[^0-9]", "").toIntOrNull() ?: 0
            if (amount <= 0) {
                player.sendMessageAsLang("systemmessage-sign-number")
                submit(delay = 1) {
                    openShop(player, edit)
                }
                return@inputSign
            }
            buy(player, amount, element)
            submit(delay = 1) {
                openShop(player, edit)
            }
        }
    }

    fun sellSign(player: Player, element: ShopCommodityData, edit: Boolean) {
        player.inputSign(
            player.asLangTextList("sign-sell", element.buy).colored().toTypedArray()
        ) { len ->
            val amount = len[0].replace("[^0-9]", "").toIntOrNull() ?: 0
            if (amount <= 0) {
                player.sendMessageAsLang("systemmessage-sign-number")
                submit(delay = 1) {
                    openShop(player, edit)
                }
                return@inputSign
            }
            sell(player, amount, element)
            submit(delay = 1) {
                openShop(player, edit)
            }
        }
    }

    fun openInteractiveUI(player: Player, element: ShopCommodityData, edit: Boolean) {
        PAPIHook.map[player.uniqueId] = this to element
        val config = UIReader.getUIConfig(this@ShopData)
        val uiName = config.getString("InteractiveMode.ChestName")!!
            .replace("{name}", element.showName)
        player.openMenu<Linked<ShopMaterialData>>(uiName) {
            map(*config.getStringList("InteractiveMode.Layout").toTypedArray())
            slotsBy(config.getString("InteractiveMode.Material", "@")!!.asChar())
            elements {
                element.buyItems ?: listOf()
            }
            onGenerate { player, element, index, slot ->
                element.create(player) ?: buildItem(Material.STONE) {
                    name = player.asLangText("manageui-edit-main-noitem", element.form, element.id)
                }
            }
            onClick { event, element ->
                event.isCancelled = true
            }
            val nextChar = config.getString("InteractiveMode.NextItem.slot", "G")!!.asChar()
            this.setNextPage(getFirstSlot(nextChar)) { page, hasNextPage ->
                if (hasNextPage) {
                    config.getItemStack("InteractiveMode.NextItem.has").papi(player)
                        ?: buildItem(XMaterial.SPECTRAL_ARROW) {
                            name = "§f下一页"
                        }
                } else {
                    config.getItemStack("InteractiveMode.NextItem.normal").papi(player) ?: buildItem(XMaterial.ARROW) {
                        name = "§7下一页"
                    }
                }
            }
            val previoustChar = config.getString("InteractiveMode.PreviousItem.slot")?.asChar() ?: 'F'
            this.setPreviousPage(getFirstSlot(previoustChar)) { page, hasPreviousPage ->
                if (hasPreviousPage) {
                    config.getItemStack("InteractiveMode.PreviousItem.has").papi(player)
                        ?: buildItem(XMaterial.SPECTRAL_ARROW) {
                            name = "§f上一页"
                        }
                } else {
                    config.getItemStack("InteractiveMode.PreviousItem.normal").papi(player)
                        ?: buildItem(XMaterial.ARROW) {
                            name = "§7上一页"
                        }
                }
            }
            config.getConfigurationSection("InteractiveMode.OtherItem")?.getKeys(false)?.forEach { key ->
                config.getItemStack("InteractiveMode.OtherItem.${key}.item")?.papi(player)?.let {
                    set(key.asChar(), it) {
                        isCancelled = true
                        if (clickEvent().isLeftClick) {
                            if (clickEvent().isShiftClick) {
                                config.getStringList("InteractiveMode.OtherItem.${key}.action.left_shift").eval(player)
                                return@set
                            }
                            config.getStringList("InteractiveMode.OtherItem.${key}.action.left").eval(player)
                            return@set
                        }
                        if (clickEvent().isRightClick) {
                            if (clickEvent().isShiftClick) {
                                config.getStringList("InteractiveMode.OtherItem.${key}.action.right_shift").eval(player)
                                return@set
                            }
                            config.getStringList("InteractiveMode.OtherItem.${key}.action.right").eval(player)
                            return@set
                        }
                    }
                }
            }

            getSlots(config.getString("InteractiveMode.Commodity")!!.asChar()).forEach {
                set(it, element.item.create(player) ?: return@forEach) {
                    isCancelled = true
                }
            }

            getSlots(config.getString("InteractiveMode.Back.slot")!!.asChar()).forEach {
                set(it, config.getItemStack("InteractiveMode.Back.item")?.papi(player) ?: return@forEach) {
                    player.closeInventory()
                    submit(delay = 1) {
                        openShop(player, edit)
                    }
                }
            }

            getSlots(config.getString("InteractiveMode.Quit.slot")!!.asChar()).forEach {
                set(it, config.getItemStack("InteractiveMode.Quit.item")?.papi(player) ?: return@forEach) {
                    player.closeInventory()
                }
            }


            if (element.price > 0.0 || !config.getBoolean("InteractiveMode.Buy.hide")) {
                getSlots(config.getString("InteractiveMode.Buy.slot")!!.asChar()).forEach {
                    set(it, config.getItemStack("InteractiveMode.Buy.item")?.papi(player) ?: return@forEach) {
                        player.closeInventory()
                        Shop.amount.remove(player.uniqueId)
                        //打开数量选择
                        submit(delay = 1) {
                            //number
                            openAmountUI(player, element, edit, false)
                        }
                    }
                }
            }

            if (element.buy > 0.0 || element.buyItems?.isNotEmpty() == true || !config.getBoolean("InteractiveMode.Sell.hide")) {
                getSlots(config.getString("InteractiveMode.Sell.slot")!!.asChar()).forEach {
                    set(it, config.getItemStack("InteractiveMode.Sell.item")?.papi(player) ?: return@forEach) {
                        player.closeInventory()
                        Shop.amount.remove(player.uniqueId)
                        //打开数量选择
                        submit(delay = 1) {
                            //number
                            openAmountUI(player, element, edit, true)
                        }
                    }
                }
            }
        }
    }

    fun openAmountUI(player: Player, element: ShopCommodityData, edit: Boolean, sell: Boolean) {
        val amount = Shop.amount.getOrPut(player.uniqueId) { 0 }
        val config = UIReader.getUIConfig(this@ShopData)
        val typeName = if (sell) {
            config.getString("AmountUI.type.sell", "出售")!!
        } else {
            config.getString("AmountUI.type.buy", "购买")!!
        }
        val uiName = config.getString("AmountUI.ui_name")!!
            .replace("{name}", element.showName)
            .replace("{type}", typeName)
            .replace("{amount}", amount.toString())
        player.openMenu<Basic>(uiName) {
            map(*config.getStringList("AmountUI.Layout").toTypedArray())

            getSlots(config.getString("AmountUI.Commodity")!!.asChar()).forEach {
                set(it, element.item.create(player) ?: return@forEach) {
                    isCancelled = true
                }
            }
            getSlots(config.getString("AmountUI.Back.slot")!!.asChar()).forEach {
                set(it, config.getItemStack("AmountUI.Back.item")?.papi(player) ?: return@forEach) {
                    player.closeInventory()
                    submit(delay = 1) {
                        openInteractiveUI(player, element, edit)
                    }
                }
            }

            getSlots(config.getString("AmountUI.Confirm.slot")!!.asChar()).forEach {
                set(it, config.getItemStack("AmountUI.Confirm.item")?.papi(player) ?: return@forEach) {
                    player.closeInventory()
                    if (sell) {
                        sell(player, amount, element)
                    } else {
                        buy(player, amount, element)
                    }
                    Shop.amount.remove(player.uniqueId)
                }
            }
            getSlots(config.getString("AmountUI.Clear.slot")!!.asChar()).forEach {
                set(it, config.getItemStack("AmountUI.Clear.item")?.papi(player) ?: return@forEach) {
                    player.closeInventory()
                    Shop.amount.remove(player.uniqueId)
                    submit(delay = 1) {
                        openAmountUI(player, element, edit, sell)
                    }
                }
            }

            getSlots(config.getString("AmountUI.Max.slot")!!.asChar()).forEach {
                set(it, config.getItemStack("AmountUI.Max.item")?.papi(player) ?: return@forEach) {
                    player.closeInventory()
                    if (!sell) {
                        Shop.amount[player.uniqueId] = (Vault.getMoney(player, element.moneyType) / element.getPriceNew(
                            player,
                            this@ShopData
                        )).toInt()
                    } else {
                        Shop.amount[player.uniqueId] = element.item.lib().amount(player.inventory, element.item.id)
                    }
                    submit(delay = 1) {
                        openAmountUI(player, element, edit, sell)
                    }
                }
            }

            config.getConfigurationSection("AmountUI.AmountItem")?.getKeys(false)?.forEach z@{ key ->

                getSlots(key.asChar()).forEach {
                    set(it, config.getItemStack("AmountUI.AmountItem.${key}.item")?.papi(player) ?: return@z) {
                        val configAmount = config.getInt("AmountUI.AmountItem.${key}.amount")
                        if (clickEvent().isRightClick) {
                            Shop.amount[player.uniqueId] = if (amount - configAmount <= 0) {
                                0
                            } else {
                                amount - configAmount
                            }
                        } else {
                            Shop.amount[player.uniqueId] = amount + configAmount
                        }
                        openAmountUI(player, element, edit, sell)
                    }
                }

            }

        }
    }

}












