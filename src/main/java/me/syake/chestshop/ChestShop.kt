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
    val managementPanel = ManagementPanel(this)

    override fun onEnable() {
        saveDefaultConfig()
        lang.saveDefaultConfig()
        shops.saveDefaultConfig()
        Bukkit.getPluginManager().registerEvents(interactEvent, this)
        Bukkit.getPluginManager().registerEvents(shopProtect, this)
        thread.start()
        if(config.getString("mode")=="economy") {
            if(setupEconomy()) {
                economy = true
            } else {
                server.consoleSender.sendMessage(lang.toMessage("NotFoundVault", "&cI'm currently set to \"Economy\" mode in the config, but I couldn't find a vault or an economy plugin to support the vault and couldn't start it correctly. If you do not plan to install economy plugin, please change to \"Emerald\" mode"))
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

    private fun Config.toMessage(pass: String, default: String = "", prefix: Boolean = true):String {
        return ChatColor.translateAlternateColorCodes('&', (if (prefix) "${lang.config().getString("prefix", "&7[&6SHOP&7]&f ")}" else "") + this.config().getString(pass, default))
    }
}