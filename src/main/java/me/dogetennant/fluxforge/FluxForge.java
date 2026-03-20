package me.dogetennant.fluxforge;

import me.dogetennant.fluxforge.commands.GiveCommand;
import me.dogetennant.fluxforge.gui.MachineGuiListener;
import me.dogetennant.fluxforge.listeners.MachineListener;
import me.dogetennant.fluxforge.listeners.RecipeListener;
import me.dogetennant.fluxforge.managers.*;
import me.dogetennant.fluxforge.utils.ConfigUpdater;
import me.dogetennant.fluxforge.managers.MachineStateManager;
import me.dogetennant.fluxforge.managers.ChargingStationManager;
import me.dogetennant.fluxforge.listeners.JetpackListener;
import me.dogetennant.fluxforge.database.DatabaseManager;
import me.dogetennant.fluxforge.database.SQLiteDatabase;
import me.dogetennant.fluxforge.database.MySQLDatabase;
import org.bukkit.plugin.java.JavaPlugin;

public class FluxForge extends JavaPlugin {

    private static FluxForge instance;
    private DatabaseManager database;
    private MachineRegistry machineRegistry;
    private FuelManager fuelManager;
    private MachineEnergyManager machineEnergyManager;
    private MachineTickManager machineTickManager;
    private SorterManager sorterManager;
    private NetworkManager networkManager;
    private GuiManager guiManager;
    private RecipeManager recipeManager;
    private LangManager langManager;
    private ConfigUpdater configUpdater;
    private MachineStateManager machineStateManager;
    private ChargingStationManager chargingStationManager;

    @Override
    public void onEnable() {
        String dbType = getConfig().getString("database.type", "sqlite").toUpperCase();
        try {
            if (dbType.equals("MYSQL")) {
                database = new MySQLDatabase(this);
            } else {
                database = new SQLiteDatabase(this);
            }
            database.connect();
            database.createTables();
        } catch (Exception e) {
            getLogger().severe("Could not connect to database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        instance = this;
        getLogger().info("FluxForge has been enabled!");
        saveDefaultConfig();
        configUpdater = new ConfigUpdater(this);
        configUpdater.updateConfig();
        configUpdater.updateTranslations();
        langManager = new LangManager(this);
        machineRegistry = new MachineRegistry(this);
        fuelManager = new FuelManager(this);
        machineEnergyManager = new MachineEnergyManager(this);
        networkManager = new NetworkManager(this);
        sorterManager = new SorterManager(this);
        chargingStationManager = new ChargingStationManager(this);
        machineStateManager = new MachineStateManager(this);
        guiManager = new GuiManager();
        machineTickManager = new MachineTickManager(this);
        recipeManager = new RecipeManager(this);
        getServer().getPluginManager().registerEvents(new JetpackListener(this), this);
        getServer().getPluginManager().registerEvents(new MachineGuiListener(this), this);
        getServer().getPluginManager().registerEvents(new MachineListener(this), this);
        getServer().getPluginManager().registerEvents(new RecipeListener(), this);
        GiveCommand giveCommand = new GiveCommand(this);
        getCommand("fluxforge").setExecutor(giveCommand);
        getCommand("fluxforge").setTabCompleter(giveCommand);
        getServer().getPluginManager().registerEvents(giveCommand, this);
    }

    @Override
    public void onDisable() {
        if (machineEnergyManager != null) machineEnergyManager.save();
        if (networkManager != null) networkManager.save();
        if (chargingStationManager != null) chargingStationManager.save();
        if (machineStateManager != null) machineStateManager.save();
        if (database != null) database.disconnect();
        getLogger().info("FluxForge has been disabled!");
    }

    public void reloadPlugin() {
        reloadConfig();
        configUpdater.updateConfig();
        configUpdater.updateTranslations();
        langManager.load(getConfig().getString("language", "en_us"));
    }

    public static FluxForge getInstance() { return instance; }
    public DatabaseManager getDatabase() { return database; }
    public MachineRegistry getMachineRegistry() { return machineRegistry; }
    public FuelManager getFuelManager() { return fuelManager; }
    public MachineEnergyManager getMachineEnergyManager() { return machineEnergyManager; }
    public SorterManager getSorterManager() { return sorterManager; }
    public MachineStateManager getMachineStateManager() { return machineStateManager; }
    public GuiManager getGuiManager() { return guiManager; }
    public MachineTickManager getMachineTickManager() { return machineTickManager; }
    public NetworkManager getNetworkManager() { return networkManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
    public LangManager getLangManager() { return langManager; }
    public ChargingStationManager getChargingStationManager() { return chargingStationManager; }
}