package it.loneliness.mc.mobraid.Controller;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;

import it.loneliness.mc.mobraid.Plugin;

public class Metadata {

    public static void setMetadata(Plugin plugin, Entity entity, String key, String value) {
        entity.setMetadata(key, new org.bukkit.metadata.FixedMetadataValue(plugin, value));
    }

    public static boolean hasMetadata(Entity entity, String key){
        return entity.hasMetadata(key);
    }

    public static String getMetadataString(Plugin plugin, Entity entity, String key) {
        for (MetadataValue metadata : entity.getMetadata(key)) {
            if (metadata.getOwningPlugin().equals(plugin) && metadata.value() instanceof String) {
                return (String) metadata.value();
            }
        }
        return null;
    }
}