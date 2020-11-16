package me.syake.chestshop

import org.bukkit.Location
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import java.lang.NullPointerException

class ManagementPanel(val main: ChestShop) {
    fun open(player: Player, chest: Chest) {
        if(main.config.contains("${chest.location.world?.name}-${chest.location.blockX}-${chest.location.blockY}-${chest.location.blockZ}")) {
            
        } else {
            throw NullPointerException("Chest not found")
        }
    }
}