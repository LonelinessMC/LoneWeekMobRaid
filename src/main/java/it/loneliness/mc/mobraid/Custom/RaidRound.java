package it.loneliness.mc.mobraid.Custom;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import it.loneliness.mc.mobraid.Plugin;
import it.loneliness.mc.mobraid.Controller.Announcement;
import it.loneliness.mc.mobraid.Controller.Metadata;

public class RaidRound {
    public static enum STATUS {
        CREATED,
        STARTED,
        FINISHED
    };

    public static String METADATA_RAID_PLAYER_OWNER = "RAID_PLAYER_OWNER";

    private Plugin plugin;
    private Announcement announcement;
    LocalDateTime startedAt;
    STATUS state;
    private int secondsToComplete;
    private Raid raid;
    private List<LivingEntity> livingEntities;
    private boolean isWon;

    RaidRound(Plugin plugin, Raid raid, int secondsToComplete){
        this.plugin = plugin;
        this.announcement = Announcement.getInstance(plugin);
        this.startedAt = null;
        this.state = STATUS.CREATED;
        this.secondsToComplete = secondsToComplete;
        this.raid = raid;
        this.isWon = true;
    }

    boolean start(){
        if(state != STATUS.CREATED){
            return false;
        }

        this.state = STATUS.STARTED;
        this.startedAt = LocalDateTime.now();
        this.livingEntities = new ArrayList<LivingEntity>();

        EntityType entityTypeToSpawn = EntityType.valueOf("ZOMBIE");

        List<Player> players = this.raid.getPlayers();

        for (Player player : players) {
            LivingEntity entity = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), entityTypeToSpawn);
            entity.setCustomName("Raid Zombie");
            entity.setCustomNameVisible(true);
            Metadata.setMetadata(plugin, entity, METADATA_RAID_PLAYER_OWNER, this.raid.getPlayerOwner().getName());
            livingEntities.add(entity);
            if (entity instanceof Mob) {
                ((Mob) entity).setTarget(player);
            }
        }

        //TODO
        announcement.sendPrivateMessage(players, "Il raid sta iniziando per "+String.join(", ", players.stream().map(p -> p.getName()).toList())+".");

        return true;
    }

    boolean isFinished(){
        if(this.state == STATUS.FINISHED)
            return true;

        if(this.isExpired()){
            this.failRound();
            return true;
        }

        this.livingEntities = this.livingEntities.stream().filter(e -> e.isValid()).toList();
        
        if(this.livingEntities.size() == 0){
            this.state = STATUS.FINISHED;
            this.isWon = true;
            return true;
        }

        return false;

    }

    boolean isExpired(){
        LocalDateTime endTime = startedAt.plusSeconds(secondsToComplete);
        LocalDateTime currentTime = LocalDateTime.now();
        return currentTime.isAfter(endTime);
    }

    public boolean isWon() {
        return isWon;
    }

    public void onDisable() {
        if(Bukkit.isPrimaryThread()){
            removeAllEntities();
        } else {
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    removeAllEntities();
                }
                
            });
        }
        
    }

    private void removeAllEntities(){
        this.livingEntities.forEach(le -> le.remove());
    }

    public void failRound() {
        this.isWon = false;
        this.state = STATUS.FINISHED;
        this.onDisable();
        return;
    }

}
