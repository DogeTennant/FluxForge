package me.dogetennant.fluxforge.utils;

import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigUpdater {

    private final FluxForge plugin;

    public ConfigUpdater(FluxForge plugin) {
        this.plugin = plugin;
    }

    public void updateConfig() {
        updateFile("config.yml", new File(plugin.getDataFolder(), "config.yml"));
    }

    public void updateTranslations() {
        updateTranslationFile("en_us.yml");
        updateTranslationFile("cs_cz.yml");
    }

    private void updateTranslationFile(String filename) {
        File file = new File(plugin.getDataFolder(), "translations/" + filename);
        updateFile("translations/" + filename, file);
    }

    private void updateFile(String resourcePath, File existingFile) {
        // Load the default (bundled) version
        InputStream defaultStream = plugin.getResource(resourcePath);
        if (defaultStream == null) {
            plugin.getLogger().warning("Could not find default resource: " + resourcePath);
            return;
        }

        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8));

        // Load the existing file
        if (!existingFile.exists()) {
            plugin.saveResource(resourcePath, false);
            return;
        }

        FileConfiguration existingConfig = YamlConfiguration.loadConfiguration(existingFile);

        // Add any missing keys from default into existing
        boolean changed = false;
        for (String key : defaultConfig.getKeys(true)) {
            if (!existingConfig.contains(key)) {
                existingConfig.set(key, defaultConfig.get(key));
                changed = true;
                plugin.getLogger().info("Added missing config key: " + key + " to " + resourcePath);
            }
        }

        // Save if anything was added
        if (changed) {
            try {
                existingConfig.save(existingFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save updated file: " + resourcePath);
            }
        }
    }
}