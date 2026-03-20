package me.dogetennant.fluxforge.listeners;

import me.dogetennant.fluxforge.FluxForge;
import me.dogetennant.fluxforge.machines.MachineItem;
import me.dogetennant.fluxforge.machines.MachineType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import me.dogetennant.fluxforge.gui.MachineGui;
import me.dogetennant.fluxforge.machines.WrenchItem;
import me.dogetennant.fluxforge.utils.SmeltingUtil;

import java.util.List;
import java.util.Map;

public class MachineListener implements Listener {

    private final FluxForge plugin;

    public MachineListener(FluxForge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Location loc = event.getBlock().getLocation();

        if (MachineItem.isMachineItem(item)) {
            MachineType type = MachineItem.getMachineType(item);
            if (type == null) return;

            plugin.getMachineRegistry().registerMachine(loc, type.name());
            plugin.getNetworkManager().onBlockAdded(loc);

            Player player = event.getPlayer();
            player.sendMessage(plugin.getLangManager().get("machine-placed",
                    "{machine}", type.getDisplayName()));
        } else if (event.getBlock().getType() == Material.COPPER_GRATE) {
            plugin.getNetworkManager().onBlockAdded(loc);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();

        if (event.getBlock().getType() == Material.COPPER_GRATE &&
                !plugin.getMachineRegistry().isMachine(loc)) {
            plugin.getNetworkManager().onBlockRemoved(loc);
            return;
        }

        if (!plugin.getMachineRegistry().isMachine(loc)) return;

        String typeName = plugin.getMachineRegistry().getMachineType(loc);
        MachineType type = MachineType.valueOf(typeName);

        event.setDropItems(false);

        Location dropLoc = loc.clone().add(0.5, 0.5, 0.5);
        loc.getWorld().dropItemNaturally(dropLoc, MachineItem.createMachineItem(type));

        plugin.getNetworkManager().onBlockRemoved(loc);
        plugin.getMachineRegistry().unregisterMachine(loc);
        plugin.getFuelManager().removeFuel(loc);
        plugin.getMachineStateManager().removeState(loc);

        Player player = event.getPlayer();
        player.sendMessage(plugin.getLangManager().get("machine-removed",
                "{machine}", type.getDisplayName()));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // Allow shift+right-click to place blocks against conduits
        if (event.getPlayer().isSneaking()) return;

        Location loc = block.getLocation();
        if (!plugin.getMachineRegistry().isMachine(loc)) return;

        event.setCancelled(true);

        String typeName = plugin.getMachineRegistry().getMachineType(loc);
        MachineType type = MachineType.valueOf(typeName);
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (WrenchItem.isWrench(hand)) {
            openGui(player, loc, type);
            return;
        }

        switch (type) {
            case GENERATOR -> handleGenerator(player, loc, hand);
            case ELECTRIC_FURNACE -> handleElectricFurnace(player, loc, hand);
            case BATTERY -> handleBattery(player, loc, hand);
            case MINER -> handleMiner(player, loc);
            case MOB_GRINDER -> handleMobGrinder(player, loc);
            case ITEM_SORTER -> handleItemSorter(player, loc);
            case SOLAR_PANEL, CONDUIT -> showEnergyInfo(player, loc, type);
            case CHARGING_STATION -> handleChargingStation(player, loc);
        }
    }

    private void openGui(Player player, Location loc, MachineType type) {
        plugin.getGuiManager().setOpenGui(player.getUniqueId(), loc);
        switch (type) {
            case GENERATOR -> player.openInventory(MachineGui.openGeneratorGui(loc));
            case SOLAR_PANEL -> player.openInventory(MachineGui.openSolarPanelGui(loc));
            case BATTERY -> player.openInventory(MachineGui.openBatteryGui(loc));
            case ELECTRIC_FURNACE -> player.openInventory(MachineGui.openElectricFurnaceGui(loc));
            case MINER -> player.openInventory(MachineGui.openMinerGui(loc));
            case MOB_GRINDER -> player.openInventory(MachineGui.openMobGrinderGui(loc));
            case ITEM_SORTER -> player.openInventory(MachineGui.openItemSorterGui(loc));
            case CONDUIT -> player.openInventory(MachineGui.openConduitGui(loc));
            case CHARGING_STATION -> player.openInventory(MachineGui.openChargingStationGui(loc));
        }
    }

    private void handleGenerator(Player player, Location loc, ItemStack hand) {
        if (hand != null && isFuel(hand.getType())) {
            int amount = hand.getAmount();
            int ticks = getFuelTicks(hand.getType()) * amount;
            plugin.getFuelManager().addFuel(loc, ticks);
            hand.setAmount(0);
            player.sendMessage(plugin.getLangManager().get("generator-fuel-added",
                    "{amount}", String.valueOf(amount)));
            player.sendMessage(plugin.getLangManager().get("generator-fuel-current",
                    "{fuel}", formatFuelTime(plugin.getFuelManager().getFuel(loc))));
        } else {
            int fuel = plugin.getFuelManager().getFuel(loc);
            int energy = plugin.getMachineEnergyManager().getBuffer(loc);
            player.sendMessage(plugin.getLangManager().get("generator-status"));
            player.sendMessage(plugin.getLangManager().get("generator-fuel",
                    "{fuel}", formatFuelTime(fuel)));
            player.sendMessage(plugin.getLangManager().get("generator-energy",
                    "{energy}", String.valueOf(energy)));
            player.sendMessage(plugin.getLangManager().get("generator-tip"));
        }
    }

    private void showEnergyInfo(Player player, Location loc, MachineType type) {
        switch (type) {
            case SOLAR_PANEL -> {
                player.sendMessage(plugin.getLangManager().get("solar-status"));
                player.sendMessage(plugin.getLangManager().get("network-energy",
                        "{energy}", String.valueOf(plugin.getMachineEnergyManager().getBuffer(loc))));
            }
            case CONDUIT -> {
                player.sendMessage(plugin.getLangManager().get("conduit-status"));
                player.sendMessage(plugin.getLangManager().get("network-energy",
                        "{energy}", String.valueOf(plugin.getNetworkManager().getNetworkAvailableEnergy(loc))));
            }
            case CHARGING_STATION -> {
                ItemStack item = plugin.getChargingStationManager().getItem(loc);
                player.sendMessage(ChatColor.AQUA + "[FluxForge] " + ChatColor.WHITE + "Charging Station:");
                player.sendMessage(ChatColor.GRAY + "  Network Energy: " + ChatColor.GREEN +
                        plugin.getNetworkManager().getNetworkAvailableEnergy(loc) + " FE");
                if (item != null && plugin.getChargingStationManager().isChargeable(item)) {
                    int current = plugin.getChargingStationManager().getCurrentCharge(item);
                    int max = plugin.getChargingStationManager().getMaxCharge(item);
                    player.sendMessage(ChatColor.GRAY + "  Item Charge: " + ChatColor.GREEN +
                            current + "/" + max + " FE");
                } else {
                    player.sendMessage(ChatColor.GRAY + "  No item inserted.");
                }
            }
            default -> player.sendMessage(plugin.getLangManager().get("network-energy",
                    "{energy}", String.valueOf(plugin.getMachineEnergyManager().getBuffer(loc))));
        }
    }

    private void handleElectricFurnace(Player player, Location loc, ItemStack hand) {
        int energyCost = plugin.getConfig().getInt("machines.electric-furnace.energy-cost", 50);
        int currentEnergy = plugin.getMachineEnergyManager().getBuffer(loc);

        if (hand == null || hand.getType() == Material.AIR) {
            player.sendMessage(plugin.getLangManager().get("furnace-status"));
            player.sendMessage(plugin.getLangManager().get("furnace-energy",
                    "{energy}", String.valueOf(currentEnergy)));
            player.sendMessage(plugin.getLangManager().get("furnace-cost",
                    "{cost}", String.valueOf(energyCost)));
            player.sendMessage(plugin.getLangManager().get("furnace-tip"));
            return;
        }

        ItemStack result = SmeltingUtil.getSmeltingResult(hand);
        if (result == null) {
            player.sendMessage(plugin.getLangManager().get("furnace-not-smeltable"));
            return;
        }

        if (currentEnergy < energyCost) {
            player.sendMessage(plugin.getLangManager().get("furnace-not-enough-energy",
                    "{cost}", String.valueOf(energyCost),
                    "{energy}", String.valueOf(currentEnergy)));
            return;
        }

        int canSmelt = Math.min(hand.getAmount(), currentEnergy / energyCost);
        plugin.getMachineEnergyManager().addBuffer(loc, -(canSmelt * energyCost));

        hand.setAmount(hand.getAmount() - canSmelt);
        result.setAmount(canSmelt);
        player.getInventory().addItem(result);

        player.sendMessage(plugin.getLangManager().get("furnace-smelted",
                "{amount}", String.valueOf(canSmelt),
                "{item}", result.getType().toString().toLowerCase().replace("_", " ")));
        player.sendMessage(plugin.getLangManager().get("furnace-energy-remaining",
                "{energy}", String.valueOf(plugin.getMachineEnergyManager().getBuffer(loc))));
    }

    private void handleBattery(Player player, Location loc, ItemStack hand) {
        int currentEnergy = plugin.getMachineEnergyManager().getBuffer(loc);
        int maxEnergy = plugin.getConfig().getInt("machines.battery.max-buffer", 5000);

        player.sendMessage(plugin.getLangManager().get("battery-status"));
        player.sendMessage(plugin.getLangManager().get("battery-energy",
                "{energy}", String.valueOf(currentEnergy),
                "{max}", String.valueOf(maxEnergy)));

        int filled = (int) ((currentEnergy / (double) maxEnergy) * 20);
        String bar = ChatColor.GREEN + "|".repeat(filled) + ChatColor.GRAY + "|".repeat(20 - filled);
        player.sendMessage(ChatColor.GRAY + "  [" + bar + ChatColor.GRAY + "]");
    }

    private void handleMiner(Player player, Location loc) {
        int energy = plugin.getMachineEnergyManager().getBuffer(loc);
        int energyCost = plugin.getConfig().getInt("machines.miner.energy-cost", 100);

        Location below = loc.clone().subtract(0, 1, 0);
        Block nextBlock = null;
        while (below.getBlockY() >= loc.getWorld().getMinHeight()) {
            Block block = below.getBlock();
            if (block.getType() != Material.AIR &&
                    block.getType() != Material.CAVE_AIR &&
                    block.getType() != Material.WATER &&
                    block.getType() != Material.LAVA &&
                    block.getType() != Material.BEDROCK) {
                nextBlock = block;
                break;
            }
            below.subtract(0, 1, 0);
        }

        player.sendMessage(plugin.getLangManager().get("miner-status"));
        player.sendMessage(plugin.getLangManager().get("miner-energy",
                "{energy}", String.valueOf(energy)));
        player.sendMessage(plugin.getLangManager().get("miner-cost",
                "{cost}", String.valueOf(energyCost)));
        if (nextBlock != null) {
            player.sendMessage(plugin.getLangManager().get("miner-next-block",
                    "{block}", nextBlock.getType().toString().toLowerCase().replace("_", " "),
                    "{y}", String.valueOf(nextBlock.getY())));
        } else {
            player.sendMessage(plugin.getLangManager().get("miner-nothing"));
        }
    }

    private void handleMobGrinder(Player player, Location loc) {
        int energy = plugin.getMachineEnergyManager().getBuffer(loc);
        int energyCost = plugin.getConfig().getInt("machines.mob-grinder.energy-cost", 75);
        int radius = plugin.getConfig().getInt("machines.mob-grinder.radius", 8);

        int nearbyMobs = 0;
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof Monster) nearbyMobs++;
        }

