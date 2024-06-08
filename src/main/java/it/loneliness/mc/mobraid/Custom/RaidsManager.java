package it.loneliness.mc.mobraid.Custom;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import it.loneliness.mc.mobraid.Plugin;
import it.loneliness.mc.mobraid.Controller.Announcement;
import it.loneliness.mc.mobraid.Controller.ConfigManager;
import it.loneliness.mc.mobraid.Controller.CooldownManager;
import it.loneliness.mc.mobraid.Controller.ConfigManager.CONFIG_ITEMS;
import it.loneliness.mc.mobraid.Controller.Metadata;
import it.loneliness.mc.mobraid.Model.LogHandler;
import it.loneliness.mc.mobraid.Model.PeriodicManagerRunner;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class RaidsManager extends PeriodicManagerRunner implements Listener {
    private Plugin plugin;
    private LogHandler logger;
    List<Raid> ongoingRaids;
    private String npcSubstringName;
    private Announcement announcement;
    private SummonRaidItemFactory summonRaidItemFactory;
    private CooldownManager giveRaidItemCooldown;

    public RaidsManager(Plugin plugin, LogHandler logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.announcement = Announcement.getInstance(plugin);
        this.ongoingRaids = new ArrayList<>();
        this.npcSubstringName = plugin.getConfigManager().getString(ConfigManager.CONFIG_ITEMS.NPC_NAME_SUBSTRING);
        if (this.npcSubstringName == "")
            this.npcSubstringName = null;
        else
            this.npcSubstringName = this.npcSubstringName.toLowerCase();
        this.summonRaidItemFactory = new SummonRaidItemFactory(plugin);

        this.giveRaidItemCooldown = new CooldownManager(plugin.getConfigManager().getInt(ConfigManager.CONFIG_ITEMS.COOLDOWN_GIVE_RAID_ITEM_MINUTES));
    }

    public RaidRequest requestNewRaid(Player p, Location l) {
        //TODO testa questa parte cosa succede se si spawna allo spawn

        boolean aRaidInTheSameArea = this.ongoingRaids.stream()
            .map(Raid::getLocation)
            .filter(location -> location.distanceSquared(l) <= Math.pow(this.plugin.getConfigManager().getInt(CONFIG_ITEMS.ARENA_RADIUS) * 2, 2))
            .findFirst()
            .isPresent();
        if (aRaidInTheSameArea) {
            return new RaidRequestFailure(plugin, RaidRequestFailure.FailureReason.TOO_CLOSE_TO_RAID);
        }

        boolean isTopMostlyAir = percentBlockCheck(l, this.plugin.getConfigManager().getInt(CONFIG_ITEMS.ARENA_RADIUS), 3, 80, Block::isEmpty);
        if(!isTopMostlyAir){
            return new RaidRequestFailure(plugin, RaidRequestFailure.FailureReason.ARENA_AREA_WITHOUT_AIR);
        }

        boolean isBottomMostlySolid = percentBlockCheck(l, this.plugin.getConfigManager().getInt(CONFIG_ITEMS.ARENA_RADIUS), -2, 80, b -> b.getType().isSolid());
        if(!isBottomMostlySolid){
            return new RaidRequestFailure(plugin, RaidRequestFailure.FailureReason.ARENA_AREA_WITHOUT_SOLID_BASE);
        }

        boolean ownerAlreadyInRaid = this.ongoingRaids.stream()
            .flatMap(raid -> raid.getPlayers().stream())
            .anyMatch(raidPlayer -> raidPlayer.equals(p));
        if (ownerAlreadyInRaid) {
            return new RaidRequestFailure(plugin, RaidRequestFailure.FailureReason.OWNER_ALREADY_IN_EXISTING_RAID);
        }

        int distanceRadius = this.plugin.getConfigManager().getInt(ConfigManager.CONFIG_ITEMS.FRIENDS_RADIUS);
        List<Player> otherPlayers = p.getWorld().getNearbyEntities(l, distanceRadius, distanceRadius, distanceRadius).stream()
                .filter(i -> (i instanceof Player) && !i.equals(p) && !i.hasMetadata("NPC")).map(i -> ((Player) i)).toList();

        boolean partecipantsAlreadyInRaid = otherPlayers.stream().anyMatch(player -> 
            this.ongoingRaids.stream()
            .flatMap(raid -> raid.getPlayers().stream())
            .anyMatch(raidPlayer -> raidPlayer.equals(player))
        );
        if (partecipantsAlreadyInRaid) {
            return new RaidRequestFailure(plugin, RaidRequestFailure.FailureReason.HELPERS_ALREADY_IN_EXISTING_RAID);
        }

        Raid r = new Raid(this.plugin, this.logger, l, p, otherPlayers, this.plugin.getConfigManager().getRaidRoundConfigs());

        ongoingRaids.add(r);

        return new RaidRequestSuccess(r);
    }

    private boolean percentBlockCheck(Location l, int radius, int height, int percent, Function<Block, Boolean> checkFunction) {
        int totalBlocks = 0, matchingBlocks = 0;
        int startingY = height > 0 ? l.getBlockY() : l.getBlockY() - 1 + height;
        int endingY = height > 0 ? l.getBlockY()+height : l.getBlockY() - 1;
        World world = l.getWorld();
        for(int currentX = l.getBlockX()-radius; currentX <= l.getBlockX()+radius; currentX++){
            for(int currentZ = l.getBlockZ()-radius; currentZ <= l.getBlockZ()+radius; currentZ++){
                for(int currentY = startingY; currentY <= endingY; currentY++){
                    totalBlocks++;
                    if(checkFunction.apply(world.getBlockAt(currentX, currentY, currentZ))){
                        matchingBlocks++;
                    }
                }
            }
        }

        double matchingPercentage = (double) matchingBlocks / totalBlocks * 100;

        return matchingPercentage >= percent;
    }

    Raid getRaidByPlayer(Player p) {
        return ongoingRaids.stream().filter(r -> r.isPartecipating(p)).findFirst().orElse(null);
    }

    Raid getRaidByOwnerName(String playerName) {
        return ongoingRaids.stream().filter(r -> r.getPlayerOwner().getName().equals(playerName)).findFirst()
                .orElse(null);

    }

    public void periodicRunner() {
        ongoingRaids.forEach(
                raid -> raid.periodicRun());
        ongoingRaids = ongoingRaids.stream().filter(i -> i.getStatus() != Raid.STATUS.FINISHED)
                .collect(Collectors.toList());
    }

    public void onDisable() {
        ongoingRaids.forEach(
                raid -> raid.onDisable());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (Metadata.hasMetadata(event.getEntity(), RaidRound.METADATA_RAID_PLAYER_OWNER)) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            String playerName = Metadata.getMetadataString(plugin, entity, RaidRound.METADATA_RAID_PLAYER_OWNER);
            if (playerName != null) {
                Raid ongoingRaid = this.getRaidByOwnerName(playerName);
                if (ongoingRaid != null) {

                    Player killer = entity.getKiller();
                    if (ongoingRaid.isPartecipating(killer)) {
                        ongoingRaid.periodicRun();
                    } else if (killer != null) { // if mob dies without any killer we allow that
                        ongoingRaid.failRaid("Un boss del raid è stato ucciso da un giocatore non parte del raid");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        if (npcSubstringName != null)
            if (event.getNPC().getName().toLowerCase().contains(npcSubstringName)) {
                Player p = event.getClicker();
                if (p != null) {
                    //TODO implement cooldown
                    giveNewRaidItem(p);
                }
            }
    }

    public void giveNewRaidItem(Player p) {
        if (p == null)
            return;

        // Check if the player has space in their inventory
        if (p.getInventory().firstEmpty() == -1) {
            announcement.sendPrivateMessage(p, this.plugin.getConfigManager().getString(CONFIG_ITEMS.INVENTORY_FULL));
            return;
        }
        
        if (this.giveRaidItemCooldown.isOnCooldown(p)) {
            announcement.sendPrivateMessage(p, this.plugin.getConfigManager().getString(CONFIG_ITEMS.COOLDOWN_GIVE_RAID_ITEM_MESSAGE).replace("{MINUTES}", plugin.getConfigManager().getInt(ConfigManager.CONFIG_ITEMS.COOLDOWN_GIVE_RAID_ITEM_MINUTES)+""));
            return;
        }

        p.getInventory().addItem(summonRaidItemFactory.craftItem());
        this.giveRaidItemCooldown.updateCooldown(p);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (p != null && summonRaidItemFactory.isItem(event.getItemInHand())) {
            Location l = event.getBlock().getLocation();
            if(l != null){
                this.requestNewRaid(p, l);
            }
        }
    }

}