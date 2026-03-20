package me.dogetennant.fluxforge.managers;

import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class MachineRegistry {

    private final FluxForge plugin;
    private final Map<String, String> machines = new HashMap<>();

    public MachineRegistry(FluxForge plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        machines.putAll(plugin.getDatabase().loadAllMachines());
        plugin.getLogger().info("Loaded " + machines.size() + " machines.");
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," +
                loc.getBlockY() + "," + loc.getBlockZ();
    }

    public void registerMachine(Location loc, String type) {
        machines.put(key(loc), type);
        plugin.getDatabase().saveMachine(loc, type);
    }

    public void unregisterMachine(Location loc) {
        machines.remove(key(loc));
        plugin.getDatabase().deleteMachine(loc);
    }

    public boolean isMachine(Location loc) {
        return machines.containsKey(key(loc));
    }

    public String getMachineType(Location loc) {
        return machines.get(key(loc));
    }

    public Map<String, String> getAllMachines() {
        return machines;
    }
}