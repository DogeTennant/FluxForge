package me.dogetennant.fluxforge.listeners;

import me.dogetennant.fluxforge.FluxForge;
import me.dogetennant.fluxforge.machines.JetpackItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class JetpackListener implements Listener {

    private final FluxForge plugin;
    private final Set<UUID> thrusting = new HashSet<>();

    public JetpackListener(FluxForge plugin) {
        this.plugin = plugin;
        startTasks();
    }

    private void startTasks() {
        // Every 20 ticks — drain charge while thrusting
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID uuid : new HashSet<>(thrusting)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    thrusting.remove(uuid);
                    continue;
                }

                ItemStack chestplate = player.getInventory().getChestplate();
                if (!JetpackItem.isJetpack(chestplate)) {
                    disableThrust(player);
                    continue;
                }

                int charge = JetpackItem.getCharge(chestplate);
                if (charge <= 0) {
                    disableThrust(player);
                    player.setAllowFlight(false);
                    player.sendMessage(plugin.getLangManager().get("jetpack-no-charge"));
                    continue;
                }

                JetpackItem.setCharge(chestplate, charge - 1);
                player.getInventory().setChestplate(chestplate);

                if (charge <= 100 && charge % 20 == 0) {
                    player.sendMessage(plugin.getLangManager().get("jetpack-low-charge",
                            "{charge}", String.valueOf(charge)));
                }
            }
        }, 20L, 20L);
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE ||
                player.getGameMode() == GameMode.SPECTATOR) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (!JetpackItem.isJetpack(chestplate)) return;

        event.setCancelled(true);

        if (!thrusting.contains(player.getUniqueId())) {
            int charge = JetpackItem.getCharge(chestplate);
            if (charge <= 0) {
                player.sendMessage(plugin.getLangManager().get("jetpack-no-charge"));
                return;
            }
            thrusting.add(player.getUniqueId());
            player.setAllowFlight(true); // Must be before setFlying
            player.setFlying(true);
            player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.5f, 1.2f);
        } else {
            disableThrust(player);
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW_FALLING, 60, 0, false, false));
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!thrusting.contains(player.getUniqueId())) return;

        // Auto-disable thrust when landing
        if (player.isOnGround()) {
            disableThrust(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> refreshJetpack(player), 1L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin,
                () -> refreshJetpack(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        thrusting.remove(event.getPlayer().getUniqueId());
    }

    private void disableThrust(Player player) {
        thrusting.remove(player.getUniqueId());
        if (player.getGameMode() != GameMode.CREATIVE &&
                player.getGameMode() != GameMode.SPECTATOR) {
            player.setFlying(false);
            // Keep allowFlight true so they can double-tap again
            // Only disable if no jetpack or no charge
            ItemStack chestplate = player.getInventory().getChestplate();
            if (!JetpackItem.isJetpack(chestplate) || JetpackItem.getCharge(chestplate) <= 0) {
                player.setAllowFlight(false);
            }
        }
    }

    public void refreshJetpack(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE ||
                player.getGameMode() == GameMode.SPECTATOR) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (JetpackItem.isJetpack(chestplate) && JetpackItem.getCharge(chestplate) > 0) {
            player.setAllowFlight(true);
        } else {
            thrusting.remove(player.getUniqueId());
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    @EventHandler
    public void onGameModeChange(org.bukkit.event.player.PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        // When switching TO survival/adventure, refresh jetpack state
        if (event.getNewGameMode() == GameMode.SURVIVAL ||
                event.getNewGameMode() == GameMode.ADVENTURE) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> refreshJetpack(player), 1L);
        } else {
            // Switching away from survival — clean up thrusting state
            thrusting.remove(player.getUniqueId());
        }
    }
}