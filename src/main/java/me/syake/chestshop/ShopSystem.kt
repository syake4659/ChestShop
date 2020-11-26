package me.syake.chestshop

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.data.type.WallSign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

class ShopSystem(private val main: ChestShop) {
    private fun delItemInventory(inv: Inventory, item: ItemStack, amo: Int):Boolean {
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
            when {
                i.amount==amount -> {
                    i.amount = 0
                    return true
                }
                i.amount>amount -> {
                    i.amount -= amount
                    return true
                }
                else -> {
                    amount -= i.amount
                    i.amount = 0
                }
            }
        }
        return false
    }
    private fun hasItem(inv: Inventory, item: ItemStack, amo: Int): Boolean {
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
    private fun hasInventoryArea(inv: Inventory, item: ItemStack, amo: Int): Boolean {
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
    private fun addItemInventory(inv: Inventory, item: ItemStack, amo: Int): Boolean {
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
        for(i in items) {
            if(i.amount==i.maxStackSize) {
                continue
            }
            when {
                item.maxStackSize-i.amount==amount -> {
                    i.amount = item.maxStackSize
                    return true
                }
                item.maxStackSize-i.amount>amount -> {
                    i.amount+=amount
                    return true
                }
                else -> {
                    amount-=i.amount
                    i.amount = item.maxStackSize
                }
            }
        }
        while (true) {
            when {
                amount==item.maxStackSize -> {
                    item.amount = item.maxStackSize
                    inv.addItem(item)
                    return true
                }
                amount<item.maxStackSize -> {
                    item.amount = amount
                    inv.addItem(item)
                    return true
                }
                else -> {
                    item.amount = item.maxStackSize
                    inv.addItem(item)
                    amount -= item.maxStackSize
                }
            }
        }
    }
    private fun dropItem(player: Player, item: ItemStack, amo: Int) {
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
    fun onSignRightClick(event: PlayerInteractEvent, block: Block, data: WallSign) {
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
        if(!(main.shops.config().contains("${chest!!.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}"))) {
            return
        }
        if(!main.shops.config().getBoolean("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.bought")) {
            event.player.sendMessage(main.lang.toMessage("sellOnly", "This shop is for sell only."))
            return
        }
        val owner = main.shops.config().getString("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.owner")
        if(event.player.uniqueId.toString() == owner) {
            return
        }
        val item = main.shops.config().getItemStack("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.item")
        val amount = main.shops.config().getInt("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.amount")
        val boughtPrice = main.shops.config().getInt("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.price")
        item?.amount = 1
        if(chest.isAdmin()) {
            event.player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
            if(main.economy) {
                if(!(main.econ!!.has(Bukkit.getOfflinePlayer(event.player.uniqueId) ,boughtPrice.toDouble()))) {
                    event.player.sendMessage(main.lang.toMessage("balanceInsufficient", "Your balance is insufficient."))
                    return
                }
                event.player.sendMessage(main.lang.toMessage("boughtItem", "You just bought %item% for %price%!").replace("%item%", "${item!!.type.name} x$amount").replace("%price%", main.econ!!.format(boughtPrice.toDouble())))
                main.econ!!.withdrawPlayer(Bukkit.getOfflinePlayer(event.player.uniqueId), boughtPrice.toDouble())
                dropItem(event.player, item, amount)
            } else {
                if(delItemInventory(event.player.inventory, main.currency, boughtPrice)) {
                    dropItem(event.player, item!!, amount)
                    event.player.sendMessage(main.lang.toMessage("boughtItem", "You just bought %item% for %price%!").replace("%item%", "${item.type.name} x$amount").replace("%price%", "$boughtPrice Emerald"))
                } else {
                    event.player.sendMessage(main.lang.toMessage("emeraldInsufficient", "Your emerald is insufficient."))
                    return
                }
            }
            return
        }
        if(main.economy) {
            if(!(main.econ!!.has(Bukkit.getOfflinePlayer(event.player.uniqueId) ,boughtPrice.toDouble()))) {
                event.player.sendMessage(main.lang.toMessage("balanceInsufficient", "Your balance is insufficient."))
                return
            }
            event.player.sendMessage(main.lang.toMessage("boughtItem", "You just bought %item% for %price%!").replace("%item%", "${item!!.type.name} x$amount").replace("%price%", main.econ!!.format(boughtPrice.toDouble())))
            main.econ!!.withdrawPlayer(Bukkit.getOfflinePlayer(event.player.uniqueId), boughtPrice.toDouble())
            dropItem(event.player, item, amount)
        } else {
            if(hasInventoryArea(chest.inventory, main.currency, boughtPrice)) {
                if(hasItem(chest.inventory, item!!, amount)) {
                    if(delItemInventory(event.player.inventory, main.currency, boughtPrice)) {
                        addItemInventory(chest.snapshotInventory, main.currency, boughtPrice)
                        delItemInventory(chest.snapshotInventory, item, amount)
                        event.player.sendMessage(main.lang.toMessage("boughtItem", "You just bought %item% for %price%!").replace("%item%", "${item.type.name} x$amount").replace("%price%", "$boughtPrice Emerald"))
                        event.player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
                        dropItem(event.player, item, amount)
                        chest.update()
                        return
                    } else {
                        event.player.sendMessage(main.lang.toMessage("emeraldInsufficient", "Your emerald is insufficient."))
                        return
                    }
                } else {
                    event.player.sendMessage(main.lang.toMessage("outOfStock", "This shop does not have it in stock. Please wait until it's refilled or contact us at %player%.").replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(owner)).name!!))
                    return
                }
            } else {
                event.player.sendMessage(main.lang.toMessage("shopNoInventoryBought", "I couldn't bought it because the shop is full. Please wait until the stock is low or contact %player%!").replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(owner)).name!!))
                return
            }
        }
    }
    fun onSignLeftClick(event: PlayerInteractEvent, block: Block, data: WallSign) {
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
        if(!(main.shops.config().contains("${chest!!.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}"))) {
            return
        }
        if(!main.shops.config().getBoolean("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.sell")) {
            event.player.sendMessage(main.lang.toMessage("sellOnly", "This shop is for sell only."))
            return
        }
        val owner = main.shops.config().getString("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.owner")
        if(event.player.uniqueId.toString() == owner) {
            return
        }
        val item = main.shops.config().getItemStack("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.item")
        val amount = main.shops.config().getInt("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.amount")
        val sellPrice = main.shops.config().getInt("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.sellPrice")
        item?.amount = 1
        if(chest.isAdmin()) {
            if(delItemInventory(event.player.inventory, item!!, amount)) {
                event.player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
                if(main.economy) {
                    event.player.sendMessage(main.lang.toMessage("sellItem", "You sold %item% items for %price%!").replace("%item%", "${item.type.name} x$amount").replace("%price%", main.econ!!.format(sellPrice.toDouble())))
                    main.econ!!.depositPlayer(Bukkit.getOfflinePlayer(event.player.uniqueId), (sellPrice.toDouble() / 100) * (100 - main.config.getInt("tax", 0)))
                } else {
                    event.player.sendMessage(main.lang.toMessage("sellItem", "You sold %item% items for %price%!").replace("%item%", "${item.type.name} x$amount").replace("%price%", "$sellPrice Emerald"))
                    dropItem(event.player, main.currency, sellPrice)
                }
                return
            } else {
                event.player.sendMessage(main.lang.toMessage("notEnoughItem", "Not enough items to sell."))
            }
            return
        }
        if(main.economy) {
            if(!hasItem(event.player.inventory, item!!, amount)) {
                event.player.sendMessage(main.lang.toMessage("notEnoughItem", "Not enough items to sell."))
                return
            }
            if(!(main.econ!!.has(Bukkit.getOfflinePlayer(UUID.fromString(owner)) ,sellPrice.toDouble()))) {
                event.player.sendMessage(main.lang.toMessage("notEnoughMoneyOwner", "The shop owner does not have enough money to sell."))
                return
            }
            event.player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
            event.player.sendMessage(main.lang.toMessage("sellItem", "You sold %item% items for %price%!").replace("%item%", "${item.type.name} x$amount").replace("%price%", main.econ!!.format(sellPrice.toDouble())))
            delItemInventory(event.player.inventory, item, amount)
            main.econ!!.depositPlayer(Bukkit.getOfflinePlayer(event.player.uniqueId), sellPrice.toDouble())
            main.econ!!.withdrawPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)), (sellPrice.toDouble()/100)*(100-main.config.getInt("tax", 0)))
        } else {
            if(hasItem(chest.inventory, main.currency, sellPrice)) {
                if(hasInventoryArea(chest.inventory, item!!, amount)) {
                    event.player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
                    event.player.sendMessage(main.lang.toMessage("sellItem", "You sold %item% items for %price%!").replace("%item%", "${item.type.name} x$amount").replace("%price%", "$sellPrice Emerald"))
                    delItemInventory(event.player.inventory, item, amount)
                    delItemInventory(chest.snapshotInventory, main.currency, sellPrice)
                    addItemInventory(chest.snapshotInventory, item, amount)
                    dropItem(event.player, main.currency, sellPrice)
                    chest.update()
                } else {
                    event.player.sendMessage(main.lang.toMessage("shopNoInventorySell", "I couldn't sell it because the shop is full. Please wait until the stock is low or contact %player%!").replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(owner)).name!!))
                    return
                }
            } else {
                event.player.sendMessage(main.lang.toMessage("notFoundEmerald", "Can't sell items because there are no emeralds in the shop. Please wait until the stock is low or contact %player%!").replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(owner)).name!!))
                return
            }
        }
    }
    private fun Config.toMessage(pass: String, default: String = "", prefix: Boolean = true):String {
        return ChatColor.translateAlternateColorCodes('&', (if(prefix) "${main.lang.config().getString("prefix", "&7[&6SHOP&7]&f ")}" else "") + this.config().getString(pass, default))
    }
    private fun Chest.isAdmin(): Boolean {
        return main.shops.config().getBoolean("${this.location.world!!.name}-${this.location.blockX}-${this.location.blockY}-${this.location.blockZ}.admin")
    }
}