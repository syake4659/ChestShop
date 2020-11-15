package me.syake.chestshop

import me.syake.realfishing.Config
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class ShopSystem(private val main: ChestShop) {
    fun onSignRightClick(event: PlayerInteractEvent, block: Block, blockState: Sign, data: WallSign) {
        if(!(blockState.getLine(0)=="§e§lSHOP"||blockState.getLine(0)=="§c§lAdmin SHOP")) {
            return
        }
        var chest: Chest? = null
        when(data.facing) {
            BlockFace.EAST -> {
                chest = block.location.add(-1.0, 0.0, 0.0).block.state as Chest
            }
            BlockFace.SOUTH -> {
                chest = block.location.add(0.0, 0.0, -1.0).block.state as Chest
            }
            BlockFace.WEST -> {
                chest = block.location.add(1.0, 0.0, 0.0).block.state as Chest
            }
            BlockFace.NORTH -> {
                chest = block.location.add(0.0, 0.0, 1.0).block.state as Chest
            }
            else -> {
            }
        }
        if(!main.shops.config().getBoolean("${chest!!.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.buy")) {
            event.player.sendMessage(main.lang.toMessage("sellOnly", "This shop is for sell only."))
            return
        }
        if(!(main.shops.config().contains("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}"))) {
            return
        }
        val owner = main.shops.config().getString("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.owner")
        if(event.player.uniqueId.toString() == owner) {
            TODO("今後作成します。")
        }
        val item = main.shops.config().getItemStack("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.item")
        var amount = main.shops.config().getInt("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.amount")
        val buyPrice = main.shops.config().getInt("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.price")
        item?.amount = 1
        if(blockState.getLine(0)=="§c§lAdmin SHOP") {
            while (true) {
                if (amount > item!!.maxStackSize) {
                    item.amount = item.maxStackSize
                    amount -= item.maxStackSize
                    val dropItem = event.player.location.world!!.dropItemNaturally(event.player.location, item)
                    dropItem.pickupDelay = 0
                } else {
                    item.amount = amount
                    val dropItem = event.player.location.world!!.dropItemNaturally(event.player.location, item)
                    dropItem.pickupDelay = 0
                    break
                }
            }
            return
        }
        var amountTwo = amount
        var itemCount = 0
        for (i in chest.inventory.contents) {
            if (i == null) {
                continue
            }
            val invItem = i.clone()
            val invItemCount = invItem.amount
            invItem.amount = 1
            if (invItem == item) {
                itemCount += invItemCount
            }
        }
        if(itemCount>=amount) {
            if(main.economy) {
                if(!(main.econ!!.has(Bukkit.getOfflinePlayer(event.player.uniqueId) ,buyPrice.toDouble()))) {
                    event.player.sendMessage(main.lang.toMessage("balanceInsufficient", "Your balance is insufficient."))
                    return
                }
                main.econ!!.withdrawPlayer(Bukkit.getOfflinePlayer(event.player.uniqueId), buyPrice.toDouble())
                main.econ!!.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)), (buyPrice.toDouble()/100)*(100-main.config.getInt("tax", 0)))
            } else {
                if(!main.withdrawItem.withdraw(event.player, ItemStack(Material.EMERALD), buyPrice)) {
                    event.player.sendMessage(main.lang.toMessage("emeraldInsufficient", "Your emerald is insufficient."))
                    return
                }
            }
            while (true) {
                if (amount > item!!.maxStackSize) {
                    item.amount = item.maxStackSize
                    amount -= item.maxStackSize
                    val dropItem = event.player.location.world!!.dropItemNaturally(event.player.location, item)
                    dropItem.pickupDelay = 0
                } else {
                    item.amount = amount
                    val dropItem = event.player.location.world!!.dropItemNaturally(event.player.location, item)
                    dropItem.pickupDelay = 0
                    break
                }
            }
            for (i in chest.inventory.contents) {
                if (i == null) {
                    continue
                }
                item.amount = i.amount
                if (i == item) {
                    when {
                        i.amount == amountTwo -> {
                            i.amount = 0
                            chest.update()
                            return
                        }
                        i.amount <= amountTwo -> {
                            amountTwo -= i.amount
                            i.amount = 0
                        }
                        i.amount >= amountTwo -> {
                            i.amount -= amountTwo
                            chest.update()
                            return
                        }
                    }
                }
            }
        } else {
            event.player.sendMessage(main.lang.toMessage("outOfStock", "This shop does not have it in stock. Please wait until it's refilled or contact us at %player%.").replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(owner)).name!!))
            return
        }
    }
    fun onSignLeftClick(event: PlayerInteractEvent, block: Block, blockState: Sign, data: WallSign) {
    }
    private fun Config.toMessage(pass: String, default: String = "", prefix: Boolean = true):String {
        return ChatColor.translateAlternateColorCodes('&', (if(prefix) "${main.lang.config().getString("prefix", "&7[&6SHOP&7]&r")} " else "") + this.config().getString(pass, default))
    }
}