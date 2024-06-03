package it.loneliness.mc.mobraid.Custom;

public class RaidRequestFailure extends RaidRequest {

    public static enum FailureReason {
        NOT_ENOUGH_SPACE,
        PLAYER_ALREADY_IN_RAID,
        CHUNK_ALREADY_IN_RAID
    }

    private FailureReason failureReason;

    RaidRequestFailure(FailureReason r){
        this.failureReason = r;
    }

    String getFailureReason(){
        //TODO find a way to display the failure reason from config strings
        return "";
    }
}
