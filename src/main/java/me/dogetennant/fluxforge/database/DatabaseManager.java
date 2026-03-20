package me.dogetennant.fluxforge.database;

import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DatabaseManager {

    protected final FluxForge plugin;

    public DatabaseManager(FluxForge plugin) {
        this.plugin = plugin;
    }

    // Connection
    public abstract void connect() throws Exception;
    public abstract void disconnect();
    public abstract void createTables();

    // Machines
    public abstract void saveMachine(Location loc, String type);
    public abstract void deleteMachine(Location loc);
    public abstract Map<String, String> loadAllMachines();

    // Energy
    public abstract void saveEnergy(Location loc, int amount);
    public abstract void deleteEnergy(Location loc);
    public abstract Map<String, Integer> loadAllEnergy();

    // Fuel
    public abstract void saveFuel(Location loc, int amount);
    public abstract void deleteFuel(Location loc);
    public abstract Map<String, Integer> loadAllFuel();

    // Networks
    public abstract void saveNetwork(String networkId, Set<String> blocks);
    public abstract void deleteNetwork(String networkId);
    public abstract Map<String, Set<String>> loadAllNetworks();

    // States
    public abstract void saveState(Location loc, boolean enabled);
    public abstract void deleteState(Location loc);
    public abstract Map<String, Boolean> loadAllStates();

    // Sorters
    public abstract void saveSorterSource(Location sorterLoc, Location sourceLoc);
    public abstract void deleteSorterSource(Location sorterLoc);
    public abstract Map<String, String> loadAllSorterSources();

    // Sorter filters
    public abstract void saveSorterFilter(Location sorterLoc, Location destLoc, String material);
    public abstract void deleteSorterFilter(Location sorterLoc, Location destLoc, String material);
    public abstract void deleteAllSorterFilters(Location sorterLoc);
    public abstract Map<String, Map<String, List<String>>> loadAllSorterFilters();

    // Charging
    public abstract void saveChargingItem(Location loc, ItemStack item);
    public abstract void deleteChargingItem(Location loc);
    public abstract Map<String, ItemStack> loadAllChargingItems();

    // Helper
    protected String locationToKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," +
                loc.getBlockY() + "," + loc.getBlockZ();
    }
}