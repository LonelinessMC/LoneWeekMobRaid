package it.loneliness.mc.mobraid.Controller;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import it.loneliness.mc.mobraid.Model.PeriodicManagerRunner;

public class TaskScheduler {
    private final JavaPlugin plugin;
    private final long periodSeconds;
    private int taskId = -1;
    private PeriodicManagerRunner manager;

    public TaskScheduler(JavaPlugin plugin, long periodSeconds, PeriodicManagerRunner manager) {
        this.plugin = plugin;
        this.periodSeconds = periodSeconds;
        this.manager = manager;
    }

    public void start() {
        if (taskId == -1) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::periodicRunner, 0, periodSeconds*20);
        }
    }

    public void periodicRunner(){
        new BukkitRunnable() {
            @Override
            public void run() {
                manager.periodicRunner();
            }
        }.runTaskAsynchronously(plugin);
        
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public boolean isRunning() {
        return taskId != -1;
    }
}
