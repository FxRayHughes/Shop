package ray.mintcat.shop.data

import com.google.gson.annotations.Expose
import org.bukkit.Material
import org.bukkit.entity.Player
import ray.mintcat.shop.Shop
import ray.mintcat.shop.data.materials.MaterialFeed
import ray.mintcat.shop.data.materials.ShopMaterialData
import ray.mintcat.shop.utils.*
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.module.nms.getName
import taboolib.module.nms.inputSign
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.buildItem
import taboolib.platform.util.inputBook
import taboolib.platform.util.isAir
import java.util.*

class ShopCommodityData(
    @Expose
    val uuid: UUID,
    @Expose
    var item: ShopMaterialData,
    @Expose
    var price: Double,
    @Expose
    var buy: Double,
    @Expose
    var give: Boolean = true,
    @Expose
    var showName: String = item.create(null)?.getName() ?: item.id,
    @Expose
    var info: List<String> = listOf(),
    @Expose
    var condition: List<String> = listOf(),
    @Expose
    var action: List<String> = listOf(),
    @Expose
    var actionBuy: List<String> = listOf(),
    @Expose
    var actionSell: List<String> = listOf()
) {

    fun openEdit(player: Player, father: ShopData) {
        player.openMenu<Basic>("正在编辑${uuid}...") {
            map(
                "#########",
                "####A####",
                "#B#C#D#E#",
                "#M#N#H#I#",
                "#G#F#J#K#",
                "####L####",
            )
            set('A', buildItem(item.create(player) ?: buildItem(Material.BARRIER) {
                name = "&4物品不存在&e ${item.form}:${item.id}"
                colored()
            }) {
                lore.add(" ")
                lore.add("&7点击修改物品")
            }) {
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
                                player.error("修改失败")
                                submit(delay = 1) {
                                    openEdit(player, father)
                                }
                            }
                            if (item.isAir) {
                                player.error("修改失败")
                                submit(delay = 1) {
                                    openEdit(player, father)
                                }
                                return@onClose
                            }
                            this@ShopCommodityData.item = MaterialFeed.toMaterial(item)!!
                            player.info("成功修改为! ${item.getName()}")
                            submit(delay = 1) {
                                openEdit(player, father)
                            }
                        }
                    }
                }
            }

            set('B', buildItem(XMaterial.IRON_INGOT) {
                name = "&7编辑出售价:&f ${price}"
                lore.add(" ")
                lore.add("&7点击编辑")
            }) {
                player.closeInventory()
                player.inputSign(arrayOf("$price", "出售单价", "第一行输入新的价格", "点击确认确定")) { lens ->
                    val new = lens[0].toDoubleOrNull() ?: 0.0
                    if (new < 0.0 || new == price) {
                        player.error("价格错误")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputSign
                    }
                    price = new
                    player.info("编辑成功!")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputSign
                }
            }

            set('C', buildItem(XMaterial.GOLD_INGOT) {
                name = "&7编辑收购价:&f ${buy}"
                lore.add(" ")
                lore.add("&7点击编辑")
            }) {
                player.closeInventory()
                player.inputSign(arrayOf("$buy", "回收单价", "第一行输入新的价格", "点击确认确定")) { lens ->
                    val new = lens[0].toDoubleOrNull() ?: 0.0
                    if (new < 0.0 || new == buy) {
                        player.error("价格错误")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputSign
                    }
                    buy = new
                    player.info("编辑成功!")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputSign
                }
            }

            set('D', buildItem(XMaterial.EMERALD) {
                name = "&7编辑货币类型:&f 暂不支持"
                lore.add(" ")
                lore.add("&7点击编辑")
            })

            set('E', buildItem(XMaterial.PAPER) {
                name = "&7编辑商品额外显示信息:"
                lore.addAll(info.map { "&f${it.color()}" })
                lore.add(" ")
                lore.add("&7点击编辑")
            }) {
                player.closeInventory()
                player.infoTitle("&3编辑书本: 修改商品额外信息", "&7此内容显示在商品下方")
                player.inputBook("商品额外信息", true, info) { lens ->
                    if (lens.isEmpty() || lens == info) {
                        player.error("内容错误")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputBook
                    }
                    info = lens
                    player.info("编辑成功!")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputBook
                }
            }

            set('F', buildItem(XMaterial.COMPARATOR) {
                name = "&7编辑商品收购后动作"
                lore.addAll(actionSell.map { "&f- ${it.color()}" })
                lore.add(" ")
                lore.add("&7动作为Kether脚本购买后执行数量次")
                lore.add("&7点击编辑")
            }) {
                player.closeInventory()
                player.infoTitle("&3编辑书本: 修改收购购买后动作", "&7动作为Kether脚本收购后执行数量次")
                player.inputBook("成功动作", true, actionSell) { lens ->
                    if (lens.isEmpty() || lens == actionSell) {
                        player.error("内容错误")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputBook
                    }
                    actionSell = lens
                    player.info("编辑成功!")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputBook
                }
            }
            set('G', buildItem(XMaterial.REPEATER) {
                name = "&7编辑商品出售后动作"
                lore.addAll(actionBuy.map { "&f- ${it.color()}" })
                lore.add(" ")
                lore.add("&7动作为Kether脚本回收后执行数量次")
                lore.add("&7点击编辑")
            }) {
                player.closeInventory()
                player.infoTitle("&3编辑书本: 修改商品出售后动作", "&7动作为Kether脚本物品回收后执行数量次")
                player.inputBook("成功动作", true, actionBuy) { lens ->
                    if (lens.isEmpty() || lens == actionBuy) {
                        player.error("内容错误")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputBook
                    }
                    actionBuy = lens
                    player.info("编辑成功!")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputBook
                }
            }
            set('H', buildItem(XMaterial.REDSTONE) {
                name = "&7编辑商品固有动作"
                lore.addAll(action.map { "&f- ${it.color()}" })
                lore.add(" ")
                lore.add("&7动作为Kether脚本购买/出售后执行一次")
                lore.add("&7点击编辑")
            }) {
                player.closeInventory()
                player.infoTitle("&3编辑书本: 修改商品购买后动作", "&7动作为Kether脚本购买、出售后执行一次")
                player.inputBook("成功动作", true, action) { lens ->
                    if (lens.isEmpty() || lens == action) {
                        player.error("内容错误")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputBook
                    }
                    action = lens
                    player.info("编辑成功!")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputBook
                }
            }
            set('I', buildItem(XMaterial.OBSERVER) {
                name = "&7编辑商品购买条件"
                lore.addAll(condition.map { "&f- ${it.color()}" })
                lore.add(" ")
                lore.add("&7点击编辑")
            }) {
                player.closeInventory()
                player.infoTitle("&3编辑书本: 修改商品购买条件", "&7条件为Kether脚本返回True则为可购买/出售")
                player.inputBook("购买条件", true, condition) { lens ->
                    if (lens.isEmpty() || lens == condition) {
                        player.error("内容错误")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputBook
                    }
                    condition = lens
                    player.info("编辑成功!")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputBook
                }
            }

            set('J', buildItem(XMaterial.HOPPER) {
                name = "&7是否给予展示物品: ${give.display}"
                lore.add(" ")
                lore.add("&7点击编辑")
            }) {
                player.closeInventory()
                player.info("修改成功!")
                give = !give
                submit(delay = 1) {
                    openEdit(player, father)
                }
            }

            set('K', buildItem(XMaterial.NAME_TAG) {
                name = "&7商品显示名: ${showName}"
                lore.add(" ")
                lore.add("&7点击编辑")
            }) {
                player.closeInventory()
                player.inputSign(arrayOf(showName, "", "第一行输入显示名", "点击确认确定")) { lens ->
                    val new = lens[0]
                    if (new.isEmpty() || new == showName) {
                        player.error("内容错误")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputSign
                    }
                    showName = new
                    player.info("编辑成功!")
                    submit(delay = 1) {
                        openEdit(player, father)
                    }
                    return@inputSign
                }
            }

            set('L', buildItem(XMaterial.LAVA_BUCKET) {
                name = "&4删除商品"
                lore.add(" ")
                lore.add("&7点击删除")
            }) {
                player.closeInventory()
                player.inputSign(arrayOf("", "", "第一行输入 Y", "点击确认确定")) { lens ->
                    val new = lens[0]
                    if (new.isEmpty() || new != "Y") {
                        player.error("取消删除")
                        submit(delay = 1) {
                            openEdit(player, father)
                        }
                        return@inputSign
                    }
                    father.commodity.remove(this@ShopCommodityData)
                    player.info("删除成功")
                    submit(delay = 1) {
                        father.openShop(player)
                    }
                    return@inputSign
                }
            }
            onClose {
                Shop.save()
            }
        }
    }

}