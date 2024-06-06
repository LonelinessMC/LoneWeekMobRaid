package it.loneliness.mc.mobraid.Custom;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import it.loneliness.mc.mobraid.Plugin;
import it.loneliness.mc.mobraid.Controller.Announcement;
import it.loneliness.mc.mobraid.Controller.ConfigManager;
import it.loneliness.mc.mobraid.Model.LogHandler;

public class Raid {

    public static enum STATUS {
        STARTED,
        FINISHED
    };

    private Plugin plugin;
    private LogHandler logger;
    private Announcement announcement;
    private Location location;
    private Player player;
    private List<Player> helpers;
    private STATUS state;
    private List<RaidRound> rounds;
    private int currentRoundIndex;
    private int maxAllowedDistanceSquared;

    Raid(Plugin plugin, LogHandler logger, Location location, Player player, List<Player> helpers, List<RaidRoundConfig> raidRoundConfigs){
        this.plugin = plugin;
        this.logger = logger;
        this.announcement = Announcement.getInstance(plugin);
        this.location = location;
        this.player = player;
        this.helpers = helpers;
        this.state = STATUS.STARTED;

        // ^2 to compensate for distanceSquared
        this.maxAllowedDistanceSquared = this.plugin.getConfigManager().getInt(ConfigManager.CONFIG_ITEMS.ARENA_RADIUS)*this.plugin.getConfigManager().getInt(ConfigManager.CONFIG_ITEMS.ARENA_RADIUS);

        this.rounds = new ArrayList<RaidRound>();

        raidRoundConfigs.forEach(raidRoundConfig -> rounds.add(new RaidRound(plugin, this, raidRoundConfig)));

        this.currentRoundIndex = 0;
        rounds.get(currentRoundIndex).start();
    }

    public void failRaid(String failMessage){
        try{
            RaidRound currentRound = this.rounds.get(this.currentRoundIndex);
            currentRound.failRound();
        } catch (IndexOutOfBoundsException e){}
        announcement.sendPrivateMessage(this.getPlayers(), "RAID lost:" + failMessage);
        this.state = STATUS.FINISHED;
    }

    public void winRaid(){
        announcement.sendPrivateMessage(this.getPlayers(), "RAID won");
        this.state = STATUS.FINISHED;
    }

    public void removeHelper(Player helper){
        this.helpers = this.helpers.stream().filter(h -> !h.equals(helper)).collect(Collectors.toList());
        announcement.sendPrivateMessage(helper, "Hai abbandonato il raid");
        announcement.sendPrivateMessage(this.getPlayers(), helper.getName()+" non sta più partecipando al raid");
    }

    public boolean checkDistance(Player player){
        double distance = player.getLocation().distanceSquared(location);
        return (distance > maxAllowedDistanceSquared);
    }
    public boolean checkNotOnline(Player player){
        return Bukkit.getPlayerExact(player.getName()) == null;
    }


    private void runCheck(Function<Player, Boolean> checkFunction, Player subject, String personalWarning, String ownerWarning, String failRaidMsg,
    String personalOkMessage, String ownerOkMessage) {

        if(checkFunction.apply(subject)){
            if(personalWarning != null){
                announcement.sendPrivateMessage(subject, personalWarning.replace("{PLAYER}", subject.getName()));
            }
            if(!subject.equals(this.getPlayerOwner()) && ownerWarning != null)
                announcement.sendPrivateMessage(this.getPlayerOwner(), ownerWarning.replace("{PLAYER}", subject.getName()));

            //wait INFRINGMENT_TIMEOUT_SECONDS seconds if player still infringing rules
            Bukkit.getServer().getScheduler().runTaskLater(this.plugin, new Runnable() {

                @Override
                public void run() {
                    if(state != STATUS.FINISHED){ //if in the meantime the raid has finished then we're not interested in this check
                        if(checkFunction.apply(subject)){
                            if(subject.equals(getPlayerOwner())){
                                failRaid(failRaidMsg);
                            } else {
                                removeHelper(subject);
                            }
                        } else {
                            announcement.sendPrivateMessage(subject, personalOkMessage.replace("{PLAYER}", subject.getName()));
                            if(!subject.equals(getPlayerOwner()))
                                announcement.sendPrivateMessage(getPlayerOwner(), ownerOkMessage.replace("{PLAYER}", subject.getName()));
                        }
                    }
                }
                
            }, this.plugin.getConfigManager().getInt(ConfigManager.CONFIG_ITEMS.INFRINGMENT_TIMEOUT_SECONDS)*20); // *20 because every second has 20 ticks
        }

            
    }

    public synchronized void periodicRun() {
        logger.debug("periodicRun is running");
        if(this.state == STATUS.STARTED){

            for (Player player : this.getPlayers()) {

                this.runCheck(
                    this::checkNotOnline, 
                    player, 
                    null, //this has to be null cause player could be null if not online
                    null, //this has to be null cause player could be null if not online
                    "l'owner ha lasciato il server", 
                    "Sei tornato nel server",
                    "Il player {PLAYER} è tornato nel server"
                );

                this.runCheck(
                    this::checkDistance, 
                    player, 
                    "Torna nell'arena", 
                    "Il player {PLAYER} si sta allontanando dall'arena",
                    "l'owner ha lasciato l'area di gioco", 
                    "Sei tornato nell'arena",
                    "Il player {PLAYER} è tornato nell'arena"
                );
            }

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
                        this.winRaid();
                    } else {
                        this.failRaid("non avete vinto il round");
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
        List<Player> output = new ArrayList<>();
        output.add(this.getPlayerOwner());
        output.addAll(this.helpers);
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

    public Player getPlayerOwner() {
        return this.player;
    }

    public Location getLocation() {
        return this.location;
    }
}