package it.loneliness.mc.mobraid.Custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import it.loneliness.mc.mobraid.Plugin;

class RaidsManager {
    private Plugin plugin; 
    List<Raid> ongoingRaids;
    RaidsManager(Plugin plugin){
        this.plugin = plugin;
        this.ongoingRaids = new ArrayList<>();
    }

    RaidRequest requestNewRaid(Player p){
        //TODO: check everything such as if the player is already
        //if area is free enough etc. Invalid world
        if(true) {
            return new RaidRequestFailure(RaidRequestFailure.FailureReason.CHUNK_ALREADY_IN_RAID);
        }

        //TODO get other players in chunk
        Chunk c = p.getLocation().getChunk();

        List<Player> otherPlayers = Arrays.asList(c.getEntities()).stream().filter(i -> (i instanceof Player && !i.equals(p))).map(i -> (Player) i).toList();

        Raid r = new Raid(c, p, otherPlayers);

        return new RaidRequestSuccess(r);
    }

    Raid getRaidByPlayer(Player p){
        return ongoingRaids.stream().filter(r -> r.isPartecipating(p)).findFirst().orElse(null);
    }

    Raid getRaidByChunk(Chunk c){
        return ongoingRaids.stream().filter(r -> r.isChunk(c)).findFirst().orElse(null);
    }

    void periodicRun(){
        ongoingRaids.forEach(
            raid -> raid.periodicRun()
        );
        ongoingRaids = ongoingRaids.stream().filter(i -> i.getStatus() != Raid.STATUS.FINISHED).toList();
    }

    void onDisable(){
        ongoingRaids.forEach(
            raid -> raid.onDisable()
        );
    }


}