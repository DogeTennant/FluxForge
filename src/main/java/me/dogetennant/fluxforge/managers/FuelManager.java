package me.dogetennant.fluxforge.managers;

import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class FuelManager {

    private final FluxForge plugin;
    private final Map<String, Integer> fuelStorage = new HashMap<>();

    public FuelManager(FluxForge plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        fuelStorage.putAll(plugin.getDatabase().loadAllFuel());
        plugin.getLogger().info("Loaded fuel data for " + fuelStorage.size() + " generators.");
    }

    private String locationToKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," +
                loc.getBlockY() + "," + loc.getBlockZ();
    }

    public int getFuel(Location loc) {
        return fuelStorage.getOrDefault(locationToKey(loc), 0);
    }

    public void addFuel(Location loc, int amount) {
        int current = getFuel(loc);
        int newAmount = Math.max(0, current + amount);
        fuelStorage.put(locationToKey(loc), newAmount);
        plugin.getDatabase().saveFuel(loc, newAmount);
    }

    public void removeFuel(Location loc) {
        fuelStorage.remove(locationToKey(loc));
        plugin.getDatabase().deleteFuel(loc);
    }
}