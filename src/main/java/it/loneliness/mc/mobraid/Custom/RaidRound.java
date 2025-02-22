package it.loneliness.mc.mobraid.Custom;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import it.loneliness.mc.mobraid.Plugin;
import it.loneliness.mc.mobraid.Controller.Announcement;
import it.loneliness.mc.mobraid.Controller.Metadata;
import it.loneliness.mc.mobraid.Controller.ConfigManager.CONFIG_ITEMS;

public class RaidRound {
    private static final Random RANDOM = new Random();

    public static Location getRandomLocationWithinRadius(Location origin, double radius) {
        // Generate a random angle in radians
        double angle = RANDOM.nextDouble() * 2 * Math.PI;

        // Generate a random distance from the origin, ensuring it is within the radius
        double distance = RANDOM.nextDouble() * radius;

        // Calculate the new X and Z offsets
        double xOffset = distance * Math.cos(angle);
        double zOffset = distance * Math.sin(angle);

        // Create the new location with the same Y as the original
        return origin.clone().add(xOffset, 2, zOffset);
    }

    public static Location getRandomLocationTryingAir(Location origin, double raidus) {
        for(int attempt = 0; attempt < 3; attempt++){
            Location loc = getRandomLocationTryingAir(origin, raidus);
            if(loc.getBlock().isEmpty()){
                return loc;
            }
        }
        return getRandomLocationTryingAir(origin, 1);
    }


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
    private Raid raid;
    private List<LivingEntity> livingEntities;
    private boolean isWon;
    private RaidRoundConfig roundConfig;

    private String failReason;

    RaidRound(Plugin plugin, Raid raid, RaidRoundConfig config){
        this.plugin = plugin;
        this.announcement = Announcement.getInstance(plugin);
        this.startedAt = null;
        this.state = STATUS.CREATED;
        this.roundConfig = config;
        this.raid = raid;
        this.isWon = true;
        this.failReason = "";
    }

    boolean start(){
        if(state != STATUS.CREATED){
            return false;
        }

        this.state = STATUS.STARTED;
        this.startedAt = LocalDateTime.now();
        this.livingEntities = new ArrayList<LivingEntity>();
        List<Player> players = this.raid.getPlayers();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {
                for(RaidRoundEntityConfig entityConfig : roundConfig.getMobsToSpawn()){
                    for(int i=0; i<entityConfig.getHowMany(); i++){
                        if(entityConfig.isOnePerPlayer()){
                            for (Player player : players) {
                                spawnEntity(player.getLocation(), entityConfig, player);
                            }
                        } else {
                            spawnEntity(raid.getLocation(), entityConfig, raid.getPlayerOwner());
                        }
                    }
                }
            }
            
        });

        announcement.sendPrivateMessage(players, 
            this.plugin.getConfigManager().getString(CONFIG_ITEMS.NEW_ROUND_STARTING)
                .replace("{PLAYERS}", String.join(", ", players.stream().map(Player::getName).toList()))
        );

        return true;
    }

    private void spawnEntity(Location location, RaidRoundEntityConfig entityConfig, Player target) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(RaidRound.getRandomLocationTryingAir(location, 10), entityConfig.getType());
        entity.setCustomName(entityConfig.getName());
        entity.setCustomNameVisible(true);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 1, true, false, false));

        // prevent fall damage on spawn
        Metadata.setMetadata(plugin, entity, METADATA_RAID_PLAYER_OWNER, raid.getPlayerOwner().getName());

        // prevent mob from taking damage from the sun
        if (entity instanceof Zombie || entity instanceof Skeleton) {
            ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
            entity.getEquipment().setHelmet(helmet);
            entity.getEquipment().setHelmetDropChance(0.0f); // Ensure the helmet is not dropped on death
        }

        livingEntities.add(entity);
        if (entity instanceof Mob) {
            ((Mob) entity).setTarget(target);
        }
    }

    boolean isFinished(){
        if(this.state == STATUS.FINISHED)
            return true;

        if(this.isExpired()){
            this.failRound(this.plugin.getConfigManager().getString(CONFIG_ITEMS.ROUND_LOST_EXPIRED_TIME_SUBTITLE));
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
        LocalDateTime endTime = startedAt.plusSeconds(roundConfig.getSecondsToComplete());
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

    public void failRound(String failReason) {
        this.isWon = false;
        this.state = STATUS.FINISHED;
        this.failReason = failReason;
        this.onDisable();
        return;
    }

    public String getFailReason(){
        return this.failReason;
    }

    public RaidRoundConfig getRaidRoundConfig(){
        return this.roundConfig;
    }

}
