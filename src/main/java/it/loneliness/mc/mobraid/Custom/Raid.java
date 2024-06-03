package it.loneliness.mc.mobraid.Custom;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class Raid {

    public static enum STATUS {
        STARTED,
        FINISHED
    };

    private Chunk chunk;
    private Player player;
    private List<Player> helpers;
    private STATUS state;

    Raid(Chunk chunk, Player player, List<Player> helpers){
        this.chunk = chunk;
        this.player = player;
        this.helpers = helpers;
        this.state = STATUS.STARTED;

        //TODO registerChunk to get it loaded
    }

    public Object periodicRun() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'periodicRun'");
    }

    public Object onDisable() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onDisable'");
    }

    public STATUS getStatus() {
        return this.state;
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
}