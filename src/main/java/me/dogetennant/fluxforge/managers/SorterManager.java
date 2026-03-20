package me.dogetennant.fluxforge.managers;

import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SorterManager {

    private final FluxForge plugin;
    private final Map<String, String> sorterSources = new HashMap<>();
    private final Map<String, Map<String, List<String>>> sorterFilters = new HashMap<>();

    public SorterManager(FluxForge plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        sorterSources.putAll(plugin.getDatabase().loadAllSorterSources());
        sorterFilters.putAll(plugin.getDatabase().loadAllSorterFilters());
        plugin.getLogger().info("Loaded " + sorterSources.size() + " sorters.");
    }

    private String locationToKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," +
                loc.getBlockY() + "," + loc.getBlockZ();
    }

    public void setSource(Location sorterLoc, Location sourceLoc) {
        sorterSources.put(locationToKey(sorterLoc), locationToKey(sourceLoc));
        plugin.getDatabase().saveSorterSource(sorterLoc, sourceLoc);
    }

    public String getSource(Location sorterLoc) {
        return sorterSources.get(locationToKey(sorterLoc));
    }

    public void addFilter(Location sorterLoc, Location destLoc, String material) {
        String sorterKey = locationToKey(sorterLoc);
        String destKey = locationToKey(destLoc);
        sorterFilters.computeIfAbsent(sorterKey, k -> new HashMap<>())
                .computeIfAbsent(destKey, k -> new ArrayList<>())
                .add(material);
        plugin.getDatabase().saveSorterFilter(sorterLoc, destLoc, material);
    }

    public void removeFilter(Location sorterLoc, Location destLoc) {
        String sorterKey = locationToKey(sorterLoc);
        String destKey = locationToKey(destLoc);
        if (sorterFilters.containsKey(sorterKey)) {
            List<String> materials = sorterFilters.get(sorterKey).get(destKey);
            if (materials != null) {
                for (String material : new ArrayList<>(materials)) {
                    plugin.getDatabase().deleteSorterFilter(sorterLoc, destLoc, material);
                }
            }
            sorterFilters.get(sorterKey).remove(destKey);
        }
    }

    public void removeFilterMaterial(Location sorterLoc, Location destLoc, String material) {
        String sorterKey = locationToKey(sorterLoc);
        String destKey = locationToKey(destLoc);
        if (!sorterFilters.containsKey(sorterKey)) return;
        Map<String, List<String>> filters = sorterFilters.get(sorterKey);
        if (!filters.containsKey(destKey)) return;
        filters.get(destKey).remove(material);
        if (filters.get(destKey).isEmpty()) {
            filters.remove(destKey);
        }
        plugin.getDatabase().deleteSorterFilter(sorterLoc, destLoc, material);
    }

    public Map<String, List<String>> getFilters(Location sorterLoc) {
        return sorterFilters.getOrDefault(locationToKey(sorterLoc), new HashMap<>());
    }

    public void removeSorter(Location sorterLoc) {
        String key = locationToKey(sorterLoc);
        sorterSources.remove(key);
        sorterFilters.remove(key);
        plugin.getDatabase().deleteSorterSource(sorterLoc);
        plugin.getDatabase().deleteAllSorterFilters(sorterLoc);
    }
}