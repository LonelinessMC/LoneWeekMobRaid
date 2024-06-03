package it.loneliness.mc.mobraid.Custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.MetadataValue;

import it.loneliness.mc.mobraid.Plugin;
import it.loneliness.mc.mobraid.Controller.Metadata;
import it.loneliness.mc.mobraid.Model.LogHandler;
import it.loneliness.mc.mobraid.Model.PeriodicManagerRunner;

public class RaidsManager extends PeriodicManagerRunner implements Listener{
    private Plugin plugin; 
    private LogHandler logger;
    List<Raid> ongoingRaids;
    public RaidsManager(Plugin plugin, LogHandler logger){
        this.plugin = plugin;
        this.logger = logger;
        this.ongoingRaids = new ArrayList<>();
    }

    public RaidRequest requestNewRaid(Player p){
        //TODO: check everything such as if the player is already
        //if area is free enough etc. Invalid world
        if(false) {
            return new RaidRequestFailure(RaidRequestFailure.FailureReason.CHUNK_ALREADY_IN_RAID);
        }

        //TODO get other players in chunk
        Chunk c = p.getLocation().getChunk();

        List<Player> otherPlayers = Arrays.asList(c.getEntities()).stream().filter(i -> (i instanceof Player && !i.equals(p))).map(i -> (Player) i).toList();

        Raid r = new Raid(this.plugin, this.logger, c, p, otherPlayers);

        ongoingRaids.add(r);

        return new RaidRequestSuccess(r);
    }

    Raid getRaidByPlayer(Player p){
        return ongoingRaids.stream().filter(r -> r.isPartecipating(p)).findFirst().orElse(null);
    }

    Raid getRaidByOwnerName(String playerName){
        return ongoingRaids.stream().filter(r -> r.getPlayerOwner().getName().equals(playerName)).findFirst().orElse(null);

    }

    Raid getRaidByChunk(Chunk c){
        return ongoingRaids.stream().filter(r -> r.isChunk(c)).findFirst().orElse(null);
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

        Entity entity = event.getEntity();
        if(Metadata.hasMetadata(entity, RaidRound.METADATA_RAID_PLAYER_OWNER)){
            String playerName = Metadata.getMetadataString(plugin, entity, RaidRound.METADATA_RAID_PLAYER_OWNER);
            if(playerName != null){
                Raid ongoingRaid = this.getRaidByOwnerName(playerName);
                if(ongoingRaid != null){
                    ongoingRaid.periodicRun();
                }
            }
        }
    }

}