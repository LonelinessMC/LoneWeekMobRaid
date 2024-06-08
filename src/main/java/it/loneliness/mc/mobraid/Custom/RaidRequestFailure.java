package it.loneliness.mc.mobraid.Custom;

import it.loneliness.mc.mobraid.Plugin;
import it.loneliness.mc.mobraid.Controller.ConfigManager.CONFIG_ITEMS;

public class RaidRequestFailure extends RaidRequest {

    public static enum FailureReason {
        TOO_CLOSE_TO_RAID, 
        ARENA_AREA_WITHOUT_AIR, 
        ARENA_AREA_WITHOUT_SOLID_BASE, 
        OWNER_ALREADY_IN_EXISTING_RAID, 
        HELPERS_ALREADY_IN_EXISTING_RAID
    }

    private Plugin plugin;
    private FailureReason failureReason;

    RaidRequestFailure(Plugin plugin, FailureReason r){
        this.plugin = plugin;
        this.failureReason = r;
    }

    String getFailureReason(){
        switch (failureReason) {
            case TOO_CLOSE_TO_RAID:
                return this.plugin.getConfigManager().getString(CONFIG_ITEMS.TOO_CLOSE_TO_RAID);
            case ARENA_AREA_WITHOUT_AIR:
                return this.plugin.getConfigManager().getString(CONFIG_ITEMS.ARENA_AREA_WITHOUT_AIR);
            case ARENA_AREA_WITHOUT_SOLID_BASE:
                return this.plugin.getConfigManager().getString(CONFIG_ITEMS.ARENA_AREA_WITHOUT_SOLID_BASE);
            case OWNER_ALREADY_IN_EXISTING_RAID:
                return this.plugin.getConfigManager().getString(CONFIG_ITEMS.OWNER_ALREADY_IN_EXISTING_RAID);
            case HELPERS_ALREADY_IN_EXISTING_RAID:
                return this.plugin.getConfigManager().getString(CONFIG_ITEMS.ARENA_AREA_WITHOUT_AIR);
            default:
                return "unknown failure reason - warn the developer";
        }
    }
}
