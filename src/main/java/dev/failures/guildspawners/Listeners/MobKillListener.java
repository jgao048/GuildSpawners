package dev.failures.guildspawners.Listeners;

import dev.failures.guildspawners.GuildSpawners;
import dev.failures.guildspawners.Utils.ChatUtil;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.guild.GuildMember;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.Set;

public class MobKillListener implements Listener {
    private GuildSpawners main;

    public MobKillListener(GuildSpawners main) {
        this.main = main;
    }



    @EventHandler
    private void countKills(EntityDeathEvent e) {
        if(e.getEntity().getKiller() == null) return;
        Player p = e.getEntity().getKiller();

        String uuid = "";

        if(GuildSpawners.guilds.getGuild(p) == null) {
            uuid = p.getUniqueId().toString();
        } else {
            uuid = GuildSpawners.guilds.getGuild(p).getId().toString();
        }

        String pathtoType = uuid + "." + e.getEntity().getType().toString().toUpperCase();

        if(!main.dataFile.get().contains(pathtoType)) {
            main.dataFile.get().set(pathtoType,1);
            main.dataFile.save();
            return;
        }

        int amountKilled = main.dataFile.get().getInt(pathtoType);
        main.dataFile.get().set(pathtoType, amountKilled+1);
        main.dataFile.save();


        Set<String> mobs = main.getConfig().getConfigurationSection("gui-spawners").getKeys(false);
        for(String mob: mobs) {
            int amountNeed = main.getConfig().getInt("gui-spawners." +mob+ ".requirements.amount");
            if(amountKilled+1 == amountNeed) {
                if(GuildSpawners.guilds.getGuild(p) == null) {
                    p.sendMessage(ChatUtil.colorize(main.getConfig().getString("spawner-unlocked")
                            .replace("%type%",main.getConfig().getString("gui-spawners."+mob+".type"))
                    ));
                } else {
                    for(GuildMember gm : GuildSpawners.guilds.getGuild(p).getMembers()) {
                        if(!gm.isOnline()) continue;
                        gm.getAsPlayer().sendMessage(ChatUtil.colorize(main.getConfig().getString("spawner-unlocked")
                                .replace("%type%",main.getConfig().getString("gui-spawners."+mob+".type"))
                        ));
                    }
                }
            }
        }
    }
}
