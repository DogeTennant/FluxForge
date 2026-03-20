package me.dogetennant.fluxforge.machines;

import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class JetpackItem {

    private static final NamespacedKey JETPACK_KEY = new NamespacedKey(FluxForge.getInstance(), "jetpack");
    private static final NamespacedKey CHARGE_KEY = new NamespacedKey(FluxForge.getInstance(), "jetpack_charge");
    public static final int MAX_CHARGE = 1500;

    public static ItemStack createJetpack() {
        return createJetpack(0);
    }

    public static ItemStack createJetpack(int charge) {
        ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Flux Jetpack");
        meta.setLore(Arrays.asList(
                FluxForge.getInstance().getLangManager().get("item-lore-jetpack"),
                FluxForge.getInstance().getLangManager().get("item-lore-jetpack-charge",
                        "{charge}", String.valueOf(charge),
                        "{max}", String.valueOf(MAX_CHARGE))
        ));

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        meta.getPersistentDataContainer().set(JETPACK_KEY, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(CHARGE_KEY, PersistentDataType.INTEGER, charge);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isJetpack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(JETPACK_KEY, PersistentDataType.BOOLEAN);
    }

    public static int getCharge(ItemStack item) {
        if (!isJetpack(item)) return 0;
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(CHARGE_KEY, PersistentDataType.INTEGER, 0);
    }

    public static void setCharge(ItemStack item, int charge) {
        if (!isJetpack(item)) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(CHARGE_KEY, PersistentDataType.INTEGER,
                Math.max(0, Math.min(charge, MAX_CHARGE)));
        meta.setLore(Arrays.asList(
                FluxForge.getInstance().getLangManager().get("item-lore-jetpack"),
                FluxForge.getInstance().getLangManager().get("item-lore-jetpack-charge",
                        "{charge}", String.valueOf(Math.max(0, Math.min(charge, MAX_CHARGE))),
                        "{max}", String.valueOf(MAX_CHARGE))
        ));
        item.setItemMeta(meta);
    }
}