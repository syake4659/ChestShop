package me.syake.realfishing

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.logging.Level

class Config(private val plugin: Plugin, private val fileName: String) {
    private var configuration: FileConfiguration? = null
    private var configFile: File? = null

    init {
        configFile = File(plugin.dataFolder, this.fileName)
    }

    fun saveDefaultConfig() {
        if (!configFile!!.exists()) {
            plugin.saveResource(fileName, false)
        }
    }

    fun reloadConfig() {
        configuration = YamlConfiguration.loadConfiguration(configFile!!)
        val defaultConfigStream = plugin.getResource(fileName) ?: return
        configuration!!.setDefaults(YamlConfiguration.loadConfiguration(InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8)))
    }

    fun config(): FileConfiguration {
        if(configuration == null) {
            reloadConfig()
        }
        return configuration!!
    }

    fun saveConfig() {
        if (configuration == null) {
            return
        }
        try {
            config().save(configFile!!)
        } catch (e: IOException) {
            plugin.logger.log(Level.SEVERE, "Could not save config to $configFile", e)
        }
    }
}