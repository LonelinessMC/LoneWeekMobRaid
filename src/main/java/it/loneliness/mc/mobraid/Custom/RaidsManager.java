package it.loneliness.mc.mobraid.Custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import it.loneliness.mc.mobraid.Plugin;
import it.loneliness.mc.mobraid.Controller.ConfigManager;
import it.loneliness.mc.mobraid.Controller.Metadata;
import it.loneliness.mc.mobraid.Model.LogHandler;
import it.loneliness.mc.mobraid.Model.PeriodicManagerRunner;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class RaidsManager extends PeriodicManagerRunner implements Listener{
    private Plugin plugin; 
    private LogHandler logger;
    List<Raid> ongoingRaids;
    private String npcSubstringName;
    public RaidsManager(Plugin plugin, LogHandler logger){
        this.plugin = plugin;
        this.logger = logger;
        this.ongoingRaids = new ArrayList<>();
        this.npcSubstringName = plugin.getConfigManager().getString(ConfigManager.CONFIG_ITEMS.NPC_NAME_SUBSTRING);
        if(this.npcSubstringName == "")
            this.npcSubstringName = null;
        else
            this.npcSubstringName = this.npcSubstringName.toLowerCase();
    }

    public RaidRequest requestNewRaid(Player p){
        //TODO: check everything such as if the player is already
        //if area is free enough etc. Invalid world
        if(false) {
            return new RaidRequestFailure(RaidRequestFailure.FailureReason.CHUNK_ALREADY_IN_RAID);
        }

        Location l = p.getLocation();

        //TODO MAKE THIS RADIUS A VARIABLE
        List<Player> otherPlayers = p.getWorld().getNearbyEntities(l, 20, 20, 20).stream().filter(i -> (i instanceof Player) && !i.equals(p)).map(i -> ((Player) i)).toList();

        Raid r = new Raid(this.plugin, this.logger, l, p, otherPlayers);

        ongoingRaids.add(r);

        return new RaidRequestSuccess(r);
    }

    Raid getRaidByPlayer(Player p){
        return ongoingRaids.stream().filter(r -> r.isPartecipating(p)).findFirst().orElse(null);
    }

    Raid getRaidByOwnerName(String playerName){
        return ongoingRaids.stream().filter(r -> r.getPlayerOwner().getName().equals(playerName)).findFirst().orElse(null);

    }

    public void periodicRunner(){
        ongoingRaids.forEach(
            raid -> raid.periodicRun()
        );
        ongoingRaids = ongoingRaids.stream().filter(i -> i.getStatus() != Raid.STATUS.FINISHED).collect(Collectors.toList());
    }

    public void onDisable(){
        ongoingRaids.forEach(
            raid -> raid.onDisable()
        );
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        //TODO if the killer is not partecipating then the Raid should be over and lost
        if(Metadata.hasMetadata(event.getEntity(), RaidRound.METADATA_RAID_PLAYER_OWNER)){
            LivingEntity entity = (LivingEntity) event.getEntity();
            String playerName = Metadata.getMetadataString(plugin, entity, RaidRound.METADATA_RAID_PLAYER_OWNER);
            if(playerName != null){
                Raid ongoingRaid = this.getRaidByOwnerName(playerName);
                if(ongoingRaid != null){

                    Player killer = entity.getKiller();
                    if(ongoingRaid.isPartecipating(killer)){
                        ongoingRaid.periodicRun();
                    } else if (killer != null){ // if mob dies without any killer we allow that
                        ongoingRaid.failRaid("Un boss del raid è stato ucciso da un giocatore non parte del raid");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        if(npcSubstringName != null)
            if(event.getNPC().getName().toLowerCase().contains(npcSubstringName)){
                Player p = event.getClicker();
                if(p != null){
                    giveNewRaidItem(p);
                }
            }
    }

    public void giveNewRaidItem(Player p){
        p.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
    }

}