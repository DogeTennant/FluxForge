package me.dogetennant.fluxforge.managers;

import me.dogetennant.fluxforge.FluxForge;
import me.dogetennant.fluxforge.machines.JetpackItem;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ChargingStationManager {

    private final FluxForge plugin;
    private final Map<String, ItemStack> stationItems = new HashMap<>();

    public ChargingStationManager(FluxForge plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        stationItems.putAll(plugin.getDatabase().loadAllChargingItems());
        plugin.getLogger().info("Loaded " + stationItems.size() + " charging stations.");
    }

    public void save() {
        // No-op — saves happen immediately on change
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," +
                loc.getBlockY() + "," + loc.getBlockZ();
    }

    public ItemStack getItem(Location loc) {
        return stationItems.get(key(loc));
    }

    public void setItem(Location loc, ItemStack item) {
        if (item == null) {
            stationItems.remove(key(loc));
            plugin.getDatabase().deleteChargingItem(loc);
        } else {
            stationItems.put(key(loc), item);
            plugin.getDatabase().saveChargingItem(loc, item);
        }
    }

    public void removeStation(Location loc) {
        stationItems.remove(key(loc));
        plugin.getDatabase().deleteChargingItem(loc);
    }

    public boolean isChargeable(ItemStack item) {
        return JetpackItem.isJetpack(item);
    }

    public boolean isFullyCharged(ItemStack item) {
        if (JetpackItem.isJetpack(item)) {
            return JetpackItem.getCharge(item) >= JetpackItem.MAX_CHARGE;
        }
        return true;
    }

    public int getMaxCharge(ItemStack item) {
        if (JetpackItem.isJetpack(item)) return JetpackItem.MAX_CHARGE;
        return 0;
    }

    public int getCurrentCharge(ItemStack item) {
        if (JetpackItem.isJetpack(item)) return JetpackItem.getCharge(item);
        return 0;
    }

    public void addCharge(ItemStack item, int amount) {
        if (JetpackItem.isJetpack(item)) {
            JetpackItem.setCharge(item, JetpackItem.getCharge(item) + amount);
        }
    }
}