package me.dogetennant.fluxforge.managers;

import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class MachineEnergyManager {

    private final FluxForge plugin;
    private final Map<String, Integer> buffers = new HashMap<>();

    public MachineEnergyManager(FluxForge plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        buffers.putAll(plugin.getDatabase().loadAllEnergy());
        plugin.getLogger().info("Loaded energy buffers for " + buffers.size() + " machines.");
    }

    public void save() {
        // No-op — saves happen immediately on change now
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," +
                loc.getBlockY() + "," + loc.getBlockZ();
    }

    public int getBuffer(Location loc) {
        return buffers.getOrDefault(key(loc), 0);
    }

    public void setBuffer(Location loc, int amount) {
        int capped = Math.max(0, amount);
        buffers.put(key(loc), capped);
        plugin.getDatabase().saveEnergy(loc, capped);
    }

    public void addBuffer(Location loc, int amount) {
        setBuffer(loc, getBuffer(loc) + amount);
    }

    public boolean consumeBuffer(Location loc, int amount) {
        int current = getBuffer(loc);
        if (current < amount) return false;
        setBuffer(loc, current - amount);
        return true;
    }

    public void removeBuffer(Location loc) {
        buffers.remove(key(loc));
        plugin.getDatabase().deleteEnergy(loc);
    }
}