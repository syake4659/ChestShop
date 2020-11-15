package me.syake.chestshop

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class WithdrawItem {
    fun withdraw(player: Player, item: ItemStack, amount: Int): Boolean {
        val cloneItem = item.clone()
        cloneItem.amount = 1
        var remainingAmount = amount
        val items = mutableListOf<ItemStack>()
        var invAmount = 0
        for(i in player.inventory.contents) {
            if(i==null) {
                continue
            }
            val invItem = i.clone()
            invItem.amount = 1
            if(invItem==cloneItem) {
                invAmount += i.amount
                items.add(i)
            }
        }
        if(amount==0) {
            return true
        }
        if(invAmount<amount) {
            return false
        }
        for(i in items) {
            when {
                remainingAmount==i.amount -> {
                    i.amount = 0
                    return true
                }
                remainingAmount > i.amount -> {
                    remainingAmount -= i.amount
                    i.amount = 0
                }
                remainingAmount < i.amount -> {
                    i.amount -= remainingAmount
                    return true
                }
            }
        }
        return false
    }
}