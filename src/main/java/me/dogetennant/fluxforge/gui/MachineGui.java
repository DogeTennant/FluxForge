package me.dogetennant.fluxforge.gui;

import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import me.dogetennant.fluxforge.managers.RecipeBook;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MachineGui {

    private static final FluxForge plugin = FluxForge.getInstance();

    public static Inventory openGeneratorGui(Location loc) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-generator"));

        int energy = plugin.getMachineEnergyManager().getBuffer(loc);

        int fuel = plugin.getFuelManager().getFuel(loc);
        ItemStack fuelItem = createItem(Material.COAL,
                plugin.getLangManager().get("gui-fuel"),
                plugin.getLangManager().get("gui-fuel-current",
                        "{fuel}", formatFuelTime(fuel)),
                plugin.getLangManager().get("gui-fuel-tip"));
        gui.setItem(11, fuelItem);

        ItemStack energyItem = createItem(Material.GLOWSTONE_DUST,
                plugin.getLangManager().get("gui-network-energy"),
                plugin.getLangManager().get("gui-energy-current", "{energy}", String.valueOf(energy)),
                plugin.getLangManager().get("gui-energy-max", "{max}", "500"));
        gui.setItem(13, energyItem);

        fillEmpty(gui);
        return gui;
    }

    public static Inventory openSolarPanelGui(Location loc) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-solar-panel"));

        int energy = plugin.getMachineEnergyManager().getBuffer(loc);

        ItemStack energyItem = createItem(Material.GLOWSTONE_DUST,
                plugin.getLangManager().get("gui-network-energy"),
                plugin.getLangManager().get("gui-energy-current", "{energy}", String.valueOf(energy)),
                plugin.getLangManager().get("gui-energy-max", "{max}", "500"),
                plugin.getLangManager().get("gui-energy-solar-tip"));
        gui.setItem(13, energyItem);

        fillEmpty(gui);
        return gui;
    }

    public static Inventory openBatteryGui(Location loc) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-battery"));

        int energy = plugin.getMachineEnergyManager().getBuffer(loc);
        int maxEnergy = plugin.getConfig().getInt("machines.battery.max-buffer", 5000);
        int filled = (int) ((energy / (double) maxEnergy) * 7);

        for (int i = 0; i < 7; i++) {
            Material mat = i < filled ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
            gui.setItem(10 + i, createItem(mat,
                    plugin.getLangManager().get("gui-energy-bar"), ""));
        }

        ItemStack energyItem = createItem(Material.GLOWSTONE_DUST,
                plugin.getLangManager().get("gui-network-energy"),
                plugin.getLangManager().get("gui-energy-current", "{energy}", String.valueOf(energy)),
                plugin.getLangManager().get("gui-energy-max", "{max}", String.valueOf(maxEnergy)));
        gui.setItem(22, energyItem);

        fillEmpty(gui);
        return gui;
    }

    public static Inventory openElectricFurnaceGui(Location loc) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-electric-furnace"));

        int energy = plugin.getMachineEnergyManager().getBuffer(loc);
        int energyCost = plugin.getConfig().getInt("machines.electric-furnace.energy-cost", 20);

        ItemStack energyItem = createItem(Material.GLOWSTONE_DUST,
                plugin.getLangManager().get("gui-network-energy"),
                plugin.getLangManager().get("gui-energy-current", "{energy}", String.valueOf(energy)),
                plugin.getLangManager().get("gui-smelt-cost", "{cost}", String.valueOf(energyCost)));
        gui.setItem(11, energyItem);

        ItemStack smeltItem = createItem(Material.FURNACE,
                plugin.getLangManager().get("gui-smelt-button"),
                plugin.getLangManager().get("gui-smelt-tip1"),
                plugin.getLangManager().get("gui-smelt-tip2"));
        gui.setItem(13, smeltItem);

        fillEmpty(gui);
        return gui;
    }

    public static Inventory openMinerGui(Location loc) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-miner"));

        int energy = plugin.getMachineEnergyManager().getBuffer(loc);
        int energyCost = plugin.getConfig().getInt("machines.miner.energy-cost", 100);
        boolean enabled = plugin.getMachineStateManager().isEnabled(loc);

        Location below = loc.clone().subtract(0, 1, 0);
        Material nextBlock = null;
        int nextY = 0;
        while (below.getBlockY() >= loc.getWorld().getMinHeight()) {
            Material type = below.getBlock().getType();
            if (type != Material.AIR && type != Material.CAVE_AIR &&
                    type != Material.WATER && type != Material.LAVA &&
                    type != Material.BEDROCK) {
                nextBlock = type;
                nextY = below.getBlockY();
                break;
            }
            below.subtract(0, 1, 0);
        }

        ItemStack energyItem = createItem(Material.GLOWSTONE_DUST,
                plugin.getLangManager().get("gui-network-energy"),
                plugin.getLangManager().get("gui-energy-current", "{energy}", String.valueOf(energy)),
                plugin.getLangManager().get("gui-miner-cost", "{cost}", String.valueOf(energyCost)));
        gui.setItem(11, energyItem);

        String blockName = nextBlock != null
                ? nextBlock.toString().toLowerCase().replace("_", " ") + " at Y=" + nextY
                : plugin.getLangManager().get("miner-nothing");
        ItemStack targetItem = createItem(
                nextBlock != null ? nextBlock : Material.BARRIER,
                plugin.getLangManager().get("gui-miner-target"),
                plugin.getLangManager().get("gui-miner-block", "{block}", blockName));
        gui.setItem(13, targetItem);

        // Toggle button
        ItemStack toggleItem = createItem(
                enabled ? Material.RED_DYE : Material.LIME_DYE,
                enabled ? plugin.getLangManager().get("gui-toggle-disable")
                        : plugin.getLangManager().get("gui-toggle-enable"),
                enabled ? plugin.getLangManager().get("gui-toggle-status-on")
                        : plugin.getLangManager().get("gui-toggle-status-off"),
                plugin.getLangManager().get("gui-toggle-tip"));
        gui.setItem(15, toggleItem);

        fillEmpty(gui);
        return gui;
    }

    public static Inventory openMobGrinderGui(Location loc) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-mob-grinder"));

        int energy = plugin.getMachineEnergyManager().getBuffer(loc);
        int energyCost = plugin.getConfig().getInt("machines.mob-grinder.energy-cost", 75);
        int radius = plugin.getConfig().getInt("machines.mob-grinder.radius", 8);
        boolean enabled = plugin.getMachineStateManager().isEnabled(loc);

        int nearbyMobs = 0;
        for (org.bukkit.entity.Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof org.bukkit.entity.Monster) nearbyMobs++;
        }

        ItemStack energyItem = createItem(Material.GLOWSTONE_DUST,
                plugin.getLangManager().get("gui-network-energy"),
                plugin.getLangManager().get("gui-energy-current", "{energy}", String.valueOf(energy)),
                plugin.getLangManager().get("gui-mobs-cost", "{cost}", String.valueOf(energyCost)));
        gui.setItem(11, energyItem);

        ItemStack mobItem = createItem(Material.ROTTEN_FLESH,
                plugin.getLangManager().get("gui-mobs-nearby"),
                plugin.getLangManager().get("gui-mobs-count", "{count}", String.valueOf(nearbyMobs)),
                plugin.getLangManager().get("gui-mobs-radius", "{radius}", String.valueOf(radius)));
        gui.setItem(13, mobItem);

        // Toggle button
        ItemStack toggleItem = createItem(
                enabled ? Material.RED_DYE : Material.LIME_DYE,
                enabled ? plugin.getLangManager().get("gui-toggle-disable")
                        : plugin.getLangManager().get("gui-toggle-enable"),
                enabled ? plugin.getLangManager().get("gui-toggle-status-on")
                        : plugin.getLangManager().get("gui-toggle-status-off"),
                plugin.getLangManager().get("gui-toggle-tip"));
        gui.setItem(15, toggleItem);

        fillEmpty(gui);
        return gui;
    }

    public static Inventory openConduitGui(Location loc) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-conduit"));

        int energy = plugin.getMachineEnergyManager().getBuffer(loc);
        String netId = plugin.getNetworkManager().getNetworkId(loc);
        String netIdShort = netId != null ? netId.substring(0, 8) + "..." : "None";

        ItemStack energyItem = createItem(Material.GLOWSTONE_DUST,
                plugin.getLangManager().get("gui-network-energy"),
                plugin.getLangManager().get("gui-energy-current", "{energy}", String.valueOf(energy)),
                plugin.getLangManager().get("gui-conduit-network-id", "{id}", netIdShort));
        gui.setItem(13, energyItem);

        fillEmpty(gui);
        return gui;
    }

    public static Inventory openItemSorterGui(Location loc) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-item-sorter"));

        int energy = plugin.getMachineEnergyManager().getBuffer(loc);
        String source = plugin.getSorterManager().getSource(loc);
        int filterCount = plugin.getSorterManager().getFilters(loc).size();

        ItemStack energyItem = createItem(Material.GLOWSTONE_DUST,
                plugin.getLangManager().get("gui-network-energy"),
                plugin.getLangManager().get("gui-energy-current", "{energy}", String.valueOf(energy)));
        gui.setItem(10, energyItem);

        ItemStack sourceItem = createItem(
                source != null ? Material.CHEST : Material.BARRIER,
                plugin.getLangManager().get("gui-sorter-source-chest"),
                source != null
                        ? plugin.getLangManager().get("gui-sorter-source-set")
                        : plugin.getLangManager().get("gui-sorter-source-not-set"),
                plugin.getLangManager().get("gui-sorter-source-tip"));
        gui.setItem(12, sourceItem);

        ItemStack filterItem = createItem(Material.HOPPER,
                plugin.getLangManager().get("gui-sorter-filters-button"),
                plugin.getLangManager().get("gui-sorter-filters-count",
                        "{count}", String.valueOf(filterCount)),
                plugin.getLangManager().get("gui-sorter-filters-tip"));
        gui.setItem(14, filterItem);

        ItemStack clearItem = createItem(Material.BARRIER,
                plugin.getLangManager().get("gui-sorter-clear"),
                plugin.getLangManager().get("gui-sorter-clear-tip"));
        gui.setItem(16, clearItem);

        fillEmpty(gui);
        return gui;
    }

    public static Inventory openSorterFiltersGui(Location loc) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-sorter-filters"));

        Map<String, List<String>> filters = plugin.getSorterManager().getFilters(loc);

        ItemStack addItem = createItem(Material.LIME_DYE,
                plugin.getLangManager().get("gui-sorter-add-filter"),
                plugin.getLangManager().get("gui-sorter-add-filter-tip1"),
                plugin.getLangManager().get("gui-sorter-add-filter-tip2"));
        gui.setItem(2, addItem);

        ItemStack backItem = createItem(Material.ARROW,
                plugin.getLangManager().get("gui-sorter-back"),
                plugin.getLangManager().get("gui-sorter-back-tip"));
        gui.setItem(8, backItem);

        int slot = 9;
        for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
            for (String material : entry.getValue()) {
                if (slot >= 27) break;
                try {
                    Material mat = Material.valueOf(material);
                    ItemStack filterItem = createItem(mat,
                            ChatColor.YELLOW + material,
                            plugin.getLangManager().get("gui-sorter-filter-dest",
                                    "{dest}", entry.getKey()),
                            plugin.getLangManager().get("gui-sorter-filter-remove"));
                    gui.setItem(slot++, filterItem);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        fillEmpty(gui);
        return gui;
    }

    public static Inventory openRecipesGui() {
        Inventory gui = Bukkit.createInventory(null, 54,
                ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-recipes-title"));

        List<RecipeBook.RecipeEntry> recipes = RecipeBook.getAllRecipes();

        for (int i = 0; i < recipes.size() && i < 54; i++) {
            RecipeBook.RecipeEntry entry = recipes.get(i);
            ItemStack display = entry.result.clone();
            // Add click tip to lore
            ItemMeta meta = display.getItemMeta();
            List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add(plugin.getLangManager().get("gui-recipe-click-tip"));
            meta.setLore(lore);
            display.setItemMeta(meta);
            gui.setItem(i, display);
        }

        fillEmpty(gui);
        return gui;
    }

    public static Inventory openRecipeViewGui(RecipeBook.RecipeEntry entry) {
        String resultName = entry.result.hasItemMeta() && entry.result.getItemMeta().hasDisplayName()
                ? ChatColor.stripColor(entry.result.getItemMeta().getDisplayName())
                : entry.result.getType().toString();

        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-recipe-title",
                        "{item}", resultName));

        // Crafting grid slots: 0-8 map to GUI slots 1,2,3,10,11,12,19,20,21
        int[] gridSlots = {1, 2, 3, 10, 11, 12, 19, 20, 21};
        for (int i = 0; i < 9; i++) {
            if (entry.grid[i] != null) {
                gui.setItem(gridSlots[i], entry.grid[i]);
            }
        }

        // Arrow at slot 14
        gui.setItem(14, createArrowHead());

        // Result at slot 16
        ItemStack result = entry.result.clone();
        ItemMeta meta = result.getItemMeta();
        List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(0, plugin.getLangManager().get("gui-recipe-result"));
        meta.setLore(lore);
        result.setItemMeta(meta);
        gui.setItem(16, result);

        // Back button at slot 26
        gui.setItem(26, createItem(Material.ARROW,
                plugin.getLangManager().get("gui-recipes-back"),
                plugin.getLangManager().get("gui-recipes-back-tip")));

        fillEmpty(gui);
        return gui;
    }

    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private static void fillEmpty(Inventory gui) {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }

    private static ItemStack createArrowHead() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        org.bukkit.profile.PlayerProfile profile = Bukkit.createPlayerProfile(java.util.UUID.randomUUID());
        org.bukkit.profile.PlayerTextures textures = profile.getTextures();

        try {
            java.net.URL url = new java.net.URL("https://textures.minecraft.net/texture/18660691d1ca029f120a3ff0eabab93a2306b37a7d61119fcd141ff2f6fcd798");
            textures.setSkin(url);
        } catch (java.net.MalformedURLException e) {
            e.printStackTrace();
        }

        profile.setTextures(textures);
        meta.setOwnerProfile(profile);
        meta.setDisplayName(" ");
        skull.setItemMeta(meta);
        return skull;
    }

    public static Inventory openChargingStationGui(Location loc) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-charging-station"));

        int networkEnergy = plugin.getMachineEnergyManager().getBuffer(loc);
        int chargeRate = plugin.getConfig().getInt("machines.charging-station.charge-rate", 10);
        ItemStack charging = plugin.getChargingStationManager().getItem(loc);

        ItemStack networkItem = createItem(Material.GLOWSTONE_DUST,
                plugin.getLangManager().get("gui-network-energy"),
                plugin.getLangManager().get("gui-charging-network",
                        "{energy}", String.valueOf(networkEnergy)));
        gui.setItem(11, networkItem);

        if (charging != null && plugin.getChargingStationManager().isChargeable(charging)) {
            int current = plugin.getChargingStationManager().getCurrentCharge(charging);
            int max = plugin.getChargingStationManager().getMaxCharge(charging);

            // Progress bar
            int filled = (int) ((current / (double) max) * 7);
            for (int i = 0; i < 7; i++) {
                Material mat = i < filled ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
                gui.setItem(9 + i, createItem(mat, " "));
            }

            ItemStack progressItem = createItem(Material.COMPARATOR,
                    plugin.getLangManager().get("gui-charging-progress"),
                    plugin.getLangManager().get("gui-charging-current",
                            "{charge}", String.valueOf(current),
                            "{max}", String.valueOf(max)),
                    plugin.getLangManager().get("gui-charging-rate",
                            "{rate}", String.valueOf(chargeRate)));
            gui.setItem(20, progressItem);

            gui.setItem(13, charging.clone());
        } else {
            ItemStack slotItem = createItem(Material.BARRIER,
                    plugin.getLangManager().get("gui-charging-slot"),
                    plugin.getLangManager().get("gui-charging-slot-tip"),
                    plugin.getLangManager().get("gui-charging-empty"));
            gui.setItem(13, slotItem);
        }

        fillEmpty(gui);
        return gui;
    }

    private static String formatFuelTime(int ticks) {
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
}