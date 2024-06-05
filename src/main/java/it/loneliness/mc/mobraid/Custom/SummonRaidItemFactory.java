package it.loneliness.mc.mobraid.Custom;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import it.loneliness.mc.mobraid.Plugin;
import it.loneliness.mc.mobraid.Controller.ConfigManager.CONFIG_ITEMS;

public class SummonRaidItemFactory {
    private Plugin plugin;
    private NamespacedKey summonItemNamespacedKey;
    private Material material;

    SummonRaidItemFactory(Plugin plugin){
        this.plugin = plugin;
        this.summonItemNamespacedKey = new NamespacedKey(plugin, "SUMMON_ITEM");

        this.material = Material
                .getMaterial(this.plugin.getConfigManager().getString(CONFIG_ITEMS.SUMMON_RAID_ITEM_TYPE));
        if (material == null) {
            material = Material.ZOMBIE_HEAD; // default to ZOMBIE_HEAD if material is not valid
        }
    }

    public ItemStack craftItem(){
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(this.plugin.getConfigManager().getString(CONFIG_ITEMS.SUMMON_RAID_ITEM_NAME));
            meta.setLore(this.plugin.getConfigManager().getStringList(CONFIG_ITEMS.SUMMON_RAID_ITEM_LORE));
            meta.getPersistentDataContainer().set(summonItemNamespacedKey, PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isItem(ItemStack item) {
        if (item != null && item.getType() == material) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                return meta.getPersistentDataContainer().has(summonItemNamespacedKey, PersistentDataType.BYTE);
            }
        }
        return false;
    }
}
