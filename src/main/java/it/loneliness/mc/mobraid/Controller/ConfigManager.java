package it.loneliness.mc.mobraid.Controller;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import it.loneliness.mc.mobraid.Custom.RaidRoundConfig;
import it.loneliness.mc.mobraid.Custom.RaidRoundEntityConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    public enum CONFIG_ITEMS {
        CHAT_PREFIX("chatPrefix", "&#FE6847[&#FBB13CLoneWeek MobRaid&#FE6847] "),
        SCOREBOARD_ID("scoreboardId", "punteggiomobraid"),
        NPC_NAME_SUBSTRING("npcNameSubstring", "Fabbro"),
        ARENA_RADIUS("arenaRadius", 30),
        FRIENDS_RADIUS("friendsRadius", 30),
        INFRINGMENT_TIMEOUT_SECONDS("infringmentTimeoutSeconds", 10),
        SUMMON_RAID_ITEM_TYPE("summonRaidItemType", "zombie_head"),
        SUMMON_RAID_ITEM_NAME("summonRaidItemName", "RAID generator"),
        SUMMON_RAID_ITEM_LORE("summonRaidItemLore", List.of("Piazza questo item per iniziare un raid!", "")),
        RAID_ROUND_CONFIGS("raidRoundConfigs", getDefaultRaidRoundConfigs()),
        DEBUG("debug", true);

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

        private static Map<String, Object> getDefaultRaidRoundEntity(String type, String name, int howMany, boolean onePerPlayer){
            Map<String, Object> entityConfig = new HashMap<>();
            entityConfig.put("type", type);
            entityConfig.put("name", name);
            entityConfig.put("howMany", howMany);
            entityConfig.put("onePerPlayer", onePerPlayer);
            return entityConfig;
        }

        private static List<Map<String, Object>> getDefaultRaidRoundConfigs() {
            List<Map<String, Object>> configs = new ArrayList<>();

            List<Map<String, Object>> mobsToSpawn1 = new ArrayList<>();
            mobsToSpawn1.add(getDefaultRaidRoundEntity("ZOMBIE", "Zombie 1st RAID", 1, true));
            mobsToSpawn1.add(getDefaultRaidRoundEntity("SKELETON", "Skeleton 1st RAID", 3, false));

            Map<String, Object> config1 = new HashMap<>();
            config1.put("secondsToComplete", 100);
            config1.put("mobsToSpawn", mobsToSpawn1);
            configs.add(config1);

            List<Map<String, Object>> mobsToSpawn2 = new ArrayList<>();
            mobsToSpawn2.add(getDefaultRaidRoundEntity("ZOMBIE", "Zombie 2nd RAID", 3, false));

            Map<String, Object> config2 = new HashMap<>();
            config2.put("secondsToComplete", 10);
            config2.put("mobsToSpawn", mobsToSpawn2);
            configs.add(config2);

            return configs;
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

    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> getMapList(CONFIG_ITEMS item) {
        return (List<Map<String,Object>>) getConfig().getList(item.getKey(), (List<Map<String,Object>>) item.getDefaultValue());
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
    
    // Method to load RaidRoundConfig from config.yml
    @SuppressWarnings("unchecked")
    public List<RaidRoundConfig> getRaidRoundConfigs() {
        List<RaidRoundConfig> configs = new ArrayList<>();

        List<Map<String, Object>> rawConfigs = getMapList(CONFIG_ITEMS.RAID_ROUND_CONFIGS);
        for(Map<String, Object> rawConfig : rawConfigs){
            int secondsToComplete = (Integer) rawConfig.get("secondsToComplete");
            List<RaidRoundEntityConfig> entities = new ArrayList<>();
            if(rawConfig.get("mobsToSpawn") != null){
                for(Map<String, Object> mobToSpawn : (List<Map<String, Object>>) rawConfig.get("mobsToSpawn")){
                    EntityType type = EntityType.valueOf((String) mobToSpawn.get("type"));
                    String name = (String) mobToSpawn.get("name");
                    int howMany = (Integer) mobToSpawn.get("howMany");
                    boolean onePerPlayer = (Boolean) mobToSpawn.get("onePerPlayer");
                    entities.add(new RaidRoundEntityConfig(type, name, howMany, onePerPlayer));
                }
            }
            configs.add(new RaidRoundConfig(entities, secondsToComplete));
        }

        return configs;
    }

}
