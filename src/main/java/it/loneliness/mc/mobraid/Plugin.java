package it.loneliness.mc.mobraid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import it.loneliness.mc.mobraid.Controller.CommandHandler;
import it.loneliness.mc.mobraid.Controller.ConfigManager;
import it.loneliness.mc.mobraid.Controller.TaskScheduler;
import it.loneliness.mc.mobraid.Model.LogHandler;
import it.loneliness.mc.mobraid.Custom.RaidsManager;

public class Plugin extends JavaPlugin{
    LogHandler logger;
    CommandHandler commandHandler;
    TaskScheduler taskScheduler;
    ConfigManager configManager;
    private RaidsManager manager;
    boolean enabled;
    
    @Override
    public void onEnable() {
        logger = LogHandler.getInstance(getLogger());
        logger.info("Enabling the plugin");

        configManager = new ConfigManager(this);

        if(configManager.getBoolean(ConfigManager.CONFIG_ITEMS.DEBUG)){
            logger.setDebug(true);
        }
        
        manager = new RaidsManager(this, logger);
        this.getServer().getPluginManager().registerEvents(manager, this);

        try {
            //Make sure this is alligned with the plugin.yml, the first in the list is used for the permissions
            List<String> prefixes = new ArrayList<>(Arrays.asList("mobraid", "mr"));
            this.commandHandler = new CommandHandler(this, prefixes, manager);
            for(String prefix : prefixes){
                this.getCommand(prefix).setExecutor(commandHandler);
                this.getCommand(prefix).setTabCompleter(commandHandler);
            }
        } catch(NullPointerException e){
            logger.severe("Ensure you're defining the same comands both in plugin.yml as well as in Plugin.java");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.taskScheduler = new TaskScheduler(this, 10, manager);
        taskScheduler.start();
    }

    @Override
    public void onDisable() {
        logger.info("Disabling the plugin");
        manager.onDisable();
        if(taskScheduler != null && taskScheduler.isRunning()){
            taskScheduler.stop();
        }
    }

    public TaskScheduler getTaskScheduler(){
        return this.taskScheduler;
    }

    public ConfigManager getConfigManager(){
        return this.configManager;
    }
}