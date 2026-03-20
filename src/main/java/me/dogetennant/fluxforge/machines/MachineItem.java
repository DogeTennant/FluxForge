package me.dogetennant.fluxforge.machines;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import me.dogetennant.fluxforge.FluxForge;

import java.util.Arrays;

public class MachineItem {

    private static final NamespacedKey MACHINE_KEY = new NamespacedKey(FluxForge.getInstance(), "machine_type");

    public static ItemStack createMachineItem(MachineType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + type.getDisplayName());
        meta.setLore(Arrays.asList(
                FluxForge.getInstance().getLangManager().get("item-lore-machine"),
                FluxForge.getInstance().getLangManager().get("item-lore-type",
                        "{type}", type.getDisplayName())
        ));

        meta.getPersistentDataContainer().set(MACHINE_KEY, PersistentDataType.STRING, type.name());

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isMachineItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(MACHINE_KEY, PersistentDataType.STRING);
    }

    public static MachineType getMachineType(ItemStack item) {
        if (!isMachineItem(item)) return null;
        String typeName = item.getItemMeta().getPersistentDataContainer().get(MACHINE_KEY, PersistentDataType.STRING);
        try {
            return MachineType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}