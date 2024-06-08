package it.loneliness.mc.mobraid.Custom;

import java.util.List;

public class RaidRoundConfig {

    List<RaidRoundEntityConfig> mobsToSpawn;
    int secondsToComplete;
    private int pointsIfWinRound;

    public RaidRoundConfig(List<RaidRoundEntityConfig> list, int secondsToComplete, int pointsIfWinRound){
        this.mobsToSpawn = list;
        this.secondsToComplete = secondsToComplete;
        this.pointsIfWinRound = pointsIfWinRound;
    }

    public int getSecondsToComplete() {
        return secondsToComplete;
    }

    public int getPointsIfWinRound() {
        return pointsIfWinRound;
    }

    public List<RaidRoundEntityConfig> getMobsToSpawn(){
        return mobsToSpawn;
    }
}
