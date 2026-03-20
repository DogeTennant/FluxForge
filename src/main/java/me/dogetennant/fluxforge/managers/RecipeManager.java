package me.dogetennant.fluxforge.managers;

import me.dogetennant.fluxforge.FluxForge;
import me.dogetennant.fluxforge.machines.ComponentItem;
import me.dogetennant.fluxforge.machines.ComponentType;
import me.dogetennant.fluxforge.machines.MachineItem;
import me.dogetennant.fluxforge.machines.MachineType;
import me.dogetennant.fluxforge.machines.WrenchItem;
import me.dogetennant.fluxforge.machines.JetpackItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

public class RecipeManager {

    private final FluxForge plugin;

    public RecipeManager(FluxForge plugin) {
        this.plugin = plugin;
        registerAll();
    }

    private void registerAll() {
        registerComponentRecipes();
        registerMachineRecipes();
        registerWrenchRecipe();
    }

    private void registerComponentRecipes() {

        // Copper Plate
        ShapedRecipe copperPlate = new ShapedRecipe(
                new NamespacedKey(plugin, "copper_plate"),
                ComponentItem.createComponent(ComponentType.COPPER_PLATE));
        copperPlate.shape("CC", "CC");
        copperPlate.setIngredient('C', Material.COPPER_INGOT);
        plugin.getServer().addRecipe(copperPlate);

        // Iron Plate
        ShapedRecipe ironPlate = new ShapedRecipe(
                new NamespacedKey(plugin, "iron_plate"),
                ComponentItem.createComponent(ComponentType.IRON_PLATE));
        ironPlate.shape("II", "II");
        ironPlate.setIngredient('I', Material.IRON_INGOT);
        plugin.getServer().addRecipe(ironPlate);

        // Gold Plate
        ShapedRecipe goldPlate = new ShapedRecipe(
                new NamespacedKey(plugin, "gold_plate"),
                ComponentItem.createComponent(ComponentType.GOLD_PLATE));
        goldPlate.shape("GG", "GG");
        goldPlate.setIngredient('G', Material.GOLD_INGOT);
        plugin.getServer().addRecipe(goldPlate);

        // Basic Circuit
        ShapedRecipe basicCircuit = new ShapedRecipe(
                new NamespacedKey(plugin, "basic_circuit"),
                ComponentItem.createComponent(ComponentType.BASIC_CIRCUIT));
        basicCircuit.shape("GRG", "RCR", "GRG");
        basicCircuit.setIngredient('G', Material.GOLD_NUGGET);
        basicCircuit.setIngredient('R', Material.REDSTONE);
        basicCircuit.setIngredient('C', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.COPPER_PLATE)));
        plugin.getServer().addRecipe(basicCircuit);

        // Advanced Circuit
        ShapedRecipe advancedCircuit = new ShapedRecipe(
                new NamespacedKey(plugin, "advanced_circuit"),
                ComponentItem.createComponent(ComponentType.ADVANCED_CIRCUIT));
        advancedCircuit.shape("PGP", "GBG", "PGP");
        advancedCircuit.setIngredient('G', Material.GLOWSTONE_DUST);
        advancedCircuit.setIngredient('P', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.GOLD_PLATE)));
        advancedCircuit.setIngredient('B', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.BASIC_CIRCUIT)));
        plugin.getServer().addRecipe(advancedCircuit);

        // Machine Frame
        ShapedRecipe machineFrame = new ShapedRecipe(
                new NamespacedKey(plugin, "machine_frame"),
                ComponentItem.createComponent(ComponentType.MACHINE_FRAME));
        machineFrame.shape("III", "C C", "III");
        machineFrame.setIngredient('I', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.IRON_PLATE)));
        machineFrame.setIngredient('C', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.COPPER_PLATE)));
        plugin.getServer().addRecipe(machineFrame);

        // Energy Core
        ShapedRecipe energyCore = new ShapedRecipe(
                new NamespacedKey(plugin, "energy_core"),
                ComponentItem.createComponent(ComponentType.ENERGY_CORE));
        energyCore.shape("DAD", "ARA", "DAD");
        energyCore.setIngredient('D', Material.DIAMOND);
        energyCore.setIngredient('R', Material.REDSTONE_BLOCK);
        energyCore.setIngredient('A', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ADVANCED_CIRCUIT)));
        plugin.getServer().addRecipe(energyCore);

        // Flux Crystal
        ShapedRecipe fluxCrystal = new ShapedRecipe(
                new NamespacedKey(plugin, "flux_crystal"),
                ComponentItem.createComponent(ComponentType.FLUX_CRYSTAL));
        fluxCrystal.shape("BNB", "NEN", "BNB");
        fluxCrystal.setIngredient('B', Material.BLAZE_ROD);
        fluxCrystal.setIngredient('N', Material.NETHER_STAR);
        fluxCrystal.setIngredient('E', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ENERGY_CORE)));
        plugin.getServer().addRecipe(fluxCrystal);

        // Advanced Machine Frame
        ShapedRecipe advancedMachineFrame = new ShapedRecipe(
                new NamespacedKey(plugin, "advanced_machine_frame"),
                ComponentItem.createComponent(ComponentType.ADVANCED_MACHINE_FRAME));
        advancedMachineFrame.shape("AFA", "FXF", "AFA");
        advancedMachineFrame.setIngredient('A', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ADVANCED_CIRCUIT)));
        advancedMachineFrame.setIngredient('F', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.MACHINE_FRAME)));
        advancedMachineFrame.setIngredient('X', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.FLUX_CRYSTAL)));
        plugin.getServer().addRecipe(advancedMachineFrame);

        // Flux Coil: CPR / PRP / RPC (C=CopperPlate, P=IronPlate, R=Redstone)
        ShapedRecipe fluxCoil = new ShapedRecipe(
                new NamespacedKey(plugin, "flux_coil"),
                ComponentItem.createComponent(ComponentType.FLUX_COIL));
        fluxCoil.shape("CIR", "IRI", "RIC");
        fluxCoil.setIngredient('C', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.COPPER_PLATE)));
        fluxCoil.setIngredient('I', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.IRON_PLATE)));
        fluxCoil.setIngredient('R', Material.REDSTONE);
        plugin.getServer().addRecipe(fluxCoil);

        // Energy Cell: FCF / CEC / FCF (F=FluxCrystal, C=FluxCoil, E=EnergyCore)
        ShapedRecipe energyCell = new ShapedRecipe(
                new NamespacedKey(plugin, "energy_cell"),
                ComponentItem.createComponent(ComponentType.ENERGY_CELL));
        energyCell.shape("FCF", "CEC", "FCF");
        energyCell.setIngredient('F', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.FLUX_CRYSTAL)));
        energyCell.setIngredient('C', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.FLUX_COIL)));
        energyCell.setIngredient('E', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ENERGY_CORE)));
        plugin.getServer().addRecipe(energyCell);
    }

    private void registerMachineRecipes() {

        // Conduit
        ShapedRecipe conduit = new ShapedRecipe(
                new NamespacedKey(plugin, "conduit"),
                MachineItem.createMachineItem(MachineType.CONDUIT));
        conduit.shape("CCC", "RRR", "CCC");
        conduit.setIngredient('C', Material.COPPER_INGOT);
        conduit.setIngredient('R', Material.REDSTONE);
        plugin.getServer().addRecipe(conduit);

        // Generator
        ShapedRecipe generator = new ShapedRecipe(
                new NamespacedKey(plugin, "generator"),
                MachineItem.createMachineItem(MachineType.GENERATOR));
        generator.shape("FFF", "FUF", "FCF");
        generator.setIngredient('U', Material.FURNACE);
        generator.setIngredient('F', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.MACHINE_FRAME)));
        generator.setIngredient('C', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.BASIC_CIRCUIT)));
        plugin.getServer().addRecipe(generator);

        // Solar Panel
        ShapedRecipe solarPanel = new ShapedRecipe(
                new NamespacedKey(plugin, "solar_panel"),
                MachineItem.createMachineItem(MachineType.SOLAR_PANEL));
        solarPanel.shape("GGG", "FAF", "FPF");
        solarPanel.setIngredient('G', Material.GLASS);
        solarPanel.setIngredient('F', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.MACHINE_FRAME)));
        solarPanel.setIngredient('A', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ADVANCED_CIRCUIT)));
        solarPanel.setIngredient('P', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.GOLD_PLATE)));
        plugin.getServer().addRecipe(solarPanel);

        // Battery
        ShapedRecipe battery = new ShapedRecipe(
                new NamespacedKey(plugin, "battery"),
                MachineItem.createMachineItem(MachineType.BATTERY));
        battery.shape("FRF", "RER", "FRF");
        battery.setIngredient('R', Material.REDSTONE_BLOCK);
        battery.setIngredient('F', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.MACHINE_FRAME)));
        battery.setIngredient('E', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ENERGY_CORE)));
        plugin.getServer().addRecipe(battery);

        // Electric Furnace
        ShapedRecipe electricFurnace = new ShapedRecipe(
                new NamespacedKey(plugin, "electric_furnace"),
                MachineItem.createMachineItem(MachineType.ELECTRIC_FURNACE));
        electricFurnace.shape("AAA", "ABA", "AEA");
        electricFurnace.setIngredient('B', Material.BLAST_FURNACE);
        electricFurnace.setIngredient('A', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ADVANCED_MACHINE_FRAME)));
        electricFurnace.setIngredient('E', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ENERGY_CORE)));
        plugin.getServer().addRecipe(electricFurnace);

        // Miner
        ShapedRecipe miner = new ShapedRecipe(
                new NamespacedKey(plugin, "miner"),
                MachineItem.createMachineItem(MachineType.MINER));
        miner.shape("AFA", "APA", "AFA");
        miner.setIngredient('P', Material.DIAMOND_PICKAXE);
        miner.setIngredient('A', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ADVANCED_MACHINE_FRAME)));
        miner.setIngredient('F', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.FLUX_CRYSTAL)));
        plugin.getServer().addRecipe(miner);

        // Mob Grinder
        ShapedRecipe mobGrinder = new ShapedRecipe(
                new NamespacedKey(plugin, "mob_grinder"),
                MachineItem.createMachineItem(MachineType.MOB_GRINDER));
        mobGrinder.shape("AFA", "ASA", "AFA");
        mobGrinder.setIngredient('S', Material.DIAMOND_SWORD);
        mobGrinder.setIngredient('A', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ADVANCED_MACHINE_FRAME)));
        mobGrinder.setIngredient('F', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.FLUX_CRYSTAL)));
        plugin.getServer().addRecipe(mobGrinder);

        // Item Sorter
        ShapedRecipe itemSorter = new ShapedRecipe(
                new NamespacedKey(plugin, "item_sorter"),
                MachineItem.createMachineItem(MachineType.ITEM_SORTER));
        itemSorter.shape("FAF", "AHA", "FAF");
        itemSorter.setIngredient('H', Material.HOPPER);
        itemSorter.setIngredient('F', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.MACHINE_FRAME)));
        itemSorter.setIngredient('A', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ADVANCED_CIRCUIT)));
        plugin.getServer().addRecipe(itemSorter);

        // Charging Station: FFF / FCF / FFF (F=MachineFrame, C=FluxCoil, E=EnergyCore)
        ShapedRecipe chargingStation = new ShapedRecipe(
                new NamespacedKey(plugin, "charging_station"),
                MachineItem.createMachineItem(MachineType.CHARGING_STATION));
        chargingStation.shape("FEF", "FCF", "FEF");
        chargingStation.setIngredient('F', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.MACHINE_FRAME)));
        chargingStation.setIngredient('E', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ENERGY_CORE)));
        chargingStation.setIngredient('C', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.FLUX_COIL)));
        plugin.getServer().addRecipe(chargingStation);

        // Jetpack: AEA / ACA / AEA (A=AdvMachineFrame, E=EnergyCell, C=FluxCrystal)
        ShapedRecipe jetpack = new ShapedRecipe(
                new NamespacedKey(plugin, "jetpack"),
                JetpackItem.createJetpack());
        jetpack.shape("AEA", "ACA", "AEA");
        jetpack.setIngredient('A', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ADVANCED_MACHINE_FRAME)));
        jetpack.setIngredient('E', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.ENERGY_CELL)));
        jetpack.setIngredient('C', new RecipeChoice.ExactChoice(
                ComponentItem.createComponent(ComponentType.FLUX_CRYSTAL)));
        plugin.getServer().addRecipe(jetpack);
    }

    private void registerWrenchRecipe() {
        ShapedRecipe wrench = new ShapedRecipe(
                new NamespacedKey(plugin, "wrench"),
                WrenchItem.createWrench());
        wrench.shape("G  ", "GS ", " S ");
        wrench.setIngredient('G', Material.GOLD_INGOT);
        wrench.setIngredient('S', Material.STICK);
        plugin.getServer().addRecipe(wrench);
    }
}