        player.sendMessage(plugin.getLangManager().get("grinder-status"));
        player.sendMessage(plugin.getLangManager().get("grinder-energy",
                "{energy}", String.valueOf(energy)));
        player.sendMessage(plugin.getLangManager().get("grinder-cost",
                "{cost}", String.valueOf(energyCost)));
        player.sendMessage(plugin.getLangManager().get("grinder-radius",
                "{radius}", String.valueOf(radius)));
        player.sendMessage(plugin.getLangManager().get("grinder-mobs",
                "{count}", String.valueOf(nearbyMobs)));
    }

    private void handleItemSorter(Player player, Location loc) {
        int energy = plugin.getMachineEnergyManager().getBuffer(loc);
        Map<String, List<String>> filters = plugin.getSorterManager().getFilters(loc);
        String source = plugin.getSorterManager().getSource(loc);

        player.sendMessage(plugin.getLangManager().get("sorter-status"));
        player.sendMessage(plugin.getLangManager().get("sorter-energy",
                "{energy}", String.valueOf(energy)));
        player.sendMessage(source != null
                ? plugin.getLangManager().get("sorter-source-set")
                : plugin.getLangManager().get("sorter-source-not-set"));
        player.sendMessage(plugin.getLangManager().get("sorter-filters",
                "{count}", String.valueOf(filters.size())));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> blocks = event.blockList();
        for (Block block : blocks) {
            Location loc = block.getLocation();

            if (block.getType() == Material.COPPER_GRATE && !plugin.getMachineRegistry().isMachine(loc)) {
                plugin.getNetworkManager().onBlockRemoved(loc);
                continue;
            }

            if (!plugin.getMachineRegistry().isMachine(loc)) continue;

            String typeName = plugin.getMachineRegistry().getMachineType(loc);
            MachineType type = MachineType.valueOf(typeName);

            loc.getWorld().dropItemNaturally(loc, MachineItem.createMachineItem(type));
            plugin.getNetworkManager().onBlockRemoved(loc);
            plugin.getMachineRegistry().unregisterMachine(loc);
            plugin.getFuelManager().removeFuel(loc);
            plugin.getMachineStateManager().removeState(loc);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        List<Block> blocks = event.blockList();
        for (Block block : blocks) {
            Location loc = block.getLocation();

            if (block.getType() == Material.COPPER_GRATE && !plugin.getMachineRegistry().isMachine(loc)) {
                plugin.getNetworkManager().onBlockRemoved(loc);
                continue;
            }

            if (!plugin.getMachineRegistry().isMachine(loc)) continue;

            String typeName = plugin.getMachineRegistry().getMachineType(loc);
            MachineType type = MachineType.valueOf(typeName);

            loc.getWorld().dropItemNaturally(loc, MachineItem.createMachineItem(type));
            plugin.getNetworkManager().onBlockRemoved(loc);
            plugin.getMachineRegistry().unregisterMachine(loc);
            plugin.getFuelManager().removeFuel(loc);
            plugin.getMachineStateManager().removeState(loc);
        }
    }

    private boolean isFuel(Material material) {
        return material == Material.COAL ||
                material == Material.CHARCOAL ||
                material == Material.COAL_BLOCK;
    }

    private int getFuelTicks(Material material) {
        int coalTicks = plugin.getConfig().getInt("machines.generator.coal-ticks", 80);
        return switch (material) {
            case COAL -> coalTicks;
            case CHARCOAL -> coalTicks / 2;
            case COAL_BLOCK -> coalTicks * 9;
            default -> 0;
        };
    }

    private String formatFuelTime(int ticks) {
        int seconds = ticks % 60;
        int minutes = (ticks / 60) % 60;
        int hours = ticks / 3600;

        if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    private void handleChargingStation(Player player, Location loc) {
        ItemStack item = plugin.getChargingStationManager().getItem(loc);
        int buffer = plugin.getMachineEnergyManager().getBuffer(loc);
        player.sendMessage(plugin.getLangManager().get("charging-station-status"));
        player.sendMessage(plugin.getLangManager().get("network-energy",
                "{energy}", String.valueOf(buffer)));
        if (item != null && plugin.getChargingStationManager().isChargeable(item)) {
            int current = plugin.getChargingStationManager().getCurrentCharge(item);
            int max = plugin.getChargingStationManager().getMaxCharge(item);
            player.sendMessage(plugin.getLangManager().get("charging-station-item-charge",
                    "{charge}", String.valueOf(current),
                    "{max}", String.valueOf(max)));
        } else {
            player.sendMessage(plugin.getLangManager().get("charging-station-no-item"));
        }
    }
}