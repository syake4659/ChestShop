package me.syake.chestshop

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class DeleteShop(private val main: ChestShop): Listener {

    @EventHandler
    fun delete(event: BlockBreakEvent) {
        val block = event.block
        if (block.type == Material.CHEST) {
            if(!(main.shops.config().contains("${block.location.world?.name}-${block.location.blockX}-${block.location.blockY}-${block.location.blockZ}"))) {
                return
            }
            val chest = block.state as Chest
            val admin = main.shops.config().getBoolean("${block.location.world?.name}-${block.location.blockX}-${block.location.blockY}-${block.location.blockZ}.admin")
            val owner = main.shops.config().getString("${block.location.world?.name}-${block.location.blockX}-${block.location.blockY}-${block.location.blockZ}.owner")
            if(admin) {
                if(!event.player.hasPermission("chestshop.adminshop")) {
                    event.player.sendMessage(main.lang.toMessage("DeleteIsOwnerOnly", "The shop can only be destroyed by the player owner!"))
                    event.isCancelled = true
                    return
                }
            } else if(owner != event.player.uniqueId.toString()) {
                event.player.sendMessage(main.lang.toMessage("DeleteIsOwnerOnly", "The shop can only be destroyed by the player owner!"))
                event.isCancelled = true
                return
            }
            for(i in event.block.world.entities) {
                if(block.location.add(0.5, 1.0 , 0.5).distance(i.location)<=0.2) {
                    if(i.scoreboardTags.contains("ChestShopItemTag")) {
                        chest.customName = null
                        chest.update()
                        chest.block.breakNaturally()
                        i.remove()
                        event.player.sendMessage(main.lang.toMessage("DeleteShop", "Deleted a shop!"))
                        main.shops.config().set("${block.location.world?.name}-${block.location.blockX}-${block.location.blockY}-${block.location.blockZ}", null)
                        main.shops.saveConfig()
                        event.player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
                        return
                    }
                }
            }
            return
        } else if (block.blockData is WallSign) {
            val sign = block.state as Sign
            val data = sign.blockData as WallSign
            var chest: Chest? = null
            when (data.facing) {
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
            if (!(main.shops.config().contains("${chest!!.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}"))) {
                return
            }
            val admin = chest.isAdmin()
            val owner = main.shops.config().getString("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.owner")
            if (admin) {
                if (!event.player.hasPermission("chestshop.adminshop")) {
                    event.player.sendMessage(main.lang.toMessage("DeleteIsOwnerOnly", "The shop can only be destroyed by the player owner!"))
                    return
                }
            } else if (owner != event.player.uniqueId.toString() && event.player.hasPermission("chestshop.admin")) {
                event.player.sendMessage(main.lang.toMessage("DeleteIsOwnerOnly", "The shop can only be destroyed by the player owner!"))
                return
            }
            for (i in block.world.entities) {
                if (chest.location.add(0.5, 1.0, 0.5).distance(i.location) <= 0.2) {
                    if (i.scoreboardTags.contains("ChestShopItemTag")) {
                        chest.customName = null
                        chest.update()
                        chest.block.breakNaturally()
                        i.remove()
                        event.player.sendMessage(main.lang.toMessage("DeleteShop", "Deleted a shop!"))
                        main.shops.config().set("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}", null)
                        main.shops.saveConfig()
                        event.player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
                        return
                    }
                }
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