package it.loneliness.mc.mobraid.Custom;

public class RaidRequestSuccess extends RaidRequest {
    private Raid raid;

    RaidRequestSuccess(Raid r){
        this.raid = r;
    }

    Raid getRaid(){
        return this.raid;
    }
}
