package me.dogetennant.fluxforge.machines;

import org.bukkit.Material;

public enum MachineType {

    GENERATOR("Generator", Material.FURNACE, 50, 0),
    SOLAR_PANEL("Solar Panel", Material.DAYLIGHT_DETECTOR, 10, 0),
    BATTERY("Battery", Material.IRON_BLOCK, 0, 500),
    ELECTRIC_FURNACE("Electric Furnace", Material.BLAST_FURNACE, 20, 0),
    MINER("Miner", Material.PISTON, 30, 0),
    MOB_GRINDER("Mob Grinder", Material.SPAWNER, 25, 0),
    ITEM_SORTER("Item Sorter", Material.HOPPER, 5, 0),
    CONDUIT("Conduit", Material.COPPER_GRATE, 0, 0),
    CHARGING_STATION("Charging Station", Material.LODESTONE, 0, 0);


    private final String displayName;
    private final Material material;
    private final int energyCost;
    private final int energyStorage;

    MachineType(String displayName, Material material, int energyCost, int energyStorage) {
        this.displayName = displayName;
        this.material = material;
        this.energyCost = energyCost;
        this.energyStorage = energyStorage;
    }

    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public int getEnergyCost() { return energyCost; }
    public int getEnergyStorage() { return energyStorage; }
}