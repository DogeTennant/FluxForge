package me.dogetennant.fluxforge.gui;

import me.dogetennant.fluxforge.FluxForge;
import me.dogetennant.fluxforge.utils.SmeltingUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import me.dogetennant.fluxforge.managers.RecipeBook;
import me.dogetennant.fluxforge.machines.MachineType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MachineGuiListener implements Listener {

    private final FluxForge plugin;

    private final Map<UUID, Location> pendingSetSource = new HashMap<>();
    private final Map<UUID, Location> pendingAddDest = new HashMap<>();
    private final Map<UUID, String> pendingAddDestMaterial = new HashMap<>();
    private final Map<UUID, Location> pendingChatMaterial = new HashMap<>();

    public MachineGuiListener(FluxForge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(ChatColor.DARK_AQUA + "")) return;

        Player player = (Player) event.getWhoClicked();
        Location loc = plugin.getGuiManager().getOpenGui(player.getUniqueId());

        // Charging station needs special handling before cancelling
        if (title.equals(ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-charging-station"))) {
            if (event.getClickedInventory() == player.getInventory()) return;
            event.setCancelled(true);
            handleChargingStationClick(player, loc, event);
            return;
        }

        if (title.equals(ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-electric-furnace"))) {
            if (event.getClickedInventory() == player.getInventory()) return;
            event.setCancelled(true);
            if (event.getSlot() == 13) {
                ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR && loc != null) {
                    handleFurnaceSmelt(player, loc, cursor);
                }
            }
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        String itemName = event.getCurrentItem().hasItemMeta()
                ? event.getCurrentItem().getItemMeta().getDisplayName() : "";

        // Handle recipe GUIs first — these don't need a machine location
        if (title.equals(ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-recipes-title"))) {
            handleRecipesClick(player, event.getCurrentItem());
            return;
        }

        if (title.startsWith(ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-recipe-title")
                .replace("{item}", ""))) {
            handleRecipeViewClick(player, itemName);
            return;
        }

        // All other GUIs need a machine location
        if (loc == null) return;

        if (title.equals(ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-item-sorter"))) {
            handleSorterClick(player, loc, itemName);
        } else if (title.equals(ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-sorter-filters"))) {
            handleSorterFiltersClick(player, loc, itemName, event.getCurrentItem());
        } else if (title.equals(ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-miner"))) {
            handleToggleClick(player, loc, itemName, MachineType.MINER);
        } else if (title.equals(ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-mob-grinder"))) {
            handleToggleClick(player, loc, itemName, MachineType.MOB_GRINDER);
        } else if (title.equals(ChatColor.DARK_AQUA + plugin.getLangManager().get("gui-charging-station"))) {
            if (event.getClickedInventory() == player.getInventory()) {
                return; // Allow clicking in player inventory
            }
            handleChargingStationClick(player, loc, event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.getGuiManager().isSwitching(uuid)) return;
        plugin.getGuiManager().clearOpenGui(uuid);
    }

    private void handleFurnaceSmelt(Player player, Location loc, ItemStack cursor) {
        int energyCost = plugin.getConfig().getInt("machines.electric-furnace.energy-cost", 50);
        int currentEnergy = plugin.getMachineEnergyManager().getBuffer(loc);

        ItemStack result = SmeltingUtil.getSmeltingResult(cursor);
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

        int canSmelt = Math.min(cursor.getAmount(), currentEnergy / energyCost);
        plugin.getMachineEnergyManager().addBuffer(loc, -(canSmelt * energyCost));

        cursor.setAmount(cursor.getAmount() - canSmelt);
        result.setAmount(canSmelt);
        player.getInventory().addItem(result);

        player.sendMessage(plugin.getLangManager().get("furnace-smelted",
                "{amount}", String.valueOf(canSmelt),
                "{item}", result.getType().toString().toLowerCase().replace("_", " ")));

        plugin.getGuiManager().markSwitching(player.getUniqueId());
        plugin.getGuiManager().setOpenGui(player.getUniqueId(), loc);
        player.openInventory(MachineGui.openElectricFurnaceGui(loc));
    }

    private void handleSorterClick(Player player, Location sorterLoc, String itemName) {
        if (itemName.equals(plugin.getLangManager().get("gui-sorter-source-chest"))) {
            pendingSetSource.put(player.getUniqueId(), sorterLoc);
            player.closeInventory();
            player.sendMessage(plugin.getLangManager().get("sorter-set-source-prompt"));
        } else if (itemName.equals(plugin.getLangManager().get("gui-sorter-filters-button"))) {
            plugin.getGuiManager().markSwitching(player.getUniqueId());
            plugin.getGuiManager().setOpenGui(player.getUniqueId(), sorterLoc);
            player.openInventory(MachineGui.openSorterFiltersGui(sorterLoc));
        } else if (itemName.equals(plugin.getLangManager().get("gui-sorter-clear"))) {
            plugin.getSorterManager().removeSorter(sorterLoc);
            player.sendMessage(plugin.getLangManager().get("sorter-cleared"));
            plugin.getGuiManager().markSwitching(player.getUniqueId());
            plugin.getGuiManager().setOpenGui(player.getUniqueId(), sorterLoc);
            player.openInventory(MachineGui.openItemSorterGui(sorterLoc));
        }
    }

    private void handleSorterFiltersClick(Player player, Location sorterLoc,
                                          String itemName, ItemStack clicked) {
        if (itemName.equals(plugin.getLangManager().get("gui-sorter-add-filter"))) {
            pendingChatMaterial.put(player.getUniqueId(), sorterLoc);
            player.closeInventory();
            player.sendMessage(plugin.getLangManager().get("sorter-type-material"));
            player.sendMessage(plugin.getLangManager().get("sorter-type-cancel"));
        } else if (itemName.equals(plugin.getLangManager().get("gui-sorter-back"))) {
            plugin.getGuiManager().markSwitching(player.getUniqueId());
            plugin.getGuiManager().setOpenGui(player.getUniqueId(), sorterLoc);
            player.openInventory(MachineGui.openItemSorterGui(sorterLoc));
        } else if (itemName.startsWith(ChatColor.YELLOW + "")) {
            String material = ChatColor.stripColor(itemName);
            List<String> lore = clicked.getItemMeta().getLore();
            if (lore == null || lore.isEmpty()) return;
            String destKey = ChatColor.stripColor(lore.get(0))
                    .replace(ChatColor.stripColor(plugin.getLangManager().get("gui-sorter-filter-dest")
                            .replace("{dest}", "")), "");
            Location destLoc = plugin.getNetworkManager().keyToLocation(destKey);
            if (destLoc == null) return;
            plugin.getSorterManager().removeFilterMaterial(sorterLoc, destLoc, material);
            player.sendMessage(plugin.getLangManager().get("sorter-filter-removed",
                    "{material}", material));
            plugin.getGuiManager().markSwitching(player.getUniqueId());
            plugin.getGuiManager().setOpenGui(player.getUniqueId(), sorterLoc);
            player.openInventory(MachineGui.openSorterFiltersGui(sorterLoc));
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!pendingChatMaterial.containsKey(player.getUniqueId())) return;

        event.setCancelled(true);
        Location sorterLoc = pendingChatMaterial.remove(player.getUniqueId());
        String input = event.getMessage().trim().toUpperCase();

        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(plugin.getLangManager().get("sorter-cancelled"));
            return;
        }

        try {
            Material.valueOf(input);
        } catch (IllegalArgumentException e) {
            player.sendMessage(plugin.getLangManager().get("sorter-unknown-material",
                    "{material}", input));
            return;
        }

        pendingAddDest.put(player.getUniqueId(), sorterLoc);
        pendingAddDestMaterial.put(player.getUniqueId(), input);
        player.sendMessage(plugin.getLangManager().get("sorter-dest-prompt",
                "{material}", input));
    }

    @EventHandler
    public void onChestClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!pendingSetSource.containsKey(uuid) && !pendingAddDest.containsKey(uuid)) return;

        if (event.getClickedBlock() == null ||
                event.getClickedBlock().getType() != Material.CHEST) {
            player.sendMessage(plugin.getLangManager().get("sorter-not-chest"));
            return;
        }

        event.setCancelled(true);
        Location chestLoc = event.getClickedBlock().getLocation();

        if (pendingSetSource.containsKey(uuid)) {
            Location sorterLoc = pendingSetSource.get(uuid);
            if (!plugin.getNetworkManager().isChestOnNetwork(chestLoc, sorterLoc)) {
                player.sendMessage(plugin.getLangManager().get("sorter-chest-not-connected"));
                return;
            }
            pendingSetSource.remove(uuid);
            plugin.getSorterManager().setSource(sorterLoc, chestLoc);
            player.sendMessage(plugin.getLangManager().get("sorter-source-confirmed"));
            plugin.getGuiManager().markSwitching(uuid);
            plugin.getGuiManager().setOpenGui(uuid, sorterLoc);
            player.openInventory(MachineGui.openItemSorterGui(sorterLoc));
        } else if (pendingAddDest.containsKey(uuid)) {
            Location sorterLoc = pendingAddDest.get(uuid);
            if (!plugin.getNetworkManager().isChestOnNetwork(chestLoc, sorterLoc)) {
                player.sendMessage(plugin.getLangManager().get("sorter-chest-not-connected"));
                return;
            }
            pendingAddDest.remove(uuid);
            String material = pendingAddDestMaterial.remove(uuid);
            plugin.getSorterManager().addFilter(sorterLoc, chestLoc, material);
            player.sendMessage(plugin.getLangManager().get("sorter-filter-added",
                    "{material}", material));
            plugin.getGuiManager().markSwitching(uuid);
            plugin.getGuiManager().setOpenGui(uuid, sorterLoc);
            player.openInventory(MachineGui.openSorterFiltersGui(sorterLoc));
        }
    }

    private void handleRecipesClick(Player player, ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // Find the matching recipe by result type and name
        for (RecipeBook.RecipeEntry entry : RecipeBook.getAllRecipes()) {
            if (entry.result.getType() == clicked.getType()) {
                // Check display name matches
                String clickedName = clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()
                        ? clicked.getItemMeta().getDisplayName() : "";
                String resultName = entry.result.hasItemMeta() && entry.result.getItemMeta().hasDisplayName()
                        ? entry.result.getItemMeta().getDisplayName() : "";
                if (ChatColor.stripColor(clickedName).equals(ChatColor.stripColor(resultName))) {
                    plugin.getGuiManager().markSwitching(player.getUniqueId());
                    player.openInventory(MachineGui.openRecipeViewGui(entry));
                    return;
                }
            }
        }
    }

    private void handleRecipeViewClick(Player player, String itemName) {
        if (itemName.equals(plugin.getLangManager().get("gui-recipes-back"))) {
            plugin.getGuiManager().markSwitching(player.getUniqueId());
            player.openInventory(MachineGui.openRecipesGui());
        }
    }

    private void handleToggleClick(Player player, Location loc, String itemName, MachineType type) {
        String enableName = plugin.getLangManager().get("gui-toggle-enable");
        String disableName = plugin.getLangManager().get("gui-toggle-disable");

        if (itemName.equals(enableName) || itemName.equals(disableName)) {
            plugin.getMachineStateManager().toggle(loc);
            boolean nowEnabled = plugin.getMachineStateManager().isEnabled(loc);
            player.sendMessage(nowEnabled
                    ? plugin.getLangManager().get("machine-toggle-on", "{machine}", type.getDisplayName())
                    : plugin.getLangManager().get("machine-toggle-off", "{machine}", type.getDisplayName()));

            // Refresh GUI
            plugin.getGuiManager().markSwitching(player.getUniqueId());
            plugin.getGuiManager().setOpenGui(player.getUniqueId(), loc);
            if (type == MachineType.MINER) {
                player.openInventory(MachineGui.openMinerGui(loc));
            } else {
                player.openInventory(MachineGui.openMobGrinderGui(loc));
            }
        }
    }

    private void handleChargingStationClick(Player player, Location loc, InventoryClickEvent event) {
        int slot = event.getSlot();

        // Slot 13 is the charging slot
        if (slot == 13) {
            ItemStack cursor = event.getCursor();
            ItemStack current = event.getCurrentItem();

            // Player placing an item in
            if (cursor != null && cursor.getType() != Material.AIR) {
                if (!plugin.getChargingStationManager().isChargeable(cursor)) {
                    player.sendMessage(plugin.getLangManager().get("charging-not-chargeable",
                            "{item}", cursor.getType().toString().toLowerCase().replace("_", " ")));
                    return;
                }
                // Remove existing item if any
                if (current != null && current.getType() != Material.AIR &&
                        !current.getType().toString().equals("BARRIER")) {
                    player.getInventory().addItem(current);
                }
                plugin.getChargingStationManager().setItem(loc, cursor.clone());
                cursor.setAmount(0);
                plugin.getGuiManager().markSwitching(player.getUniqueId());
                plugin.getGuiManager().setOpenGui(player.getUniqueId(), loc);
                player.openInventory(MachineGui.openChargingStationGui(loc));
            } else if (current != null && current.getType() != Material.AIR &&
                    !current.getType().toString().equals("BARRIER")) {
                // Player taking item out
                plugin.getChargingStationManager().setItem(loc, null);
                player.getInventory().addItem(
                        plugin.getChargingStationManager().getItem(loc) != null
                                ? plugin.getChargingStationManager().getItem(loc).clone()
                                : current.clone());
                plugin.getGuiManager().markSwitching(player.getUniqueId());
                plugin.getGuiManager().setOpenGui(player.getUniqueId(), loc);
                player.openInventory(MachineGui.openChargingStationGui(loc));
            }
        }
    }
}