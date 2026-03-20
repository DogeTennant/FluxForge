package me.dogetennant.fluxforge.machines;

import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class ComponentItem {

    private static final NamespacedKey COMPONENT_KEY = new NamespacedKey(FluxForge.getInstance(), "component_type");

    public static ItemStack createComponent(ComponentType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();

        ChatColor tierColor = switch (type.getTier()) {
            case 1 -> ChatColor.WHITE;
            case 2 -> ChatColor.AQUA;
            case 3 -> ChatColor.LIGHT_PURPLE;
            default -> ChatColor.GRAY;
        };

        meta.setDisplayName(tierColor + type.getDisplayName());
        meta.setLore(Arrays.asList(
                FluxForge.getInstance().getLangManager().get("item-lore-component"),
                FluxForge.getInstance().getLangManager().get("item-lore-tier",
                        "{tier}", String.valueOf(type.getTier()))
        ));

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        meta.getPersistentDataContainer().set(COMPONENT_KEY, PersistentDataType.STRING, type.name());

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isComponent(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(COMPONENT_KEY, PersistentDataType.STRING);
    }

    public static ComponentType getComponentType(ItemStack item) {
        if (!isComponent(item)) return null;
        String typeName = item.getItemMeta().getPersistentDataContainer().get(COMPONENT_KEY, PersistentDataType.STRING);
        try {
            return ComponentType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}