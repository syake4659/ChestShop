package me.syake.chestshop

import me.syake.realfishing.Config
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class CreateShop(private val main: ChestShop): Listener {
    fun onSignRightClick(event: PlayerInteractEvent) {
        val block = event.clickedBlock?:return
        val blockState = block.state as Sign
        val data = blockState.blockData as WallSign
        if(blockState.getLine(0).equals("[shop]", ignoreCase = true)||blockState.getLine(0).equals("[adminshop]", ignoreCase = true)) {
            val admin = blockState.getLine(0).equals("[adminshop]", ignoreCase = true)
            if(admin) {
                if(!event.player.hasPermission("chestshop.adminshop")) {
                    return
                }
            }
            if(main.economy) {
                if(!(main.econ!!.has(Bukkit.getOfflinePlayer(event.player.uniqueId) ,main.config.getInt("creationCost").toDouble()))) {
                    event.player.sendMessage(main.lang.toMessage("NotEnoughCost", "The shop creation cost is not enough."))
                    return
                }
                main.econ!!.withdrawPlayer(Bukkit.getOfflinePlayer(event.player.uniqueId), main.config.getInt("creationCost").toDouble())
            } else {
                if(!main.withdrawItem.withdraw(event.player, ItemStack(Material.EMERALD), main.config.getInt("creationCost"))) {
                    event.player.sendMessage(main.lang.toMessage("NotEnoughCost", "The shop creation cost is not enough."))
                    return
                }
            }
            val itemAmount = blockState.getLine(1).toIntOrNull().run {
                if(this!=null) {
                    if(this<1) {
                        event.player.sendMessage(main.lang.toMessage("MinItemAmount", "The number of items must be at least 1."))
                        return
                    }
                    this
                } else {
                    event.player.sendMessage(main.lang.toMessage("ItemAmountNumberOnly", "Please note that the item Amount must be entered as a number"))
                    return
                }
            }
            val sellPrice = blockState.getLine(3).toIntOrNull().run {
                if(this!=null) {
                    if(this<1) {
                        event.player.sendMessage(main.lang.toMessage("MinSellPrice", "The sell price should be set at 0 or more."))
                        return
                    }
                    this
                } else {
                    -1
                }
            }
            val buyPrice = blockState.getLine(2).toIntOrNull().run {
                if(this!=null) {
                    if(this<1) {
                        event.player.sendMessage(main.lang.toMessage("MinBuyPrice", "The buy price should be set at 1 or more."))
                        return
                    }
                    this
                } else {
                    -1
                }
            }
            if(buyPrice==-1&&sellPrice==-1) {
                event.player.sendMessage(main.lang.toMessage("NotSetPrice", "To create a shop, you'll need to set a price for a sell or buy, or both!"))
                return
            }
            if(event.player.inventory.itemInMainHand.type == Material.AIR) {
                event.player.sendMessage(main.lang.toMessage("NotSetItem", "To create a shop you need to hold the item you want to sell in your hand and right click on the sign"))
                return
            }
            val item = event.player.inventory.itemInMainHand.clone()
            item.amount = 1
            val chest: Chest
            val face: BlockFace
            when (data.facing) {
                BlockFace.EAST -> {
                    if (event.clickedBlock!!.location.add(-1.0, 0.0, 0.0).block.type == Material.CHEST || event.clickedBlock!!.location.add(-1.0, 0.0, 0.0).block.type == Material.TRAPPED_CHEST) {
                        chest = event.clickedBlock!!.location.add(-1.0, 0.0, 0.0).block.state as Chest
                        face = BlockFace.EAST
                    } else {
                        return
                    }
                }
                BlockFace.SOUTH -> {
                    if (event.clickedBlock!!.location.add(0.0, 0.0, -1.0).block.type == Material.CHEST || event.clickedBlock!!.location.add(0.0, 0.0, -1.0).block.type == Material.TRAPPED_CHEST) {
                        chest = event.clickedBlock!!.location.add(0.0, 0.0, -1.0).block.state as Chest
                        face = BlockFace.SOUTH
                    } else {
                        return
                    }
                }
                BlockFace.WEST -> {
                    if (event.clickedBlock!!.location.add(1.0, 0.0, 0.0).block.type == Material.CHEST || event.clickedBlock!!.location.add(1.0, 0.0, 0.0).block.type == Material.TRAPPED_CHEST) {
                        chest = event.clickedBlock!!.location.add(1.0, 0.0, 0.0).block.state as Chest
                        face = BlockFace.WEST
                    } else {
                        return
                    }
                }
                BlockFace.NORTH -> {
                    if (event.clickedBlock!!.location.add(0.0, 0.0, 1.0).block.type == Material.CHEST || event.clickedBlock!!.location.add(0.0, 0.0, 1.0).block.type == Material.TRAPPED_CHEST) {
                        chest = event.clickedBlock!!.location.add(0.0, 0.0, 1.0).block.state as Chest
                        face = BlockFace.NORTH
                    } else {
                        return
                    }
                }
                else -> {
                    return
                }
            }
            if (chest.location.add(0.0, 1.0, 0.0).block.type != Material.AIR) {
                event.player.sendMessage(main.lang.toMessage("ChestTop", "Can't create a shop because there is a block on one of the chests"))
                return
            }
            val chestData = chest.blockData as org.bukkit.block.data.type.Chest
            if(chestData.facing != face) {
                event.player.sendMessage(main.lang.toMessage("SignNeedFront", "The sign needs to be place on the front of the chest!"))
                return
            }
            chest.apply {
                customName = "§eShopChest - x${chest.location.blockX} y${chest.location.blockY} z${chest.location.blockZ}"
                update()
            }
            event.player.world.dropItem(chest.location.add(0.5, 1.2, 0.5), item).apply {
                velocity = Vector(0.0, 0.1, 0.0)
                pickupDelay = Integer.MAX_VALUE
                isCustomNameVisible = true
                addScoreboardTag("ChestShopItemTag")
                customName = if (item.itemMeta!!.hasDisplayName()) "${item.itemMeta!!.displayName} ×${itemAmount}" else "${item.type.name} ×${itemAmount}"
                isInvulnerable = true
                ticksLived = Integer.MAX_VALUE
            }
            main.shops.config().set("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.item", item)
            main.shops.config().set("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.price", buyPrice)
            main.shops.config().set("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.sellPrice", sellPrice)
            main.shops.config().set("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.amount", itemAmount)
            main.shops.config().set("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.buy", buyPrice >= 0)
            main.shops.config().set("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.sell", sellPrice >= 0)
            main.shops.config().set("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.admin", admin)
            main.shops.config().set("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.owner", event.player.uniqueId.toString())
            event.player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
            event.player.sendMessage(main.lang.toMessage("CreateShop", "Created a shop!"))
            main.shops.saveConfig()
            blockState.setLine(1, if (item.itemMeta!!.hasDisplayName()) "${item.itemMeta!!.displayName} ×${itemAmount}" else "${item.type.name} ×${itemAmount}")
            blockState.setLine(0, if(admin) "§c§lAdmin SHOP" else "§e§lSHOP")
            if (buyPrice >= 0) {
                blockState.setLine(2, "B: §r${buyPrice}")
                if (sellPrice >= 0) {
                    blockState.setLine(2, "B §r${buyPrice} : §r${sellPrice} S")
                }
            } else {
                blockState.setLine(2, "S §r${sellPrice}")
            }
            blockState.setLine(3, if(admin) "§lAdminister" else "§l${event.player.name}")
            blockState.update()
        } else {
            main.shopSystem.onSignRightClick(event, block, blockState, data)
            return
        }
    }
    private fun Config.toMessage(pass: String, default: String = "", prefix: Boolean = true):String {
        return ChatColor.translateAlternateColorCodes('&', (if(prefix) "${main.lang.config().getString("prefix", "&7[&6SHOP&7]&r")} " else "") + this.config().getString(pass, default))
    }
}