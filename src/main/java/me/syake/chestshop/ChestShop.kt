package me.syake.chestshop

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Item
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
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
    var currency = ItemStack(Material.EMERALD)
    private val deleteShop = DeleteShop(this)

    override fun onEnable() {
        Metrics(this, 9440)
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
                server.consoleSender.sendMessage(lang.toMessage("NotFoundVault", "&cI'm currently set to \"Economy\" mode in the config, but I couldn't find a vault or an economy plugin to support the vault and couldn't start it correctly. If you do not plan to install economy plugin, please change to \"Emerald\" mode"))
                server.pluginManager.disablePlugin(this)
                return
            }
        } else {
            val temp = Material.getMaterial(config.getString("currency")!!.toUpperCase())
            if(temp!=null) {
                currency = ItemStack(temp)
            } else {
                server.consoleSender.sendMessage(lang.toMessage("notFoundItem", "The currency was set to emerald because the item name　%name% was not found.").replace("%name%", config.getString("currency")!!))
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

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(command.name == "chestshop") {
            if(args.isEmpty())  {
                sender.sendMessage(lang.toMessage("help", "&6/chestshop reload&8: &7Reload the config file.", false))
                return true
            }
            when(args[0]) {
                "reload" -> {
                    lang.saveConfig()
                    saveConfig()
                    shops.saveConfig()
                    lang.reloadConfig()
                    shops.reloadConfig()
                    reloadConfig()
                    if(config.getString("mode")=="economy") {
                        if(setupEconomy()) {
                            economy = true
                        } else {
                            server.consoleSender.sendMessage(lang.toMessage("NotFoundVault", "&cI'm currently set to \"Economy\" mode in the config, but I couldn't find a vault or an economy plugin to support the vault and couldn't start it correctly. If you do not plan to install economy plugin, please change to \"Emerald\" mode"))
                            server.pluginManager.disablePlugin(this)
                        }
                    } else {
                        val temp = Material.getMaterial(config.getString("currency")!!.toUpperCase())
                        if(temp!=null) {
                            currency = ItemStack(temp)
                        } else {
                            server.consoleSender.sendMessage(lang.toMessage("notFoundItem", "The currency was set to emerald because the item name　%name% was not found.").replace("%name%", config.getString("currency")!!))
                        }
                    }
                }
                else -> sender.sendMessage(lang.toMessage("help", "&6/chestshop reload&8: &7Reload the config file.", false))
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        val commands = mutableListOf("reload")
        if(command.name == "chestshop") {
            if(args.isEmpty())  {
                return commands
            }
            if(args.size==1) {
                val cmds = mutableListOf<String>()
                for(cmd in commands) {
                    if(args[0].startsWith(cmd)) {
                        cmds.add(cmd)
                    }
                }
                return cmds
            }
        }
        return mutableListOf()
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