package me.dogetennant.fluxforge.machines;

import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class WrenchItem {

    private static final NamespacedKey WRENCH_KEY = new NamespacedKey(FluxForge.getInstance(), "wrench");

    public static ItemStack createWrench() {
        ItemStack item = new ItemStack(Material.GOLDEN_SHOVEL);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "Flux Wrench");
        meta.setLore(Arrays.asList(
                FluxForge.getInstance().getLangManager().get("item-lore-wrench"),
                FluxForge.getInstance().getLangManager().get("item-lore-wrench-tip")
        ));

        meta.getPersistentDataContainer().set(WRENCH_KEY, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isWrench(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(WRENCH_KEY, PersistentDataType.BOOLEAN);
    }
}