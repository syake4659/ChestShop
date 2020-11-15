package me.syake.chestshop

import me.syake.realfishing.Config
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEvent

class ShopProtect(private val main: ChestShop): Listener {
    @EventHandler
    fun onChestClick(event: PlayerInteractEvent) {
        if(event.action == Action.RIGHT_CLICK_BLOCK) {
            if(event.clickedBlock!!.type == Material.CHEST) {
                if(main.shops.config().contains("${event.clickedBlock!!.location.world?.name}-${event.clickedBlock!!.location.blockX}-${event.clickedBlock!!.location.blockY}-${event.clickedBlock!!.location.blockZ}")) {
                    val admin = main.shops.config().getBoolean("${event.clickedBlock!!.location.world?.name}-${event.clickedBlock!!.location.blockX}-${event.clickedBlock!!.location.blockY}-${event.clickedBlock!!.location.blockZ}.admin")
                    if(admin) {
                        event.isCancelled = true
                        if(event.player.hasPermission("chestshop.adminshop")) {
                            event.player.sendMessage(main.lang.toMessage("adminShopOpenChest", "The Admin Shop does not need to be replenished with items."))
                            return
                        }
                    }
                    val owner = main.shops.config().getString("${event.clickedBlock!!.location.world?.name}-${event.clickedBlock!!.location.blockX}-${event.clickedBlock!!.location.blockY}-${event.clickedBlock!!.location.blockZ}.owner")
                    if(owner!=event.player.uniqueId.toString()) {
                        event.isCancelled = true
                    }
                }
            }
        }
    }
    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val block = event.block.location.add(0.0, -1.0, 0.0).block
        if(block.type== Material.CHEST) {
            val chest = block.state as Chest
            if(chest.customName==null) {
                return
            }
            if(chest.customName?.startsWith(main.lang.toMessage("Shop", "&eShop", false))!!) {
                event.isCancelled = true
            }
        }
    }
    @EventHandler
    fun onBlockFromTo(event: BlockFromToEvent) {
        val block = event.toBlock.location.add(0.0, -1.0, 0.0).block
        if(block.type== Material.CHEST) {
            val chest = block.state as Chest
            if(chest.customName==null) {
                return
            }
            if(chest.customName?.startsWith(main.lang.toMessage("Shop", "&eShop", false))!!) {
                event.isCancelled = true
            }
        }
    }
    @EventHandler
    fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent) {
        val block = event.block.location.add(0.0, -1.0, 0.0).block
        val blockTwo = event.block.location.block
        if(block.type== Material.CHEST) {
            val chest = block.state as Chest
            if(chest.customName==null) {
                return
            }
            if(chest.customName?.startsWith(main.lang.toMessage("Shop", "&eShop", false))!!) {
                event.isCancelled = true
            }
        } else if(blockTwo.type== Material.CHEST) {
            val chest = blockTwo.state as Chest
            if(chest.customName==null) {
                return
            }
            if(chest.customName?.startsWith(main.lang.toMessage("Shop", "&eShop", false))!!) {
                event.isCancelled = true
            }
        }
    }
    @EventHandler
    fun onPistonMove(event: BlockPistonExtendEvent) {
        when(event.direction) {
            BlockFace.DOWN -> {
                for(i in listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0)) {
                    val loc = event.block.location
                    loc.add(0.0, -i, 0.0)
                    if(loc.block.type== Material.AIR||loc.block.type== Material.VOID_AIR||loc.block.type== Material.CAVE_AIR) {
                        print("test")
                        print(loc.add(0.0, -1.0, 0.0).block.type)
                        if(loc.block.type == Material.CHEST) {
                            val chest = loc.block.state as Chest
                            if (chest.customName == null) {
                                break
                            }
                            if (chest.customName?.startsWith(main.lang.toMessage("Shop", "&eShop", false))!!) {
                                event.isCancelled = true
                                return
                            }
                        }
                        break
                    }
                }
            }
            BlockFace.SOUTH -> {
                for(i in listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0)) {
                    val loc = event.block.location
                    loc.add(0.0, 0.0, i)
                    if(loc.block.type== Material.AIR||loc.block.type== Material.VOID_AIR||loc.block.type== Material.CAVE_AIR) {
                        print(loc.add(0.0, -1.0, 0.0).block.type)
                        if(loc.block.type == Material.CHEST) {
                            val chest = loc.block.state as Chest
                            if (chest.customName == null) {
                                break
                            }
                            if (chest.customName?.startsWith(main.lang.toMessage("Shop", "&eShop", false))!!) {
                                event.isCancelled = true
                                return
                            }
                        }
                        break
                    }
                }
            }
            BlockFace.NORTH -> {
                for(i in listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0)) {
                    val loc = event.block.location
                    loc.add(0.0, 0.0, -i)
                    if(loc.block.type== Material.AIR||loc.block.type== Material.VOID_AIR||loc.block.type== Material.CAVE_AIR) {
                        print(loc.add(0.0, -1.0, 0.0).block.type)
                        if(loc.block.type == Material.CHEST) {
                            val chest = loc.block.state as Chest
                            if (chest.customName == null) {
                                break
                            }
                            if (chest.customName?.startsWith(main.lang.toMessage("Shop", "&eShop", false))!!) {
                                event.isCancelled = true
                                return
                            }
                        }
                        break
                    }
                }
            }
            BlockFace.WEST -> {
                for(i in listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0)) {
                    val loc = event.block.location
                    loc.add(-i, 0.0, 0.0)
                    if(loc.block.type== Material.AIR||loc.block.type== Material.VOID_AIR||loc.block.type== Material.CAVE_AIR) {
                        print(loc.add(0.0, -1.0, 0.0).block.type)
                        if(loc.block.type == Material.CHEST) {
                            val chest = loc.block.state as Chest
                            if (chest.customName == null) {
                                break
                            }
                            if (chest.customName?.startsWith(main.lang.toMessage("Shop", "&eShop", false))!!) {
                                event.isCancelled = true
                                return
                            }
                        }
                        break
                    }
                }
            }
            BlockFace.EAST -> {
                for(i in listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0)) {
                    val loc = event.block.location
                    loc.add(i, 0.0, 0.0)
                    if(loc.block.type== Material.AIR||loc.block.type== Material.VOID_AIR||loc.block.type== Material.CAVE_AIR) {
                        print(loc.add(0.0, -1.0, 0.0).block.type)
                        if(loc.block.type == Material.CHEST) {
                            val chest = loc.block.state as Chest
                            if (chest.customName == null) {
                                break
                            }
                            if (chest.customName?.startsWith(main.lang.toMessage("Shop", "&eShop", false))!!) {
                                event.isCancelled = true
                                return
                            }
                        }
                        break
                    }
                }
            }
            else -> {}
        }
    }
    @EventHandler
    fun onPlayerFish(event: PlayerFishEvent) {
        if(event.caught!!.scoreboardTags.contains("ChestShopItemTag")) {
            event.hook.remove()
            event.isCancelled = true
        }
    }
    private fun Config.toMessage(pass: String, default: String = "", prefix: Boolean = true):String {
        return ChatColor.translateAlternateColorCodes('&', (if(prefix) "${main.lang.config().getString("prefix", "&7[&6SHOP&7]&r")} " else "") + this.config().getString(pass, default))
    }
}