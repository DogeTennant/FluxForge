package me.dogetennant.fluxforge.machines;

import org.bukkit.Material;

public enum ComponentType {

    // Tier 1
    COPPER_PLATE("Copper Plate", Material.COPPER_INGOT, 1),
    IRON_PLATE("Iron Plate", Material.IRON_INGOT, 1),
    BASIC_CIRCUIT("Basic Circuit", Material.COMPARATOR, 1),

    // Tier 2
    GOLD_PLATE("Gold Plate", Material.GOLD_INGOT, 2),
    ADVANCED_CIRCUIT("Advanced Circuit", Material.REPEATER, 2),
    MACHINE_FRAME("Machine Frame", Material.IRON_BARS, 2),
    ENERGY_CORE("Energy Core", Material.NETHER_STAR, 2),
    FLUX_COIL("Flux Coil", Material.COPPER_BLOCK, 2),

    // Tier 3
    FLUX_CRYSTAL("Flux Crystal", Material.PRISMARINE_CRYSTALS, 3),
    ADVANCED_MACHINE_FRAME("Advanced Machine Frame", Material.HEAVY_CORE, 3),
    ENERGY_CELL("Energy Cell", Material.AMETHYST_SHARD, 3);

    private final String displayName;
    private final Material material;
    private final int tier;

    ComponentType(String displayName, Material material, int tier) {
        this.displayName = displayName;
        this.material = material;
        this.tier = tier;
    }

    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public int getTier() { return tier; }
}