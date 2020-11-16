package me.syake.chestshop

import me.syake.realfishing.Config
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class DeleteShop(private val main: ChestShop) {

    fun delete(block: Block, player: Player) {
        val sign = block.state as Sign
        if(sign.getLine(0)=="§e§lSHOP"||sign.getLine(0)=="§c§lAdmin SHOP") {
            val admin = sign.getLine(0)=="§c§lAdmin SHOP"
            val data = sign.blockData as WallSign
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
            val owner = main.shops.config().getString("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}.owner")
            if(admin) {
                if(!player.hasPermission("chestshop.adminshop")) {
                    player.sendMessage(main.lang.toMessage("DeleteIsOwnerOnly", "The shop can only be destroyed by the player owner!"))
                    return
                }
            } else if(owner != player.uniqueId.toString()) {
                player.sendMessage(main.lang.toMessage("DeleteIsOwnerOnly", "The shop can only be destroyed by the player owner!"))
                return
            }
            for(i in block.world.entities) {
                if(chest.location.add(0.5, 1.0 , 0.5).distance(i.location)<=0.2) {
                    if(i.scoreboardTags.contains("ChestShopItemTag")) {
                        chest.customName = null
                        chest.update()
                        chest.block.breakNaturally()
                        i.remove()
                        player.sendMessage(main.lang.toMessage("DeleteShop", "Deleted a shop!"))
                        main.shops.config().set("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}", null)
                        main.shops.saveConfig()
                        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
                        return
                    }
                }
            }
        }
    }
    private fun Config.toMessage(pass: String, default: String = "", prefix: Boolean = true):String {
        return ChatColor.translateAlternateColorCodes('&', (if(prefix) "${main.lang.config().getString("prefix", "&7[&6SHOP&7]&f ")}" else "") + this.config().getString(pass, default))
    }
}