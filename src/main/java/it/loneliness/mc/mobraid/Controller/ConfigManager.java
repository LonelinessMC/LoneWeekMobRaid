package it.loneliness.mc.mobraid.Controller;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {
    public enum CONFIG_ITEMS {
        CHAT_PREFIX("chatPrefix", "&#FE6847[&#FBB13CLoneWeek MobRaid&#FE6847] "),
        DEBUG("debug", true),
        SCOREBOARD_ID("scoreboardId", "punteggiomobraid"),
        NPC_NAME_SUBSTRING("npcNameSubstring", "Fabbro");
        //WELCOME_MESSAGES("welcomeMessages", List.of("Welcome!", "Hello!"));

        private final String key;
        private final Object defaultValue;

        CONFIG_ITEMS(String key, Object defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public String getKey() {
            return key;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        loadConfig();
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        addMissingDefaults();
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    private void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        addMissingDefaults();
    }

    private void addMissingDefaults() {
        boolean saveNeeded = false;
        for (CONFIG_ITEMS item : CONFIG_ITEMS.values()) {
            if (!config.contains(item.getKey())) {
                config.set(item.getKey(), item.getDefaultValue());
                saveNeeded = true;
            }
        }
        if (saveNeeded) {
            saveConfig();
        }
    }

    // Typized getter methods
    public String getString(CONFIG_ITEMS item) {
        return getConfig().getString(item.getKey(), (String) item.getDefaultValue());
    }

    public int getInt(CONFIG_ITEMS item) {
        return getConfig().getInt(item.getKey(), (Integer) item.getDefaultValue());
    }

    public List<String> getStringList(CONFIG_ITEMS item) {
        return getConfig().getStringList(item.getKey());
    }

    public Boolean getBoolean(CONFIG_ITEMS item){
        return getConfig().getBoolean(item.getKey());
    }

    // Typized setter methods
    public void setString(CONFIG_ITEMS item, String value) {
        config.set(item.getKey(), value);
        saveConfig();
    }

    public void setInt(CONFIG_ITEMS item, int value) {
        config.set(item.getKey(), value);
        saveConfig();
    }

    public void setStringList(CONFIG_ITEMS item, List<String> value) {
        config.set(item.getKey(), value);
        saveConfig();
    }

    public void setBoolean(CONFIG_ITEMS item, Boolean value) {
        config.set(item.getKey(), value);
        saveConfig();
    }
}
