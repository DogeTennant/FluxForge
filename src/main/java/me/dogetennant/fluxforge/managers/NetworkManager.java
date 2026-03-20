package me.dogetennant.fluxforge.managers;

import me.dogetennant.fluxforge.FluxForge;
import me.dogetennant.fluxforge.machines.MachineType;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class NetworkManager {

    private final FluxForge plugin;
    private final Map<String, String> blockToNetwork = new HashMap<>();
    private final Map<String, Set<String>> networkBlocks = new HashMap<>();

    public NetworkManager(FluxForge plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        Map<String, Set<String>> networks = plugin.getDatabase().loadAllNetworks();
        for (Map.Entry<String, Set<String>> entry : networks.entrySet()) {
            String netId = entry.getKey();
            Set<String> blocks = entry.getValue();
            networkBlocks.put(netId, blocks);
            for (String blockKey : blocks) {
                blockToNetwork.put(blockKey, netId);
            }
        }
        plugin.getLogger().info("Loaded " + networkBlocks.size() + " energy networks.");
        repairMissingNetworks();
    }

    public void save() {
        for (Map.Entry<String, Set<String>> entry : networkBlocks.entrySet()) {
            plugin.getDatabase().saveNetwork(entry.getKey(), entry.getValue());
        }
    }

    // ---- Location helpers ----

    public String locationToKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," +
                loc.getBlockY() + "," + loc.getBlockZ();
    }

    public Location keyToLocation(String key) {
        try {
            String[] parts = key.split(",");
            World world = plugin.getServer().getWorld(parts[0]);
            if (world == null) return null;
            return new Location(world, Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (Exception e) {
            return null;
        }
    }

    // ---- Network query ----

    public Set<String> getAllNetworkIds() {
        return new HashSet<>(networkBlocks.keySet());
    }

    public Set<String> getNetworkBlocks(String netId) {
        return networkBlocks.getOrDefault(netId, new HashSet<>());
    }

    public String getNetworkId(Location loc) {
        return blockToNetwork.get(locationToKey(loc));
    }

    public boolean isOnNetwork(Location loc) {
        return blockToNetwork.containsKey(locationToKey(loc));
    }

    public int getNetworkAvailableEnergy(Location loc) {
        String netId = blockToNetwork.get(locationToKey(loc));
        if (netId == null) return 0;

        int total = 0;
        for (String key : networkBlocks.getOrDefault(netId, new HashSet<>())) {
            Location blockLoc = keyToLocation(key);
            if (blockLoc == null || !plugin.getMachineRegistry().isMachine(blockLoc)) continue;
            MachineType type = MachineType.valueOf(
                    plugin.getMachineRegistry().getMachineType(blockLoc));
            if (type == MachineType.GENERATOR || type == MachineType.SOLAR_PANEL ||
                    type == MachineType.BATTERY) {
                total += plugin.getMachineEnergyManager().getBuffer(blockLoc);
            }
        }
        return total;
    }

    public boolean consumeFromNetwork(Location loc, int amount) {
        String netId = blockToNetwork.get(locationToKey(loc));
        if (netId == null) return false;

        if (getNetworkAvailableEnergy(loc) < amount) return false;

        int remaining = amount;
        int batTransferRate = plugin.getConfig().getInt("machines.battery.transfer-rate", 100);

        for (String key : networkBlocks.getOrDefault(netId, new HashSet<>())) {
            if (remaining <= 0) break;
            Location blockLoc = keyToLocation(key);
            if (blockLoc == null || !plugin.getMachineRegistry().isMachine(blockLoc)) continue;
            MachineType type = MachineType.valueOf(
                    plugin.getMachineRegistry().getMachineType(blockLoc));
            if (type == MachineType.GENERATOR || type == MachineType.SOLAR_PANEL) {
                int take = Math.min(plugin.getMachineEnergyManager().getBuffer(blockLoc), remaining);
                plugin.getMachineEnergyManager().addBuffer(blockLoc, -take);
                remaining -= take;
            }
        }

        for (String key : networkBlocks.getOrDefault(netId, new HashSet<>())) {
            if (remaining <= 0) break;
            Location blockLoc = keyToLocation(key);
            if (blockLoc == null || !plugin.getMachineRegistry().isMachine(blockLoc)) continue;
            MachineType type = MachineType.valueOf(
                    plugin.getMachineRegistry().getMachineType(blockLoc));
            if (type == MachineType.BATTERY) {
                int take = Math.min(plugin.getMachineEnergyManager().getBuffer(blockLoc), remaining);
                plugin.getMachineEnergyManager().addBuffer(blockLoc, -take);
                remaining -= take;
            }
        }

        return remaining <= 0;
    }

    public boolean isChestOnNetwork(Location chestLoc, Location sorterLoc) {
        String sorterNetId = blockToNetwork.get(locationToKey(sorterLoc));
        if (sorterNetId == null) return false;

        int[][] faces = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        for (int[] face : faces) {
            Location neighbor = chestLoc.clone().add(face[0], face[1], face[2]);
            String neighborNetId = blockToNetwork.get(locationToKey(neighbor));
            if (sorterNetId.equals(neighborNetId)) return true;
        }
        return false;
    }

    // ---- Network discovery ----

    private boolean isNetworkMember(Location loc) {
        return plugin.getMachineRegistry().isMachine(loc) ||
                loc.getBlock().getType() == org.bukkit.Material.COPPER_GRATE;
    }

    private Set<String> bfsDiscover(Location start, String excludeKey) {
        Set<String> visited = new HashSet<>();
        Queue<Location> queue = new LinkedList<>();
        queue.add(start);
        visited.add(locationToKey(start));

        int[][] faces = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        while (!queue.isEmpty()) {
            Location current = queue.poll();
            for (int[] face : faces) {
                Location neighbor = current.clone().add(face[0], face[1], face[2]);
                String neighborKey = locationToKey(neighbor);
                if (visited.contains(neighborKey)) continue;
                if (neighborKey.equals(excludeKey)) continue;
                if (isNetworkMember(neighbor)) {
                    visited.add(neighborKey);
                    queue.add(neighbor);
                }
            }
        }
        return visited;
    }

    // ---- Network management ----

    public void onBlockAdded(Location loc) {
        int[][] faces = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        Set<String> neighborNetworks = new HashSet<>();

        for (int[] face : faces) {
            Location neighbor = loc.clone().add(face[0], face[1], face[2]);
            String neighborKey = locationToKey(neighbor);
            if (blockToNetwork.containsKey(neighborKey)) {
                neighborNetworks.add(blockToNetwork.get(neighborKey));
            }
        }

        if (neighborNetworks.isEmpty()) {
            String newNetId = UUID.randomUUID().toString();
            Set<String> blocks = new HashSet<>();
            blocks.add(locationToKey(loc));
            networkBlocks.put(newNetId, blocks);
            blockToNetwork.put(locationToKey(loc), newNetId);
            plugin.getDatabase().saveNetwork(newNetId, blocks);
        } else if (neighborNetworks.size() == 1) {
            String netId = neighborNetworks.iterator().next();
            Set<String> blocks = networkBlocks.get(netId);
            if (blocks == null) {
                blocks = new HashSet<>();
                networkBlocks.put(netId, blocks);
            }
            blocks.add(locationToKey(loc));
            blockToNetwork.put(locationToKey(loc), netId);
            plugin.getDatabase().saveNetwork(netId, blocks);
        } else {
            String mergedNetId = UUID.randomUUID().toString();
            Set<String> allBlocks = new HashSet<>();

            for (String netId : neighborNetworks) {
                Set<String> netBlocks = networkBlocks.get(netId);
                if (netBlocks != null) allBlocks.addAll(netBlocks);
                networkBlocks.remove(netId);
                plugin.getDatabase().deleteNetwork(netId);
            }

            allBlocks.add(locationToKey(loc));
            networkBlocks.put(mergedNetId, allBlocks);
            for (String blockKey : allBlocks) {
                blockToNetwork.put(blockKey, mergedNetId);
            }
            plugin.getDatabase().saveNetwork(mergedNetId, allBlocks);
        }
    }

    public void onBlockRemoved(Location loc) {
        String key = locationToKey(loc);
        String oldNetId = blockToNetwork.remove(key);
        if (oldNetId == null) return;

        networkBlocks.remove(oldNetId);
        blockToNetwork.remove(key);
        plugin.getDatabase().deleteNetwork(oldNetId);

        int[][] faces = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        Set<String> alreadyAssigned = new HashSet<>();
        List<Set<String>> newNetworks = new ArrayList<>();

        for (int[] face : faces) {
            Location neighbor = loc.clone().add(face[0], face[1], face[2]);
            String neighborKey = locationToKey(neighbor);
            if (alreadyAssigned.contains(neighborKey)) continue;
            if (!isNetworkMember(neighbor)) continue;

            Set<String> discovered = bfsDiscover(neighbor, key);
            newNetworks.add(discovered);
            alreadyAssigned.addAll(discovered);
        }

        for (Set<String> networkGroup : newNetworks) {
            String newNetId = UUID.randomUUID().toString();
            networkBlocks.put(newNetId, networkGroup);
            for (String blockKey : networkGroup) {
                blockToNetwork.put(blockKey, newNetId);
            }
            plugin.getDatabase().saveNetwork(newNetId, networkGroup);
        }
    }

    private void repairMissingNetworks() {
        for (String key : plugin.getMachineRegistry().getAllMachines().keySet()) {
            if (!blockToNetwork.containsKey(key)) {
                Location loc = keyToLocation(key);
                if (loc != null) {
                    onBlockAdded(loc);
                    plugin.getLogger().info("Repaired missing network for machine at " + key);
                }
            }
        }
    }
}