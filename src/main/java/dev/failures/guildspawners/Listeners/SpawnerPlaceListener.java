package dev.failures.guildspawners.Listeners;

import dev.failures.guildspawners.GuildSpawners;
import dev.failures.guildspawners.Utils.ChatUtil;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Set;

public class SpawnerPlaceListener implements Listener {
    private GuildSpawners main;

    public SpawnerPlaceListener(GuildSpawners main) {
        this.main = main;
    }

    @EventHandler
    private void spawnerPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if(e.getBlockPlaced().getType() != Material.SPAWNER) return;
        CreatureSpawner cs = (CreatureSpawner) e.getBlockPlaced().getState();
        EntityType st = cs.getSpawnedType();

        if(st.equals(EntityType.PIG)) return;
        String type = st.toString().toUpperCase();
        Set<String> spawners = main.getConfig().getConfigurationSection("gui-spawners").getKeys(false);

        for(String spawner:spawners) {
            if(main.getConfig().getString("gui-spawners." + spawner + ".type").equals(type)) {
                String uuid = "";

                if(GuildSpawners.guilds.getGuild(p) == null) {
                    uuid = p.getUniqueId().toString();
                } else {
                    uuid = GuildSpawners.guilds.getGuild(p).getId().toString();
                }

                String needType = main.getConfig().getString("gui-spawners."+spawner+".requirements.type");
                int killHas = main.dataFile.get().getInt(uuid +"."+needType);
                int killNeed = main.getConfig().getInt("gui-spawners."+spawner+".requirements.amount");

                if(killHas < killNeed) {
                    e.setCancelled(true);
                    p.sendMessage(ChatUtil.colorize(main.getConfig().getString("spawner-locked")
                            .replace("%type%",type)
                    ));
                }
                return;
            }
        }
        e.setCancelled(true);
        p.sendMessage(ChatUtil.colorize(main.getConfig().getString("spawner-locked")
                .replace("%type%",type)
        ));
    }
}
