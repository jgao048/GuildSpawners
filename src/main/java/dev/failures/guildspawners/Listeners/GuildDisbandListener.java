package dev.failures.guildspawners.Listeners;

import dev.failures.guildspawners.GuildSpawners;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GuildDisbandListener implements Listener {
    private GuildSpawners main;

    public GuildDisbandListener(GuildSpawners main) {
        this.main = main;
    }

    @EventHandler
    private void guildDisband(GuildRemoveEvent e) {
        Player p = e.getPlayer();
        if(main.dataFile.get().contains(GuildSpawners.guilds.getGuild(p).getId()+"")) {
            main.dataFile.get().set(GuildSpawners.guilds.getGuild(p).getId() + "", null);
            main.dataFile.save();
        }
    }
}
