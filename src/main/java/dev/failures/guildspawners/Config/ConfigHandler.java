package dev.failures.guildspawners.Config;

import dev.failures.guildspawners.GuildSpawners;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigHandler {
    private FileConfiguration cfg;

    private File cf;

    public ConfigHandler(GuildSpawners p, String name) {
        this.cf = new File(p.getDataFolder(), name);
        if (!this.cf.exists())
            try {
                this.cf.createNewFile();
            } catch (IOException e) {
                Bukkit.getServer().getLogger().severe("Something went wrong!" + e);
            }
        this.cfg = (FileConfiguration) YamlConfiguration.loadConfiguration(this.cf);
    }

    public FileConfiguration get() {
        return this.cfg;
    }

    public void reload() {
        this.cfg = (FileConfiguration)YamlConfiguration.loadConfiguration(this.cf);
    }

    public void save() {
        try {
            get().save(this.cf);
            reload();
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe("Something went wrong!" + e);
        }
    }
}
