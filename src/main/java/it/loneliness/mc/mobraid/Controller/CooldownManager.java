package it.loneliness.mc.mobraid.Controller;

import org.bukkit.entity.Player;
import java.util.HashMap;

public class CooldownManager {
    private HashMap<Player, Long> cooldowns = new HashMap<>();
    private long cooldownTimeMillis;

    public CooldownManager(long cooldownTimeMinutes) {
        this.cooldownTimeMillis = cooldownTimeMinutes * 60 * 1000; // Convert minutes to milliseconds
    }

    public boolean isOnCooldown(Player player) {
        if (cooldowns.containsKey(player)) {
            long lastExecutionTime = cooldowns.get(player);
            long currentTime = System.currentTimeMillis();
            return (currentTime - lastExecutionTime) < cooldownTimeMillis;
        }
        return false; // Not on cooldown if no previous record found
    }

    public void updateCooldown(Player player) {
        cooldowns.put(player, System.currentTimeMillis());
    }
}
