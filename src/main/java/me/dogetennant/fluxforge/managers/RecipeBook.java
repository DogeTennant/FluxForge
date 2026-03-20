package me.dogetennant.fluxforge.managers;

import me.dogetennant.fluxforge.machines.ComponentItem;
import me.dogetennant.fluxforge.machines.ComponentType;
import me.dogetennant.fluxforge.machines.MachineItem;
import me.dogetennant.fluxforge.machines.MachineType;
import me.dogetennant.fluxforge.machines.WrenchItem;
import me.dogetennant.fluxforge.machines.JetpackItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeBook {

    public static class RecipeEntry {
        public final ItemStack result;
        public final ItemStack[] grid; // 9 slots, null = empty

        public RecipeEntry(ItemStack result, ItemStack[] grid) {
            this.result = result;
            this.grid = grid;
        }
    }

    private static ItemStack i(Material m) {
        return new ItemStack(m);
    }

    private static ItemStack c(ComponentType type) {
        return ComponentItem.createComponent(type);
    }

    private static ItemStack m(MachineType type) {
        return MachineItem.createMachineItem(type);
    }

    public static List<RecipeEntry> getAllRecipes() {
        List<RecipeEntry> recipes = new ArrayList<>();

        // ---- Tier 1 Components ----

        // Copper Plate: CC / CC
        recipes.add(new RecipeEntry(c(ComponentType.COPPER_PLATE), new ItemStack[]{
                i(Material.COPPER_INGOT), i(Material.COPPER_INGOT), null,
                i(Material.COPPER_INGOT), i(Material.COPPER_INGOT), null,
                null, null, null
        }));

        // Iron Plate: II / II
        recipes.add(new RecipeEntry(c(ComponentType.IRON_PLATE), new ItemStack[]{
                i(Material.IRON_INGOT), i(Material.IRON_INGOT), null,
                i(Material.IRON_INGOT), i(Material.IRON_INGOT), null,
                null, null, null
        }));

        // Gold Plate: GG / GG
        recipes.add(new RecipeEntry(c(ComponentType.GOLD_PLATE), new ItemStack[]{
                i(Material.GOLD_INGOT), i(Material.GOLD_INGOT), null,
                i(Material.GOLD_INGOT), i(Material.GOLD_INGOT), null,
                null, null, null
        }));

        // Basic Circuit: GRG / RCR / GRG
        recipes.add(new RecipeEntry(c(ComponentType.BASIC_CIRCUIT), new ItemStack[]{
                i(Material.GOLD_NUGGET), i(Material.REDSTONE), i(Material.GOLD_NUGGET),
                i(Material.REDSTONE), c(ComponentType.COPPER_PLATE), i(Material.REDSTONE),
                i(Material.GOLD_NUGGET), i(Material.REDSTONE), i(Material.GOLD_NUGGET)
        }));

        // ---- Tier 2 Components ----

        // Advanced Circuit: PGP / GBG / PGP
        recipes.add(new RecipeEntry(c(ComponentType.ADVANCED_CIRCUIT), new ItemStack[]{
                c(ComponentType.GOLD_PLATE), i(Material.GLOWSTONE_DUST), c(ComponentType.GOLD_PLATE),
                i(Material.GLOWSTONE_DUST), c(ComponentType.BASIC_CIRCUIT), i(Material.GLOWSTONE_DUST),
                c(ComponentType.GOLD_PLATE), i(Material.GLOWSTONE_DUST), c(ComponentType.GOLD_PLATE)
        }));

        // Machine Frame: III / C_C / III
        recipes.add(new RecipeEntry(c(ComponentType.MACHINE_FRAME), new ItemStack[]{
                c(ComponentType.IRON_PLATE), c(ComponentType.IRON_PLATE), c(ComponentType.IRON_PLATE),
                c(ComponentType.COPPER_PLATE), null, c(ComponentType.COPPER_PLATE),
                c(ComponentType.IRON_PLATE), c(ComponentType.IRON_PLATE), c(ComponentType.IRON_PLATE)
        }));

        // Energy Core: DAD / ARA / DAD
        recipes.add(new RecipeEntry(c(ComponentType.ENERGY_CORE), new ItemStack[]{
                i(Material.DIAMOND), c(ComponentType.ADVANCED_CIRCUIT), i(Material.DIAMOND),
                c(ComponentType.ADVANCED_CIRCUIT), i(Material.REDSTONE_BLOCK), c(ComponentType.ADVANCED_CIRCUIT),
                i(Material.DIAMOND), c(ComponentType.ADVANCED_CIRCUIT), i(Material.DIAMOND)
        }));

        // ---- Tier 3 Components ----

        // Flux Crystal: BNB / NEN / BNB
        recipes.add(new RecipeEntry(c(ComponentType.FLUX_CRYSTAL), new ItemStack[]{
                i(Material.BLAZE_ROD), i(Material.NETHER_STAR), i(Material.BLAZE_ROD),
                i(Material.NETHER_STAR), c(ComponentType.ENERGY_CORE), i(Material.NETHER_STAR),
                i(Material.BLAZE_ROD), i(Material.NETHER_STAR), i(Material.BLAZE_ROD)
        }));

        // Advanced Machine Frame: AFA / FXF / AFA
        recipes.add(new RecipeEntry(c(ComponentType.ADVANCED_MACHINE_FRAME), new ItemStack[]{
                c(ComponentType.ADVANCED_CIRCUIT), c(ComponentType.MACHINE_FRAME), c(ComponentType.ADVANCED_CIRCUIT),
                c(ComponentType.MACHINE_FRAME), c(ComponentType.FLUX_CRYSTAL), c(ComponentType.MACHINE_FRAME),
                c(ComponentType.ADVANCED_CIRCUIT), c(ComponentType.MACHINE_FRAME), c(ComponentType.ADVANCED_CIRCUIT)
        }));

        // ---- Machines ----

        // Conduit: CCC / RRR / CCC
        recipes.add(new RecipeEntry(m(MachineType.CONDUIT), new ItemStack[]{
                i(Material.COPPER_INGOT), i(Material.COPPER_INGOT), i(Material.COPPER_INGOT),
                i(Material.REDSTONE), i(Material.REDSTONE), i(Material.REDSTONE),
                i(Material.COPPER_INGOT), i(Material.COPPER_INGOT), i(Material.COPPER_INGOT)
        }));

        // Generator: FFF / FUF / FCF
        recipes.add(new RecipeEntry(m(MachineType.GENERATOR), new ItemStack[]{
                c(ComponentType.MACHINE_FRAME), c(ComponentType.MACHINE_FRAME), c(ComponentType.MACHINE_FRAME),
                c(ComponentType.MACHINE_FRAME), i(Material.FURNACE), c(ComponentType.MACHINE_FRAME),
                c(ComponentType.MACHINE_FRAME), c(ComponentType.BASIC_CIRCUIT), c(ComponentType.MACHINE_FRAME)
        }));

        // Solar Panel: GGG / FAF / FPF
        recipes.add(new RecipeEntry(m(MachineType.SOLAR_PANEL), new ItemStack[]{
                i(Material.GLASS), i(Material.GLASS), i(Material.GLASS),
                c(ComponentType.MACHINE_FRAME), c(ComponentType.ADVANCED_CIRCUIT), c(ComponentType.MACHINE_FRAME),
                c(ComponentType.MACHINE_FRAME), c(ComponentType.GOLD_PLATE), c(ComponentType.MACHINE_FRAME)
        }));

        // Battery: FRF / RER / FRF
        recipes.add(new RecipeEntry(m(MachineType.BATTERY), new ItemStack[]{
                c(ComponentType.MACHINE_FRAME), i(Material.REDSTONE_BLOCK), c(ComponentType.MACHINE_FRAME),
                i(Material.REDSTONE_BLOCK), c(ComponentType.ENERGY_CORE), i(Material.REDSTONE_BLOCK),
                c(ComponentType.MACHINE_FRAME), i(Material.REDSTONE_BLOCK), c(ComponentType.MACHINE_FRAME)
        }));

        // Electric Furnace: AAA / ABA / AEA
        recipes.add(new RecipeEntry(m(MachineType.ELECTRIC_FURNACE), new ItemStack[]{
                c(ComponentType.ADVANCED_MACHINE_FRAME), c(ComponentType.ADVANCED_MACHINE_FRAME), c(ComponentType.ADVANCED_MACHINE_FRAME),
                c(ComponentType.ADVANCED_MACHINE_FRAME), i(Material.BLAST_FURNACE), c(ComponentType.ADVANCED_MACHINE_FRAME),
                c(ComponentType.ADVANCED_MACHINE_FRAME), c(ComponentType.ENERGY_CORE), c(ComponentType.ADVANCED_MACHINE_FRAME)
        }));

        // Miner: AFA / APA / AFA
        recipes.add(new RecipeEntry(m(MachineType.MINER), new ItemStack[]{
                c(ComponentType.ADVANCED_MACHINE_FRAME), c(ComponentType.FLUX_CRYSTAL), c(ComponentType.ADVANCED_MACHINE_FRAME),
                c(ComponentType.ADVANCED_MACHINE_FRAME), i(Material.DIAMOND_PICKAXE), c(ComponentType.ADVANCED_MACHINE_FRAME),
                c(ComponentType.ADVANCED_MACHINE_FRAME), c(ComponentType.FLUX_CRYSTAL), c(ComponentType.ADVANCED_MACHINE_FRAME)
        }));

        // Mob Grinder: AFA / ASA / AFA
        recipes.add(new RecipeEntry(m(MachineType.MOB_GRINDER), new ItemStack[]{
                c(ComponentType.ADVANCED_MACHINE_FRAME), c(ComponentType.FLUX_CRYSTAL), c(ComponentType.ADVANCED_MACHINE_FRAME),
                c(ComponentType.ADVANCED_MACHINE_FRAME), i(Material.DIAMOND_SWORD), c(ComponentType.ADVANCED_MACHINE_FRAME),
                c(ComponentType.ADVANCED_MACHINE_FRAME), c(ComponentType.FLUX_CRYSTAL), c(ComponentType.ADVANCED_MACHINE_FRAME)
        }));

        // Item Sorter: FAF / AHA / FAF
        recipes.add(new RecipeEntry(m(MachineType.ITEM_SORTER), new ItemStack[]{
                c(ComponentType.MACHINE_FRAME), c(ComponentType.ADVANCED_CIRCUIT), c(ComponentType.MACHINE_FRAME),
                c(ComponentType.ADVANCED_CIRCUIT), i(Material.HOPPER), c(ComponentType.ADVANCED_CIRCUIT),
                c(ComponentType.MACHINE_FRAME), c(ComponentType.ADVANCED_CIRCUIT), c(ComponentType.MACHINE_FRAME)
        }));

        // Wrench: G__ / GS_ / _S_
        recipes.add(new RecipeEntry(WrenchItem.createWrench(), new ItemStack[]{
                i(Material.GOLD_INGOT), null, null,
                i(Material.GOLD_INGOT), i(Material.STICK), null,
                null, i(Material.STICK), null
        }));

        // Flux Coil: CIR / IRI / RIC
        recipes.add(new RecipeEntry(c(ComponentType.FLUX_COIL), new ItemStack[]{
                c(ComponentType.COPPER_PLATE), c(ComponentType.IRON_PLATE), i(Material.REDSTONE),
                c(ComponentType.IRON_PLATE), i(Material.REDSTONE), c(ComponentType.IRON_PLATE),
                i(Material.REDSTONE), c(ComponentType.IRON_PLATE), c(ComponentType.COPPER_PLATE)
        }));

        // Energy Cell: FCF / CEC / FCF
        recipes.add(new RecipeEntry(c(ComponentType.ENERGY_CELL), new ItemStack[]{
                c(ComponentType.FLUX_CRYSTAL), c(ComponentType.FLUX_COIL), c(ComponentType.FLUX_CRYSTAL),
                c(ComponentType.FLUX_COIL), c(ComponentType.ENERGY_CORE), c(ComponentType.FLUX_COIL),
                c(ComponentType.FLUX_CRYSTAL), c(ComponentType.FLUX_COIL), c(ComponentType.FLUX_CRYSTAL)
        }));

        // Charging Station: FEF / FCF / FEF
        recipes.add(new RecipeEntry(m(MachineType.CHARGING_STATION), new ItemStack[]{
                c(ComponentType.MACHINE_FRAME), c(ComponentType.ENERGY_CORE), c(ComponentType.MACHINE_FRAME),
                c(ComponentType.MACHINE_FRAME), c(ComponentType.FLUX_COIL), c(ComponentType.MACHINE_FRAME),
                c(ComponentType.MACHINE_FRAME), c(ComponentType.ENERGY_CORE), c(ComponentType.MACHINE_FRAME)
        }));

        // Jetpack: AEA / ACA / AEA
        recipes.add(new RecipeEntry(JetpackItem.createJetpack(), new ItemStack[]{
                c(ComponentType.ADVANCED_MACHINE_FRAME), c(ComponentType.ENERGY_CELL), c(ComponentType.ADVANCED_MACHINE_FRAME),
                c(ComponentType.ADVANCED_MACHINE_FRAME), c(ComponentType.FLUX_CRYSTAL), c(ComponentType.ADVANCED_MACHINE_FRAME),
                c(ComponentType.ADVANCED_MACHINE_FRAME), c(ComponentType.ENERGY_CELL), c(ComponentType.ADVANCED_MACHINE_FRAME)
        }));

        return recipes;
    }
}