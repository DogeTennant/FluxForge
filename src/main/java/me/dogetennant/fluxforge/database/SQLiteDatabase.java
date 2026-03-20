package me.dogetennant.fluxforge.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.sql.*;
import java.util.*;

public class SQLiteDatabase extends DatabaseManager {

    private HikariDataSource dataSource;

    public SQLiteDatabase(FluxForge plugin) {
        super(plugin);
    }

    @Override
    public void connect() throws Exception {
        File dbFile = new File(plugin.getDataFolder(), "fluxforge.db");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setMaximumPoolSize(1); // SQLite only supports one connection
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("FluxForge-SQLite");
        dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Connected to SQLite database.");
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Disconnected from SQLite database.");
        }
    }

    @Override
    public void createTables() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS machines (
                    world TEXT NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    PRIMARY KEY (world, x, y, z)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS energy (
                    world TEXT NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    amount INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (world, x, y, z)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS fuel (
                    world TEXT NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    amount INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (world, x, y, z)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS networks (
                    network_id TEXT NOT NULL,
                    world TEXT NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    PRIMARY KEY (network_id, world, x, y, z)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS states (
                    world TEXT NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    enabled INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (world, x, y, z)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sorter_sources (
                    sorter_world TEXT NOT NULL,
                    sorter_x INTEGER NOT NULL,
                    sorter_y INTEGER NOT NULL,
                    sorter_z INTEGER NOT NULL,
                    source_world TEXT NOT NULL,
                    source_x INTEGER NOT NULL,
                    source_y INTEGER NOT NULL,
                    source_z INTEGER NOT NULL,
                    PRIMARY KEY (sorter_world, sorter_x, sorter_y, sorter_z)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sorter_filters (
                    sorter_world TEXT NOT NULL,
                    sorter_x INTEGER NOT NULL,
                    sorter_y INTEGER NOT NULL,
                    sorter_z INTEGER NOT NULL,
                    dest_world TEXT NOT NULL,
                    dest_x INTEGER NOT NULL,
                    dest_y INTEGER NOT NULL,
                    dest_z INTEGER NOT NULL,
                    material TEXT NOT NULL,
                    PRIMARY KEY (sorter_world, sorter_x, sorter_y, sorter_z,
                                dest_world, dest_x, dest_y, dest_z, material)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS charging (
                    world TEXT NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    item_data TEXT NOT NULL,
                    PRIMARY KEY (world, x, y, z)
                )
            """);

            plugin.getLogger().info("Database tables created/verified.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create tables: " + e.getMessage());
        }
    }

    // ---- Machines ----

    @Override
    public void saveMachine(Location loc, String type) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO machines (world, x, y, z, type) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockY());
            stmt.setInt(4, loc.getBlockZ());
            stmt.setString(5, type);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save machine: " + e.getMessage());
        }
    }

    @Override
    public void deleteMachine(Location loc) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM machines WHERE world=? AND x=? AND y=? AND z=?")) {
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockY());
            stmt.setInt(4, loc.getBlockZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not delete machine: " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> loadAllMachines() {
        Map<String, String> machines = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM machines")) {
            while (rs.next()) {
                String key = rs.getString("world") + "," + rs.getInt("x") + "," +
                        rs.getInt("y") + "," + rs.getInt("z");
                machines.put(key, rs.getString("type"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load machines: " + e.getMessage());
        }
        return machines;
    }

    // ---- Energy ----

    @Override
    public void saveEnergy(Location loc, int amount) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO energy (world, x, y, z, amount) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockY());
            stmt.setInt(4, loc.getBlockZ());
            stmt.setInt(5, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save energy: " + e.getMessage());
        }
    }

    @Override
    public void deleteEnergy(Location loc) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM energy WHERE world=? AND x=? AND y=? AND z=?")) {
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockY());
            stmt.setInt(4, loc.getBlockZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not delete energy: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Integer> loadAllEnergy() {
        Map<String, Integer> energy = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM energy")) {
            while (rs.next()) {
                String key = rs.getString("world") + "," + rs.getInt("x") + "," +
                        rs.getInt("y") + "," + rs.getInt("z");
                energy.put(key, rs.getInt("amount"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load energy: " + e.getMessage());
        }
        return energy;
    }

    // ---- Fuel ----

    @Override
    public void saveFuel(Location loc, int amount) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO fuel (world, x, y, z, amount) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockY());
            stmt.setInt(4, loc.getBlockZ());
            stmt.setInt(5, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save fuel: " + e.getMessage());
        }
    }

    @Override
    public void deleteFuel(Location loc) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM fuel WHERE world=? AND x=? AND y=? AND z=?")) {
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockY());
            stmt.setInt(4, loc.getBlockZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not delete fuel: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Integer> loadAllFuel() {
        Map<String, Integer> fuel = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM fuel")) {
            while (rs.next()) {
                String key = rs.getString("world") + "," + rs.getInt("x") + "," +
                        rs.getInt("y") + "," + rs.getInt("z");
                fuel.put(key, rs.getInt("amount"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load fuel: " + e.getMessage());
        }
        return fuel;
    }

    // ---- Networks ----

    @Override
    public void saveNetwork(String networkId, Set<String> blocks) {
        try (Connection conn = dataSource.getConnection()) {
            // Delete existing entries for this network
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM networks WHERE network_id=?")) {
                stmt.setString(1, networkId);
                stmt.executeUpdate();
            }
            // Insert all blocks
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO networks (network_id, world, x, y, z) VALUES (?, ?, ?, ?, ?)")) {
                for (String key : blocks) {
                    String[] parts = key.split(",");
                    stmt.setString(1, networkId);
                    stmt.setString(2, parts[0]);
                    stmt.setInt(3, Integer.parseInt(parts[1]));
                    stmt.setInt(4, Integer.parseInt(parts[2]));
                    stmt.setInt(5, Integer.parseInt(parts[3]));
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save network: " + e.getMessage());
        }
    }

    @Override
    public void deleteNetwork(String networkId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM networks WHERE network_id=?")) {
            stmt.setString(1, networkId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not delete network: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Set<String>> loadAllNetworks() {
        Map<String, Set<String>> networks = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM networks")) {
            while (rs.next()) {
                String netId = rs.getString("network_id");
                String key = rs.getString("world") + "," + rs.getInt("x") + "," +
                        rs.getInt("y") + "," + rs.getInt("z");
                networks.computeIfAbsent(netId, k -> new HashSet<>()).add(key);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load networks: " + e.getMessage());
        }
        return networks;
    }

    // ---- States ----

    @Override
    public void saveState(Location loc, boolean enabled) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO states (world, x, y, z, enabled) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockY());
            stmt.setInt(4, loc.getBlockZ());
            stmt.setInt(5, enabled ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save state: " + e.getMessage());
        }
    }

    @Override
    public void deleteState(Location loc) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM states WHERE world=? AND x=? AND y=? AND z=?")) {
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockY());
            stmt.setInt(4, loc.getBlockZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not delete state: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Boolean> loadAllStates() {
        Map<String, Boolean> states = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM states")) {
            while (rs.next()) {
                String key = rs.getString("world") + "," + rs.getInt("x") + "," +
                        rs.getInt("y") + "," + rs.getInt("z");
                states.put(key, rs.getInt("enabled") == 1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load states: " + e.getMessage());
        }
        return states;
    }

    // ---- Sorter Sources ----

    @Override
    public void saveSorterSource(Location sorterLoc, Location sourceLoc) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO sorter_sources " +
                             "(sorter_world, sorter_x, sorter_y, sorter_z, " +
                             "source_world, source_x, source_y, source_z) VALUES (?,?,?,?,?,?,?,?)")) {
            stmt.setString(1, sorterLoc.getWorld().getName());
            stmt.setInt(2, sorterLoc.getBlockX());
            stmt.setInt(3, sorterLoc.getBlockY());
            stmt.setInt(4, sorterLoc.getBlockZ());
            stmt.setString(5, sourceLoc.getWorld().getName());
            stmt.setInt(6, sourceLoc.getBlockX());
            stmt.setInt(7, sourceLoc.getBlockY());
            stmt.setInt(8, sourceLoc.getBlockZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save sorter source: " + e.getMessage());
        }
    }

    @Override
    public void deleteSorterSource(Location sorterLoc) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM sorter_sources WHERE sorter_world=? AND sorter_x=? " +
                             "AND sorter_y=? AND sorter_z=?")) {
            stmt.setString(1, sorterLoc.getWorld().getName());
            stmt.setInt(2, sorterLoc.getBlockX());
            stmt.setInt(3, sorterLoc.getBlockY());
            stmt.setInt(4, sorterLoc.getBlockZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not delete sorter source: " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> loadAllSorterSources() {
        Map<String, String> sources = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM sorter_sources")) {
            while (rs.next()) {
                String sorterKey = rs.getString("sorter_world") + "," + rs.getInt("sorter_x") +
                        "," + rs.getInt("sorter_y") + "," + rs.getInt("sorter_z");
                String sourceKey = rs.getString("source_world") + "," + rs.getInt("source_x") +
                        "," + rs.getInt("source_y") + "," + rs.getInt("source_z");
                sources.put(sorterKey, sourceKey);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load sorter sources: " + e.getMessage());
        }
        return sources;
    }

    // ---- Sorter Filters ----

    @Override
    public void saveSorterFilter(Location sorterLoc, Location destLoc, String material) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR IGNORE INTO sorter_filters " +
                             "(sorter_world, sorter_x, sorter_y, sorter_z, " +
                             "dest_world, dest_x, dest_y, dest_z, material) VALUES (?,?,?,?,?,?,?,?,?)")) {
            stmt.setString(1, sorterLoc.getWorld().getName());
            stmt.setInt(2, sorterLoc.getBlockX());
            stmt.setInt(3, sorterLoc.getBlockY());
            stmt.setInt(4, sorterLoc.getBlockZ());
            stmt.setString(5, destLoc.getWorld().getName());
            stmt.setInt(6, destLoc.getBlockX());
            stmt.setInt(7, destLoc.getBlockY());
            stmt.setInt(8, destLoc.getBlockZ());
            stmt.setString(9, material);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save sorter filter: " + e.getMessage());
        }
    }

    @Override
    public void deleteSorterFilter(Location sorterLoc, Location destLoc, String material) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM sorter_filters WHERE sorter_world=? AND sorter_x=? AND " +
                             "sorter_y=? AND sorter_z=? AND dest_world=? AND dest_x=? AND " +
                             "dest_y=? AND dest_z=? AND material=?")) {
            stmt.setString(1, sorterLoc.getWorld().getName());
            stmt.setInt(2, sorterLoc.getBlockX());
            stmt.setInt(3, sorterLoc.getBlockY());
            stmt.setInt(4, sorterLoc.getBlockZ());
            stmt.setString(5, destLoc.getWorld().getName());
            stmt.setInt(6, destLoc.getBlockX());
            stmt.setInt(7, destLoc.getBlockY());
            stmt.setInt(8, destLoc.getBlockZ());
            stmt.setString(9, material);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not delete sorter filter: " + e.getMessage());
        }
    }

    @Override
    public void deleteAllSorterFilters(Location sorterLoc) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM sorter_filters WHERE sorter_world=? AND sorter_x=? " +
                             "AND sorter_y=? AND sorter_z=?")) {
            stmt.setString(1, sorterLoc.getWorld().getName());
            stmt.setInt(2, sorterLoc.getBlockX());
            stmt.setInt(3, sorterLoc.getBlockY());
            stmt.setInt(4, sorterLoc.getBlockZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not delete sorter filters: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Map<String, List<String>>> loadAllSorterFilters() {
        Map<String, Map<String, List<String>>> filters = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM sorter_filters")) {
            while (rs.next()) {
                String sorterKey = rs.getString("sorter_world") + "," + rs.getInt("sorter_x") +
                        "," + rs.getInt("sorter_y") + "," + rs.getInt("sorter_z");
                String destKey = rs.getString("dest_world") + "," + rs.getInt("dest_x") +
                        "," + rs.getInt("dest_y") + "," + rs.getInt("dest_z");
                String material = rs.getString("material");
                filters.computeIfAbsent(sorterKey, k -> new HashMap<>())
                        .computeIfAbsent(destKey, k -> new ArrayList<>())
                        .add(material);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load sorter filters: " + e.getMessage());
        }
        return filters;
    }

    // ---- Charging ----

    @Override
    public void saveChargingItem(Location loc, ItemStack item) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO charging (world, x, y, z, item_data) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockY());
            stmt.setInt(4, loc.getBlockZ());
            stmt.setString(5, itemToBase64(item));
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save charging item: " + e.getMessage());
        }
    }

    @Override
    public void deleteChargingItem(Location loc) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM charging WHERE world=? AND x=? AND y=? AND z=?")) {
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockY());
            stmt.setInt(4, loc.getBlockZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not delete charging item: " + e.getMessage());
        }
    }

    @Override
    public Map<String, ItemStack> loadAllChargingItems() {
        Map<String, ItemStack> items = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM charging")) {
            while (rs.next()) {
                String key = rs.getString("world") + "," + rs.getInt("x") + "," +
                        rs.getInt("y") + "," + rs.getInt("z");
                ItemStack item = itemFromBase64(rs.getString("item_data"));
                if (item != null) items.put(key, item);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load charging items: " + e.getMessage());
        }
        return items;
    }

    // ---- Item serialization ----

    private String itemToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().severe("Could not serialize item: " + e.getMessage());
            return null;
        }
    }

    private ItemStack itemFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            plugin.getLogger().severe("Could not deserialize item: " + e.getMessage());
            return null;
        }
    }
}