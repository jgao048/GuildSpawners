package dev.failures.guildspawners;

import dev.failures.guildspawners.Commands.SpawnerShopCommand;
import dev.failures.guildspawners.Config.ConfigHandler;
import dev.failures.guildspawners.Listeners.GuildDisbandListener;
import dev.failures.guildspawners.Listeners.MobKillListener;
import dev.failures.guildspawners.Listeners.SpawnerPlaceListener;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class GuildSpawners extends JavaPlugin {
    public ConfigHandler dataFile;
    public static Economy econ = null;
    public static GuildsAPI guilds;
    @Override
    public void onEnable() {
        saveDefaultConfig();

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rsp.getProvider();

        dataFile = new ConfigHandler(this,"data.yml");
        dataFile.save();

        guilds = Guilds.getApi();

        getCommand("spawnershop").setExecutor(new SpawnerShopCommand(this));
        getServer().getPluginManager().registerEvents(new MobKillListener(this),this);
        getServer().getPluginManager().registerEvents(new GuildDisbandListener(this),this);
        getServer().getPluginManager().registerEvents(new SpawnerPlaceListener(this),this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
