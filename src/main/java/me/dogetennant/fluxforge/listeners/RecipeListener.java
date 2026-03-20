package me.dogetennant.fluxforge.listeners;

import me.dogetennant.fluxforge.machines.ComponentItem;
import me.dogetennant.fluxforge.machines.ComponentType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class RecipeListener implements Listener {

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        if (!recipeNeedsComponent(event.getRecipe().getResult())) return;

        for (ItemStack item : event.getInventory().getMatrix()) {
            if (isFakeComponent(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getRecipe() == null) return;
        if (!recipeNeedsComponent(event.getRecipe().getResult())) return;

        for (ItemStack item : event.getInventory().getMatrix()) {
            if (isFakeComponent(item)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // Only block items that have been renamed to look like a component but lack the NBT tag
    private boolean isFakeComponent(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        if (!item.getItemMeta().hasDisplayName()) return false; // vanilla items have no custom name
        if (ComponentItem.isComponent(item)) return false; // properly tagged, it's real

        String displayName = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
        for (ComponentType type : ComponentType.values()) {
            if (displayName.equalsIgnoreCase(type.getDisplayName())) {
                return true; // renamed to look like a component but no NBT tag
            }
        }
        return false;
    }

    private boolean recipeNeedsComponent(ItemStack result) {
        if (result == null) return false;
        return ComponentItem.isComponent(result) ||
                me.dogetennant.fluxforge.machines.MachineItem.isMachineItem(result) ||
                me.dogetennant.fluxforge.machines.WrenchItem.isWrench(result);
    }
}