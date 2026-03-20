package me.dogetennant.fluxforge.managers;

import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class MachineStateManager {

    private final FluxForge plugin;
    private final Map<String, Boolean> states = new HashMap<>();

    public MachineStateManager(FluxForge plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        states.putAll(plugin.getDatabase().loadAllStates());
        plugin.getLogger().info("Loaded " + states.size() + " machine states.");
    }

    public void save() {
        // No-op — saves happen immediately on change
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," +
                loc.getBlockY() + "," + loc.getBlockZ();
    }

    public boolean isEnabled(Location loc) {
        return states.getOrDefault(key(loc), false);
    }

    public void setEnabled(Location loc, boolean enabled) {
        states.put(key(loc), enabled);
        plugin.getDatabase().saveState(loc, enabled);
    }

    public void toggle(Location loc) {
        setEnabled(loc, !isEnabled(loc));
    }

    public void removeState(Location loc) {
        states.remove(key(loc));
        plugin.getDatabase().deleteState(loc);
    }
}