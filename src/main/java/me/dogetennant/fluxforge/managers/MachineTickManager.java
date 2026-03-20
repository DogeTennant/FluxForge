package me.dogetennant.fluxforge.managers;

import me.dogetennant.fluxforge.FluxForge;
import me.dogetennant.fluxforge.machines.MachineType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MachineTickManager {

    private final FluxForge plugin;

    // Max buffers for consumer machines
    private static final int CONSUMER_MAX_BUFFER = 500;

    public MachineTickManager(FluxForge plugin) {
        this.plugin = plugin;
        startTicking();
    }

    private void startTicking() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    private void tick() {
        for (String netId : plugin.getNetworkManager().getAllNetworkIds()) {
            tickNetwork(netId);
        }
        plugin.getMachineEnergyManager().save();
    }

    private void tickNetwork(String netId) {
        Set<String> blocks = plugin.getNetworkManager().getNetworkBlocks(netId);
        if (blocks == null || blocks.isEmpty()) return;

        MachineEnergyManager energy = plugin.getMachineEnergyManager();
        FuelManager fuel = plugin.getFuelManager();

        List<Location> generators = new ArrayList<>();
        List<Location> solarPanels = new ArrayList<>();
        List<Location> batteries = new ArrayList<>();
        List<Location> consumers = new ArrayList<>();
        List<Location> chargingStations = new ArrayList<>();

        for (String key : blocks) {
            Location loc = plugin.getNetworkManager().keyToLocation(key);
            if (loc == null || !plugin.getMachineRegistry().isMachine(loc)) continue;
            MachineType type = MachineType.valueOf(plugin.getMachineRegistry().getMachineType(loc));
            switch (type) {
                case GENERATOR -> generators.add(loc);
                case SOLAR_PANEL -> solarPanels.add(loc);
                case BATTERY -> batteries.add(loc);
                case CHARGING_STATION -> {
                    consumers.add(loc);
                    chargingStations.add(loc);
                }
                case ELECTRIC_FURNACE -> consumers.add(loc);
                case MINER, MOB_GRINDER, ITEM_SORTER -> {
                    // Only add to consumers if enabled (or not toggleable)
                    if (type == MachineType.ITEM_SORTER ||
                            plugin.getMachineStateManager().isEnabled(loc)) {
                        consumers.add(loc);
                    }
                }
                default -> {}
            }
        }

        int genTransferRate = plugin.getConfig().getInt("machines.generator.transfer-rate", 50);
        int solTransferRate = plugin.getConfig().getInt("machines.solar-panel.transfer-rate", 20);
        int batTransferRate = plugin.getConfig().getInt("machines.battery.transfer-rate", 100);
        int chargeRate = plugin.getConfig().getInt("machines.charging-station.charge-rate", 10);
        int batMax = plugin.getConfig().getInt("machines.battery.max-buffer", 5000);


        // Phase 1: Generators and solar produce into their own buffers
        for (Location loc : generators) produceGenerator(loc, energy, fuel);
        for (Location loc : solarPanels) produceSolar(loc, energy);

        // Phase 2: Calculate total available this tick from ALL sources
        // Generators contribute up to their transfer rate
        int genAvailable = 0;
        for (Location loc : generators) {
            genAvailable += Math.min(energy.getBuffer(loc), genTransferRate);
        }
        for (Location loc : solarPanels) {
            genAvailable += Math.min(energy.getBuffer(loc), solTransferRate);
        }

        // Batteries contribute up to their transfer rate
        int batAvailable = 0;
        for (Location loc : batteries) {
            batAvailable += Math.min(energy.getBuffer(loc), batTransferRate);
        }

        int totalAvailable = genAvailable + batAvailable;
        if (totalAvailable <= 0 && consumers.isEmpty()) return;

        // Phase 3: Distribute evenly to consumers that need energy
        if (!consumers.isEmpty() && totalAvailable > 0) {
            // Find consumers that actually need energy
            List<Location> needyConsumers = new ArrayList<>();
            for (Location loc : consumers) {
                if (energy.getBuffer(loc) < CONSUMER_MAX_BUFFER) {
                    needyConsumers.add(loc);
                }
            }

            if (!needyConsumers.isEmpty()) {
                int perConsumer = Math.min(
                        totalAvailable / needyConsumers.size(),
                        CONSUMER_MAX_BUFFER);

                if (perConsumer > 0) {
                    for (Location loc : needyConsumers) {
                        int currentBuffer = energy.getBuffer(loc);
                        int give = Math.min(perConsumer, CONSUMER_MAX_BUFFER - currentBuffer);
                        if (give <= 0) continue;

                        // Draw from generators first
                        int remaining = give;
                        for (Location gen : generators) {
                            if (remaining <= 0) break;
                            int avail = Math.min(energy.getBuffer(gen), genTransferRate);
                            int take = Math.min(avail, remaining);
                            if (take > 0) {
                                energy.addBuffer(gen, -take);
                                energy.addBuffer(loc, take);
                                remaining -= take;
                            }
                        }
                        for (Location sol : solarPanels) {
                            if (remaining <= 0) break;
                            int avail = Math.min(energy.getBuffer(sol), solTransferRate);
                            int take = Math.min(avail, remaining);
                            if (take > 0) {
                                energy.addBuffer(sol, -take);
                                energy.addBuffer(loc, take);
                                remaining -= take;
                            }
                        }

                        // Then draw from batteries
                        for (Location bat : batteries) {
                            if (remaining <= 0) break;
                            int avail = Math.min(energy.getBuffer(bat), batTransferRate);
                            int take = Math.min(avail, remaining);
                            if (take > 0) {
                                energy.addBuffer(bat, -take);
                                energy.addBuffer(loc, take);
                                remaining -= take;
                            }
                        }
                    }
                }
            }
        }

        // Phase 4: Remaining generator energy goes into batteries
        for (Location bat : batteries) {
            int current = energy.getBuffer(bat);
            if (current >= batMax) continue;

            int canAbsorb = Math.min(batTransferRate, batMax - current);

            for (Location gen : generators) {
                if (canAbsorb <= 0) break;
                int avail = Math.min(energy.getBuffer(gen), genTransferRate);
                int take = Math.min(avail, canAbsorb);
                if (take > 0) {
                    energy.addBuffer(gen, -take);
                    energy.addBuffer(bat, take);
                    canAbsorb -= take;
                }
            }
            for (Location sol : solarPanels) {
                if (canAbsorb <= 0) break;
                int avail = Math.min(energy.getBuffer(sol), solTransferRate);
                int take = Math.min(avail, canAbsorb);
                if (take > 0) {
                    energy.addBuffer(sol, -take);
                    energy.addBuffer(bat, take);
                    canAbsorb -= take;
                }
            }
        }

        // Phase 5: Execute consumers from their own buffers
        for (Location loc : consumers) {
            MachineType type = getMachineType(loc);
            int cost = getEnergyCost(type);
            if (energy.getBuffer(loc) >= cost) {
                boolean didWork = executeConsumer(loc, type);
                if (didWork) {
                    energy.addBuffer(loc, -cost);
                }
            }
        }
        // Phase 6: Charging stations
        for (Location loc : chargingStations) {
            tickChargingStation(loc, energy);
        }
    }

    private MachineType getMachineType(Location loc) {
        return MachineType.valueOf(plugin.getMachineRegistry().getMachineType(loc));
    }

    private int getEnergyCost(MachineType type) {
        return switch (type) {
            case ELECTRIC_FURNACE -> plugin.getConfig().getInt("machines.electric-furnace.energy-cost", 50);
            case MINER -> plugin.getConfig().getInt("machines.miner.energy-cost", 100);
            case MOB_GRINDER -> plugin.getConfig().getInt("machines.mob-grinder.energy-cost", 75);
            case ITEM_SORTER -> plugin.getConfig().getInt("machines.item-sorter.energy-cost", 10);
            case CHARGING_STATION -> plugin.getConfig().getInt("machines.charging-station.charge-rate", 10);
            default -> 0;
        };
    }

    private boolean executeConsumer(Location loc, MachineType type) {
        return switch (type) {
            case MINER -> executeMiner(loc);
            case MOB_GRINDER -> executeMobGrinder(loc);
            case ITEM_SORTER -> executeItemSorter(loc);
            default -> false; // Electric furnace is player triggered
        };
    }

    private void produceGenerator(Location loc, MachineEnergyManager energy, FuelManager fuel) {
        int maxBuffer = plugin.getConfig().getInt("machines.generator.max-buffer", 1000);
        if (energy.getBuffer(loc) >= maxBuffer) return;

        int remaining = fuel.getFuel(loc);
        if (remaining <= 0) return;

        fuel.addFuel(loc, -1);
        int production = plugin.getConfig().getInt("machines.generator.production-per-tick", 10);
        energy.addBuffer(loc, production);

        if (energy.getBuffer(loc) > maxBuffer) {
            energy.setBuffer(loc, maxBuffer);
        }
    }

    private void produceSolar(Location loc, MachineEnergyManager energy) {
        int maxBuffer = plugin.getConfig().getInt("machines.solar-panel.max-buffer", 300);
        if (energy.getBuffer(loc) >= maxBuffer) return;

        World world = loc.getWorld();
        if (world == null) return;

        long time = world.getTime();
        boolean isDaytime = time < 12300 || time > 23850;
        boolean hasSkyAccess = loc.getBlock().getLightFromSky() > 0;

        if (isDaytime && hasSkyAccess) {
            int production = plugin.getConfig().getInt("machines.solar-panel.production-per-tick", 3);
            energy.addBuffer(loc, production);
            if (energy.getBuffer(loc) > maxBuffer) {
                energy.setBuffer(loc, maxBuffer);
            }
        }
    }

    private boolean executeMiner(Location loc) {
        Location below = loc.clone().subtract(0, 1, 0);
        while (below.getBlockY() >= loc.getWorld().getMinHeight()) {
            Block block = below.getBlock();
            if (!block.getChunk().isLoaded()) return false;
            if (block.getType() == Material.AIR ||
                    block.getType() == Material.CAVE_AIR ||
                    block.getType() == Material.WATER ||
                    block.getType() == Material.LAVA) {
                below.subtract(0, 1, 0);
                continue;
            }
            if (block.getType() == Material.BEDROCK) return false;
            if (plugin.getMachineRegistry().isMachine(block.getLocation())) return false;
            block.breakNaturally();
            return true;
        }
        return false;
    }

    private boolean executeMobGrinder(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;
        int radius = plugin.getConfig().getInt("machines.mob-grinder.radius", 8);
        for (Entity entity : world.getNearbyEntities(loc, radius, radius, radius)) {
            if (!(entity instanceof Monster monster)) continue;
            monster.setHealth(0);
            return true;
        }
        return false;
    }

    private boolean executeItemSorter(Location loc) {
        String sourceKey = plugin.getSorterManager().getSource(loc);
        if (sourceKey == null) return false;

        Location sourceLoc = plugin.getNetworkManager().keyToLocation(sourceKey);
        if (sourceLoc == null) return false;

        // Check source chest is still connected
        if (!plugin.getNetworkManager().isChestOnNetwork(sourceLoc, loc)) return false;

        Block sourceBlock = sourceLoc.getBlock();
        if (!(sourceBlock.getState() instanceof Chest sourceChest)) return false;

        Map<String, List<String>> filters = plugin.getSorterManager().getFilters(loc);
        if (filters.isEmpty()) return false;

        Inventory sourceInv = sourceChest.getInventory();

        for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
            Location destLoc = plugin.getNetworkManager().keyToLocation(entry.getKey());
            if (destLoc == null) continue;

            // Check destination chest is still connected
            if (!plugin.getNetworkManager().isChestOnNetwork(destLoc, loc)) continue;

            Block destBlock = destLoc.getBlock();
            if (!(destBlock.getState() instanceof Chest destChest)) continue;

            Inventory destInv = destChest.getInventory();
            List<String> materials = entry.getValue();

            for (int i = 0; i < sourceInv.getSize(); i++) {
                ItemStack item = sourceInv.getItem(i);
                if (item == null || item.getType() == Material.AIR) continue;
                if (!materials.contains(item.getType().name())) continue;

                Map<Integer, ItemStack> leftover = destInv.addItem(item.clone());
                if (leftover.isEmpty()) {
                    sourceInv.setItem(i, null);
                } else {
                    int amountMoved = item.getAmount() - leftover.get(0).getAmount();
                    if (amountMoved > 0) {
                        item.setAmount(item.getAmount() - amountMoved);
                        sourceInv.setItem(i, item);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void tickChargingStation(Location loc, MachineEnergyManager energy) {
        ItemStack item = plugin.getChargingStationManager().getItem(loc);
        if (item == null) return;
        if (!plugin.getChargingStationManager().isChargeable(item)) return;
        if (plugin.getChargingStationManager().isFullyCharged(item)) return;

        int chargeRate = plugin.getConfig().getInt("machines.charging-station.charge-rate", 10);
        int buffer = energy.getBuffer(loc);
        if (buffer < chargeRate) return;

        energy.addBuffer(loc, -chargeRate);
        plugin.getChargingStationManager().addCharge(item, chargeRate);
        plugin.getChargingStationManager().setItem(loc, item);
    }
}