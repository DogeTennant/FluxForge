package me.dogetennant.fluxforge.managers;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GuiManager {

    private final Map<UUID, Location> openGuis = new HashMap<>();
    private final Set<UUID> switching = new HashSet<>();

    public void setOpenGui(UUID playerId, Location loc) {
        openGuis.put(playerId, loc);
    }

    public Location getOpenGui(UUID playerId) {
        return openGuis.get(playerId);
    }

    public void clearOpenGui(UUID playerId) {
        openGuis.remove(playerId);
    }

    public void markSwitching(UUID playerId) {
        switching.add(playerId);
    }

    public boolean isSwitching(UUID playerId) {
        return switching.remove(playerId);
    }
}