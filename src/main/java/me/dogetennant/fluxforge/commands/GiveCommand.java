package me.dogetennant.fluxforge.commands;

import me.dogetennant.fluxforge.FluxForge;
import me.dogetennant.fluxforge.machines.ComponentItem;
import me.dogetennant.fluxforge.machines.ComponentType;
import me.dogetennant.fluxforge.machines.MachineItem;
import me.dogetennant.fluxforge.machines.MachineType;
import me.dogetennant.fluxforge.machines.WrenchItem;
import me.dogetennant.fluxforge.machines.JetpackItem;
import me.dogetennant.fluxforge.gui.MachineGui;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import me.dogetennant.fluxforge.database.MySQLDatabase;
import me.dogetennant.fluxforge.database.SQLiteDatabase;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GiveCommand implements CommandExecutor, TabCompleter, Listener {

    private final FluxForge plugin;
    private final Map<UUID, String> pendingAction = new HashMap<>();
    private final Map<UUID, Location> pendingSorterLoc = new HashMap<>();
    private final Map<UUID, String> pendingMaterial = new HashMap<>();

    public GiveCommand(FluxForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().get("player-only"));
            return true;
        }

        // Recipes command is available to everyone
        if (args.length > 0 && args[0].equalsIgnoreCase("recipes")) {
            plugin.getGuiManager().setOpenGui(player.getUniqueId(), null);
            player.openInventory(MachineGui.openRecipesGui());
            return true;
        }

        if (!player.hasPermission("fluxforge.admin")) {
            player.sendMessage(plugin.getLangManager().get("no-permission"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getLangManager().get("cmd-usage"));
            player.sendMessage(plugin.getLangManager().get("cmd-usage-give"));
            player.sendMessage(plugin.getLangManager().get("cmd-usage-givecomponent"));
            player.sendMessage(plugin.getLangManager().get("cmd-usage-givewrench"));
            player.sendMessage(plugin.getLangManager().get("cmd-usage-setenergy"));
            player.sendMessage(plugin.getLangManager().get("cmd-usage-language"));
            player.sendMessage(plugin.getLangManager().get("cmd-usage-recipes"));
            player.sendMessage(plugin.getLangManager().get("cmd-usage-reload"));
            player.sendMessage(plugin.getLangManager().get("cmd-usage-migratedb"));
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 2) {
                player.sendMessage(plugin.getLangManager().get("cmd-usage-give"));
                return true;
            }
            try {
                MachineType type = MachineType.valueOf(args[1].toUpperCase());
                int amount = 1;
                if (args.length >= 3) {
                    try {
                        amount = Math.min(64, Math.max(1, Integer.parseInt(args[2])));
                    } catch (NumberFormatException e) {
                        player.sendMessage(plugin.getLangManager().get("cmd-invalid-amount",
                                "{input}", args[2]));
                        return true;
                    }
                }
                ItemStack item = MachineItem.createMachineItem(type);
                item.setAmount(amount);
                player.getInventory().addItem(item);
                player.sendMessage(plugin.getLangManager().get("cmd-gave-machine",
                        "{amount}", String.valueOf(amount),
                        "{machine}", type.getDisplayName()));
            } catch (IllegalArgumentException e) {
                player.sendMessage(plugin.getLangManager().get("cmd-unknown-machine",
                        "{input}", args[1]));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("givecomponent")) {
            if (args.length < 2) {
                player.sendMessage(plugin.getLangManager().get("cmd-usage-givecomponent"));
                return true;
            }

            // Special case for jetpack
            if (args[1].equalsIgnoreCase("jetpack")) {
                int amount = 1;
                if (args.length >= 3) {
                    try {
                        amount = Math.min(64, Math.max(1, Integer.parseInt(args[2])));
                    } catch (NumberFormatException e) {
                        player.sendMessage(plugin.getLangManager().get("cmd-invalid-amount",
                                "{input}", args[2]));
                        return true;
                    }
                }
                for (int i = 0; i < amount; i++) {
                    player.getInventory().addItem(JetpackItem.createJetpack());
                }
                player.sendMessage(plugin.getLangManager().get("cmd-gave-jetpack"));
                return true;
            }

            try {
                ComponentType type = ComponentType.valueOf(args[1].toUpperCase());
                int amount = 1;
                if (args.length >= 3) {
                    try {
                        amount = Math.min(64, Math.max(1, Integer.parseInt(args[2])));
                    } catch (NumberFormatException e) {
                        player.sendMessage(plugin.getLangManager().get("cmd-invalid-amount",
                                "{input}", args[2]));
                        return true;
                    }
                }
                ItemStack item = ComponentItem.createComponent(type);
                item.setAmount(amount);
                player.getInventory().addItem(item);
                player.sendMessage(plugin.getLangManager().get("cmd-gave-component",
                        "{amount}", String.valueOf(amount),
                        "{component}", type.getDisplayName()));
            } catch (IllegalArgumentException e) {
                player.sendMessage(plugin.getLangManager().get("cmd-unknown-component",
                        "{input}", args[1]));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("givewrench")) {
            player.getInventory().addItem(WrenchItem.createWrench());
            player.sendMessage(plugin.getLangManager().get("cmd-gave-wrench"));
            return true;
        }

        if (args[0].equalsIgnoreCase("setenergy")) {
            if (args.length < 2) {
                player.sendMessage(plugin.getLangManager().get("cmd-usage-setenergy"));
                return true;
            }
            Block targetBlock = player.getTargetBlockExact(5);
            if (targetBlock == null || !plugin.getMachineRegistry().isMachine(targetBlock.getLocation())) {
                player.sendMessage(plugin.getLangManager().get("not-a-machine"));
                return true;
            }
            try {
                int amount = Integer.parseInt(args[1]);
                plugin.getMachineEnergyManager().setBuffer(targetBlock.getLocation(), amount);
                player.sendMessage(plugin.getLangManager().get("cmd-set-energy",
                        "{amount}", String.valueOf(amount)));
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getLangManager().get("cmd-invalid-amount",
                        "{input}", args[1]));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("language")) {
            if (args.length < 2) {
                player.sendMessage(plugin.getLangManager().get("language-current",
                        "{language}", plugin.getLangManager().getCurrentLanguage()));
                return true;
            }
            boolean success = plugin.getLangManager().load(args[1]);
            if (success) {
                player.sendMessage(plugin.getLangManager().get("language-changed",
                        "{language}", args[1]));
            } else {
                player.sendMessage(plugin.getLangManager().get("language-not-found",
                        "{language}", args[1]));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("recipes")) {
            plugin.getGuiManager().setOpenGui(player.getUniqueId(), null);
            player.openInventory(MachineGui.openRecipesGui());
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            player.sendMessage(plugin.getLangManager().get("plugin-reloaded"));
            return true;
        }

        if (args[0].equalsIgnoreCase("migratedb")) {
            if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
                player.sendMessage(plugin.getLangManager().get("migrate-info"));
                player.sendMessage(plugin.getLangManager().get("migrate-configure"));
                player.sendMessage(plugin.getLangManager().get("migrate-confirm"));
                return true;
            }

            String currentType = plugin.getConfig().getString("database.type", "sqlite").toUpperCase();
            if (currentType.equals("MYSQL")) {
                player.sendMessage(plugin.getLangManager().get("migrate-already-mysql"));
                return true;
            }

            player.sendMessage(plugin.getLangManager().get("migrate-starting"));

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    MySQLDatabase mysql = new MySQLDatabase(plugin);
                    mysql.connect();
                    mysql.createTables();

                    SQLiteDatabase sqlite = (SQLiteDatabase) plugin.getDatabase();

                    sqlite.loadAllMachines().forEach((key, type) -> {
                        Location loc = plugin.getNetworkManager().keyToLocation(key);
                        if (loc != null) mysql.saveMachine(loc, type);
                    });

                    sqlite.loadAllEnergy().forEach((key, amount) -> {
                        Location loc = plugin.getNetworkManager().keyToLocation(key);
                        if (loc != null) mysql.saveEnergy(loc, amount);
                    });

                    sqlite.loadAllFuel().forEach((key, amount) -> {
                        Location loc = plugin.getNetworkManager().keyToLocation(key);
                        if (loc != null) mysql.saveFuel(loc, amount);
                    });

                    sqlite.loadAllNetworks().forEach((netId, blocks) -> {
                        mysql.saveNetwork(netId, blocks);
                    });

                    sqlite.loadAllStates().forEach((key, enabled) -> {
                        Location loc = plugin.getNetworkManager().keyToLocation(key);
                        if (loc != null) mysql.saveState(loc, enabled);
                    });

                    sqlite.loadAllSorterSources().forEach((sorterKey, sourceKey) -> {
                        Location sorterLoc = plugin.getNetworkManager().keyToLocation(sorterKey);
                        Location sourceLoc = plugin.getNetworkManager().keyToLocation(sourceKey);
                        if (sorterLoc != null && sourceLoc != null) {
                            mysql.saveSorterSource(sorterLoc, sourceLoc);
                        }
                    });

                    sqlite.loadAllSorterFilters().forEach((sorterKey, destinations) -> {
                        Location sorterLoc = plugin.getNetworkManager().keyToLocation(sorterKey);
                        if (sorterLoc == null) return;
                        destinations.forEach((destKey, materials) -> {
                            Location destLoc = plugin.getNetworkManager().keyToLocation(destKey);
                            if (destLoc == null) return;
                            materials.forEach(material -> mysql.saveSorterFilter(sorterLoc, destLoc, material));
                        });
                    });

                    sqlite.loadAllChargingItems().forEach((key, item) -> {
                        Location loc = plugin.getNetworkManager().keyToLocation(key);
                        if (loc != null) mysql.saveChargingItem(loc, item);
                    });

                    mysql.disconnect();

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(plugin.getLangManager().get("migrate-complete"));
                        player.sendMessage(plugin.getLangManager().get("migrate-switch"));
                    });
                } catch (Exception e) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(plugin.getLangManager().get("migrate-failed",
                                "{error}", e.getMessage()));
                        plugin.getLogger().severe("Migration failed: " + e.getMessage());
                    });
                }
            });
            return true;
        }

        player.sendMessage(plugin.getLangManager().get("unknown-subcommand"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Recipes available to everyone
            if ("recipes".startsWith(args[0].toLowerCase())) {
                completions.add("recipes");
            }

            // Admin commands only for those with permission
            if (sender.hasPermission("fluxforge.admin")) {
                List<String> adminSubcommands = Arrays.asList("give", "givecomponent", "givewrench", "setenergy", "language", "reload", "migratedb");
                for (String sub : adminSubcommands) {
                    if (sub.startsWith(args[0].toLowerCase())) {
                        completions.add(sub);
                    }
                }
            }
        } else if (args.length == 2 && sender.hasPermission("fluxforge.admin")) {
            if (args[0].equalsIgnoreCase("give")) {
                for (MachineType type : MachineType.values()) {
                    if (type.name().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(type.name().toLowerCase());
                    }
                }
            } else if (args[0].equalsIgnoreCase("givecomponent")) {
                List<String> options = new ArrayList<>();
                options.add("jetpack");
                for (ComponentType type : ComponentType.values()) {
                    options.add(type.name().toLowerCase());
                }
                for (String option : options) {
                    if (option.startsWith(args[1].toLowerCase())) {
                        completions.add(option);
                    }
                }
            } else if (args[0].equalsIgnoreCase("language")) {
                for (String lang : plugin.getLangManager().getAvailableLanguages()) {
                    if (lang.startsWith(args[1].toLowerCase())) {
                        completions.add(lang);
                    }
                }
            }
        } else if (args.length == 3 && sender.hasPermission("fluxforge.admin")) {
            if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("givecomponent")) {
                completions.addAll(Arrays.asList("1", "8", "16", "32", "64"));
            }
        }

        return completions;
    }

    @EventHandler
    public void onChestClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        if (!pendingAction.containsKey(player.getUniqueId())) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHEST) {
            player.sendMessage(plugin.getLangManager().get("sorter-not-chest"));
            return;
        }

        event.setCancelled(true);

        String action = pendingAction.get(player.getUniqueId());
        Location sorterLoc = pendingSorterLoc.get(player.getUniqueId());
        Location chestLoc = block.getLocation();

        switch (action) {
            case "setsource" -> {
                plugin.getSorterManager().setSource(sorterLoc, chestLoc);
                player.sendMessage(plugin.getLangManager().get("sorter-source-confirmed"));
            }
            case "adddest" -> {
                String material = pendingMaterial.get(player.getUniqueId());
                plugin.getSorterManager().addFilter(sorterLoc, chestLoc, material);
                player.sendMessage(plugin.getLangManager().get("sorter-filter-added",
                        "{material}", material));
                pendingMaterial.remove(player.getUniqueId());
            }
            case "removedest" -> {
                plugin.getSorterManager().removeFilter(sorterLoc, chestLoc);
                player.sendMessage(plugin.getLangManager().get("sorter-source-confirmed"));
            }
        }

        pendingAction.remove(player.getUniqueId());
        pendingSorterLoc.remove(player.getUniqueId());
    }
}