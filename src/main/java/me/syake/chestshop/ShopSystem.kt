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
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

class ShopSystem(private val main: ChestShop) {
    fun delItemInventory(inv: Inventory, item: ItemStack, amo: Int):Boolean {
        item.amount = 1
        var amount = 0
        val items = mutableListOf<ItemStack>()
        for(invItem in inv.contents) {
            if(invItem==null) {
                continue
            }
            val copyItem = invItem.clone()
            copyItem.amount = 1
            if(copyItem == item) {
                items.add(invItem)
                amount += invItem.amount
            }
        }
        if(amount < amo) {
            return false
        }
        amount = amo
        for (i in items) {
            if(i.amount==amount) {
                i.amount = 0
                return true
            } else if(i.amount>amount) {
                i.amount -= amount
                return true
            } else {
                amount -= i.amount
                i.amount = 0
            }
        }
        return false
    }
    fun hasItem(inv: Inventory, item: ItemStack, amo: Int): Boolean {
        item.amount = 1
        var amount = 0
        val items = mutableListOf<ItemStack>()
        for(invItem in inv.contents) {
            if(invItem==null) {
                continue
            }
            val copyItem = invItem.clone()
            copyItem.amount = 1
            if(copyItem == item) {
                items.add(invItem)
                amount += invItem.amount
            }
        }
        if(amount < amo) {
            return false
        }
        return true
    }
    fun hasInventoryArea(inv: Inventory, item: ItemStack, amo: Int): Boolean {
        item.amount = 1
        var amount = 0
        val items = mutableListOf<ItemStack>()
        for (i in inv.contents) {
            if (i == null) {
                amount += item.maxStackSize
                continue
            }
            val copyItem = i.clone()
            copyItem.amount = 1
            if (copyItem == item) {
                amount += 64 - i.amount
                items.add(i)
            }
        }
        if (amount < amo) {
            return false
        }
        return true
    }
    fun addItemInventory(inv: Inventory, item: ItemStack, amo: Int): Boolean {
        item.amount = 1
        var amount = 0
        val items = mutableListOf<ItemStack>()
        for(i in inv.contents) {
            if(i == null) {
                amount+=item.maxStackSize
                continue
            }
            val copyItem = i.clone()
            copyItem.amount = 1
            if(copyItem == item) {
                amount += 64-i.amount
                items.add(i)
            }
        }
        if(amount<amo) {
            return false
        }
        amount = amo
        Bukkit.broadcastMessage(amo.toString())
        for(i in items) {
            if(i.amount==i.maxStackSize) {
                continue
            }
            if(item.maxStackSize-i.amount==amount) {
                i.amount = item.maxStackSize
                return true
            } else if(item.maxStackSize-i.amount>amount) {
                i.amount+=amount
                return true
            } else {
                amount-=i.amount
                i.amount = item.maxStackSize
            }
        }
        while (true) {
            if(amount==item.maxStackSize) {
                item.amount = item.maxStackSize
                inv.addItem(item)
                return true
            } else if(amount<item.maxStackSize) {
                item.amount = amount
                inv.addItem(item)
                return true
            } else {
                item.amount = item.maxStackSize
                inv.addItem(item)
                amount -= item.maxStackSize
            }
        }
    }
    fun dropItem(player: Player, item: ItemStack, amo: Int) {
        var amount = amo
        while (true) {
            if (amount > item.maxStackSize) {
                item.amount = item.maxStackSize
                amount -= item.maxStackSize
                val dropItem = player.location.world!!.dropItemNaturally(player.location, item)
                dropItem.pickupDelay = 0
            } else {
                item.amount = amount
                val dropItem = player.location.world!!.dropItemNaturally(player.location, item)
                dropItem.pickupDelay = 0
                break
            }
        }
    }
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
        val amount = main.shops.config().getInt("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.amount")
        val buyPrice = main.shops.config().getInt("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.price")
        item?.amount = 1
        if(blockState.getLine(0)=="§c§lAdmin SHOP") {
            dropItem(event.player, item!!, amount)
            return
        }
        val emerald = ItemStack(Material.EMERALD)
        if(main.economy) {
            if(!(main.econ!!.has(Bukkit.getOfflinePlayer(event.player.uniqueId) ,buyPrice.toDouble()))) {
                event.player.sendMessage(main.lang.toMessage("balanceInsufficient", "Your balance is insufficient."))
                return
            }
            main.econ!!.withdrawPlayer(Bukkit.getOfflinePlayer(event.player.uniqueId), buyPrice.toDouble())
            main.econ!!.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)), (buyPrice.toDouble()/100)*(100-main.config.getInt("tax", 0)))
            dropItem(event.player, item!!, amount)
        } else {
            if(hasInventoryArea(chest.inventory, emerald, buyPrice)) {
                if(hasItem(chest.inventory, item!!, amount)) {
                    delItemInventory(chest.snapshotInventory, item, amount)
                    addItemInventory(chest.snapshotInventory, emerald, buyPrice)
                    dropItem(event.player, item, amount)
                    chest.update()
                } else {
                    event.player.sendMessage(main.lang.toMessage("outOfStock", "This shop does not have it in stock. Please wait until it's refilled or contact us at %player%.").replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(owner)).name!!))
                    return
                }
            } else {
                event.player.sendMessage(main.lang.toMessage("shopNoInventory", "I couldn't buy it because the shop is full. Please wait until the stock is low or contact %player%!").replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(owner)).name!!))
                return
            }
        }
    }
    fun onSignLeftClick(event: PlayerInteractEvent, block: Block, blockState: Sign, data: WallSign) {
    }
    private fun Config.toMessage(pass: String, default: String = "", prefix: Boolean = true):String {
        return ChatColor.translateAlternateColorCodes('&', (if(prefix) "${main.lang.config().getString("prefix", "&7[&6SHOP&7]&r")} " else "") + this.config().getString(pass, default))
    }
}