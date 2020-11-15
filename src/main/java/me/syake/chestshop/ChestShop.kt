package me.syake.chestshop

import me.syake.realfishing.Config
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Item
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin


class ChestShop : JavaPlugin(), Listener {

    val lang = Config(this, "languages.yml")
    val shops = Config(this, "shops.yml")
    var econ: Economy? = null
    var economy = false
    private val shopProtect = ShopProtect(this)
    private val interactEvent = InteractEvent(this)
    val createShop = CreateShop(this)
    val shopSystem = ShopSystem(this)
    val withdrawItem = WithdrawItem()
    private val deleteShop = DeleteShop(this)

    override fun onEnable() {
        saveDefaultConfig()
        lang.saveDefaultConfig()
        shops.saveDefaultConfig()
        Bukkit.getPluginManager().registerEvents(interactEvent, this)
        Bukkit.getPluginManager().registerEvents(shopProtect, this)
        Bukkit.getPluginManager().registerEvents(deleteShop, this)
        thread.start()
        if(config.getString("mode")=="economy") {
            if(setupEconomy()) {
                economy = true
            } else {
                server.consoleSender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.config().getString("NotFoundVault", "&cIt's currently set to economy mode in the config, but it couldn't start up properly because it couldn't find the Vault. If you plan to use emeralds as your currency, please try emerald mode.")!!))
                server.pluginManager.disablePlugin(this)
                return
            }
        }
        for (i in Bukkit.getWorlds()) {
            for (x in i.entities) {
                if (x.scoreboardTags.contains("ChestShopItemTag")) {
                    if (x is Item) {
                        x.pickupDelay = Integer.MAX_VALUE
                        x.setTicksLived(Integer.MAX_VALUE)
                    }
                }
            }
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private val thread = Thread {
        server.scheduler.scheduleSyncRepeatingTask(this, {
            for (i in Bukkit.getWorlds()) {
                for (x in i.entities) {
                    if (x.scoreboardTags.contains("ChestShopItemTag")) {
                        if (x is Item) {
                            x.pickupDelay = Integer.MAX_VALUE
                            x.setTicksLived(Integer.MAX_VALUE)
                        }
                    }
                }
            }
        }, 0L, 36000L)
    }


    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false
        econ = rsp.provider
        return econ != null
    }

}

    //
    // ここからチェスト保護
    //