package it.loneliness.mc.mobraid.Custom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import it.loneliness.mc.mobraid.Plugin;
import it.loneliness.mc.mobraid.Controller.Announcement;
import it.loneliness.mc.mobraid.Model.LogHandler;

public class Raid {

    public static enum STATUS {
        STARTED,
        FINISHED
    };

    private Plugin plugin;
    private LogHandler logger;
    private Announcement announcement;
    private Chunk chunk;
    private Player player;
    private List<Player> helpers;
    private STATUS state;
    private List<RaidRound> rounds;
    private int currentRoundIndex;

    Raid(Plugin plugin, LogHandler logger, Chunk chunk, Player player, List<Player> helpers){
        this.plugin = plugin;
        this.logger = logger;
        this.announcement = Announcement.getInstance(plugin);
        this.chunk = chunk;
        this.player = player;
        this.helpers = helpers;
        this.state = STATUS.STARTED;
        this.rounds = new ArrayList<RaidRound>();
        rounds.add(new RaidRound(plugin, this, 40));


        this.currentRoundIndex = 0;
        rounds.get(currentRoundIndex).start();

        //TODO registerChunk to get it loaded
    }

    public synchronized void periodicRun() {
        if(this.state == STATUS.STARTED){
            RaidRound currentRound = this.rounds.get(this.currentRoundIndex);
            if(currentRound.isFinished()){
                if(currentRound.isWon()){
                    announcement.sendPrivateMessage(player, "Round won");
                    //TODO handle player round win

                } else {
                    announcement.sendPrivateMessage(player, "Round lost");
                    //TODO handle player roudn loss
                }

                this.currentRoundIndex++;
                if(this.currentRoundIndex >= this.rounds.size()){
                    if(currentRound.isWon()){
                        announcement.sendPrivateMessage(player, "RAID won");
                        //TODO handle player raid win
                    } else {
                        announcement.sendPrivateMessage(player, "RAID lost");
                        //TODO handle player raid loss
                    }
                    this.state = STATUS.FINISHED;
                } else {
                    this.rounds.get(this.currentRoundIndex).start();
                }
            }
        }
    }

    public void onDisable() {
        this.rounds.forEach(r -> r.onDisable());
    }

    public STATUS getStatus() {
        return this.state;
    }

    public List<Player> getPlayers(){
        List<Player> output = new ArrayList<>(this.helpers);
        output.addFirst(player);
        return output;
    }

    public boolean isPartecipating(Player testPlayer){
        if(this.player.equals(testPlayer))
            return true;
        
        for (Player player : helpers)
            if(player.equals(testPlayer))
                return true;

        return false;
    }

    public boolean isChunk(Chunk c){
        return this.chunk.equals(c);
    }

    public Player getPlayerOwner() {
        return this.player;
    }
}