package me.dogetennant.fluxforge.managers;

import me.dogetennant.fluxforge.FluxForge;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LangManager {

    private final FluxForge plugin;
    private FileConfiguration lang;
    private FileConfiguration fallback;
    private String currentLanguage;

    public LangManager(FluxForge plugin) {
        this.plugin = plugin;
        saveDefaultTranslations();
        String configLang = plugin.getConfig().getString("language", "en_us");
        load(configLang);
    }

    private void saveDefaultTranslations() {
        saveTranslation("en_us.yml");
        saveTranslation("cs_cz.yml");
    }

    private void saveTranslation(String filename) {
        File translationsFolder = new File(plugin.getDataFolder(), "translations");
        if (!translationsFolder.exists()) translationsFolder.mkdirs();

        File file = new File(translationsFolder, filename);
        if (!file.exists()) {
            plugin.saveResource("translations/" + filename, false);
        }
    }

    public boolean load(String language) {
        File translationsFolder = new File(plugin.getDataFolder(), "translations");
        File file = new File(translationsFolder, language + ".yml");

        if (!file.exists()) {
            plugin.getLogger().warning("Translation file not found: " + language + ".yml");
            return false;
        }

        lang = YamlConfiguration.loadConfiguration(file);
        currentLanguage = language;

        // Always load en_us as fallback
        File fallbackFile = new File(translationsFolder, "en_us.yml");
        if (fallbackFile.exists()) {
            fallback = YamlConfiguration.loadConfiguration(fallbackFile);
        }

        plugin.getLogger().info("Loaded language: " + language);
        return true;
    }

    public String get(String key) {
        String value = lang.getString(key);
        if (value == null && fallback != null) {
            value = fallback.getString(key);
        }
        if (value == null) {
            return "[Missing key: " + key + "]";
        }
        return value.replace("&", "§");
    }

    public String get(String key, String... placeholders) {
        String value = get(key);
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            value = value.replace(placeholders[i], placeholders[i + 1]);
        }
        return value;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public List<String> getAvailableLanguages() {
        List<String> languages = new ArrayList<>();
        File translationsFolder = new File(plugin.getDataFolder(), "translations");
        if (!translationsFolder.exists()) return languages;

        File[] files = translationsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return languages;

        for (File file : files) {
            languages.add(file.getName().replace(".yml", ""));
        }
        return languages;
    }
}