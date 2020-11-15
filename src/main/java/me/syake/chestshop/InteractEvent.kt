package me.syake.chestshop

import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class InteractEvent(private val main: ChestShop): Listener {
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if(event.action == Action.RIGHT_CLICK_BLOCK && event.clickedBlock?.blockData is WallSign) {
            main.createShop.onSignRightClick(event)
        } else if(event.action == Action.LEFT_CLICK_BLOCK && event.clickedBlock?.blockData is WallSign) {
            val block = event.clickedBlock?:return
            val blockState = block.state as Sign
            val data = blockState.blockData as WallSign
            main.shopSystem.onSignLeftClick(event, block, blockState, data)
        }
    }
}