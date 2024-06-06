package it.loneliness.mc.mobraid.Custom;

import java.util.List;

public class RaidRoundConfig {

    List<RaidRoundEntityConfig> mobsToSpawn;
    int secondsToComplete;

    public RaidRoundConfig(List<RaidRoundEntityConfig> list, int secondsToComplete){
        this.mobsToSpawn = list;
        this.secondsToComplete = secondsToComplete;
    }

    public int getSecondsToComplete() {
        return secondsToComplete;
    }

    public List<RaidRoundEntityConfig> getMobsToSpawn(){
        return mobsToSpawn;
    }
}
