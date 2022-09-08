package dev.failures.guildspawners.Commands;

import dev.failures.guildspawners.GuildSpawners;
import dev.failures.guildspawners.Utils.ChatUtil;
import me.mattstudios.mfgui.gui.components.ItemBuilder;
import me.mattstudios.mfgui.gui.guis.Gui;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SpawnerShopCommand implements CommandExecutor {
    private GuildSpawners main;
    public SpawnerShopCommand(GuildSpawners main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        Player p = (Player) sender;

        if(args.length > 1 && args[0].equals("reload") && p.hasPermission("guildspawners.reload")) {
            main.reloadConfig();
            main.dataFile.reload();
            p.sendMessage(ChatUtil.colorize("&aGuildSpawners has been reloaded."));
            return true;
        }

        Set<String> spawners = main.getConfig().getConfigurationSection("gui-spawners").getKeys(false);
        Gui spawnershop = new Gui(main.getConfig().getInt("gui-rows"), color("gui-title"));

        for(String spawner:spawners) {
            ItemStack mobspawner = buildSpawner(spawner,color("gui-spawners."+spawner+".name"));
            String texture = main.getConfig().getString("gui-spawners." +spawner+ ".texture");

            GuiItem spawn = ItemBuilder.from(Material.PLAYER_HEAD)
                    .setSkullTexture(texture)
                    .setName(color("gui-spawners." +spawner+ ".name"))
                    .setLore(colorLore("gui-spawners." +spawner+ ".lore",spawner,p))
                    .asGuiItem( event -> {
                        event.setCancelled(true);
                        if(calculatePercent(spawner,p).equals("100")) {
                            int price = main.getConfig().getInt("gui-spawners."+spawner+".cost");
                            if(GuildSpawners.econ.getBalance(p) >= price) {
                                if (p.getInventory().firstEmpty() != -1) {
                                    String type = main.getConfig().getString("gui-spawners." +spawner+ ".type").toUpperCase();
                                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),"stacker give -s " + p.getName() + " spawner " +type+ " 1");
                                    //stacker give [name] ZOMBIE 1
                                    //p.getInventory().setItem(p.getInventory().firstEmpty(), mobspawner);
                                    GuildSpawners.econ.bankWithdraw(p.getName(),price);
                                    p.sendMessage(color("spawner-purchased")
                                            .replace("%type%",main.getConfig().getString("gui-spawners."+spawner+".type"))
                                    );
                                    //p.closeInventory();
                                } else {
                                    p.sendMessage(color("full-inventory"));
                                    //p.closeInventory();
                                }
                            } else {
                                p.sendMessage(color("no-money"));
                                //p.closeInventory();
                            }
                        }
                    });
            spawnershop.setItem(Integer.parseInt(spawner),spawn);
        }

        p.playSound(p.getLocation(), Sound.valueOf(main.getConfig().getString("shop-sound")),3F,1F);
        spawnershop.open(p);

        return false;
    }

    private ItemStack buildSpawner(String spawner,String colorized) {
        ItemStack ms = ItemBuilder.from(Material.SPAWNER).setName(colorized).build();
        ItemMeta msMeta = ms.getItemMeta();
        String type = main.getConfig().getString("gui-spawners." +spawner+ ".type").toUpperCase();

        if(!EntityType.valueOf(type).isSpawnable()) return new ItemStack(Material.SPAWNER);

        BlockStateMeta bsm = (BlockStateMeta) msMeta;
        BlockState bs = bsm.getBlockState();
        CreatureSpawner cs = (CreatureSpawner)bs;
        ((CreatureSpawner) bs).setSpawnedType(EntityType.valueOf(type));
        bsm.setBlockState(cs);
        ms.setItemMeta(msMeta);
        return ms;
    }

    private String color(String path) {
        return ChatUtil.colorize(main.getConfig().getString(path));
    }

    private List<String> colorLore(String path,String spawner,Player p) {
        List<String> colored = new ArrayList<>();
        String percent = "0";
        String status = "0";
        String bar = "";
        String character = main.getConfig().getString("progress-bar");
        String killMessage = "";

        int price = main.getConfig().getInt("gui-spawners."+spawner+".cost");

        for(String l:main.getConfig().getStringList(path)) {
            percent = calculatePercent(spawner,p);
            int prog = Integer.parseInt(percent)/10;
            status = "0";
            bar = "";
            killMessage = "";

            String typepath = "gui-spawners." +spawner+".requirements.type";
            String countpath = "gui-spawners." +spawner+ ".requirements.amount";

            int amountNeed = main.getConfig().getInt(countpath);
            int amountHas = 0;
            String uuid = "";

            if(GuildSpawners.guilds.getGuild(p) == null) {
                uuid = p.getUniqueId().toString();
            } else {
                uuid = GuildSpawners.guilds.getGuild(p).getId().toString();
            }

            if(main.dataFile.get().contains(uuid+"."+main.getConfig().getString(typepath))) {
                amountHas = main.dataFile.get().getInt(uuid+"."+main.getConfig().getString(typepath));
            }

            if(amountHas >= amountNeed) {
                killMessage = ChatUtil.colorize(main.getConfig().getString("kill-completed"));
            } else {
                killMessage = ChatUtil.colorize(main.getConfig().getString("kill-format")
                        .replace("%current%",String.valueOf(amountHas))
                        .replace("%total%",String.valueOf(amountNeed))
                );
            }

            for(int i = 0 ; i < prog ;i++) {
                bar += ChatUtil.colorize(main.getConfig().getString("progress-filled")) + character;
            }
            for(int i = prog ; i < 10;i++) {
                bar += ChatUtil.colorize(main.getConfig().getString("progress-missing")) + character;
            }

            if(!percent.equals("100")) {
                status = ChatUtil.colorize(main.getConfig().getString("status-locked"));
            } else {
                status = ChatUtil.colorize(main.getConfig().getString("status-unlocked"));
            }
            colored.add(ChatUtil.colorize(l)
                    .replace("%progress%",percent)
                    .replace("%status%",status)
                    .replace("%bar%",bar)
                    .replace("%cost%",String.valueOf(price))
                    .replace("%kills%",killMessage)
            );
        }
        return colored;
    }

    private String calculatePercent(String spawner,Player p) {
        String typepath = "gui-spawners." +spawner+".requirements.type";
        String countpath = "gui-spawners." +spawner+ ".requirements.amount";

        if(!main.getConfig().contains(typepath) && !main.getConfig().contains(countpath)) {
            return "100";
        }


        String uuid = "";

        if(GuildSpawners.guilds.getGuild(p) == null) {
            uuid = p.getUniqueId().toString();
        } else {
            uuid = GuildSpawners.guilds.getGuild(p).getId().toString();
        }

        int amountNeed = main.getConfig().getInt(countpath);
        int amountHas = main.dataFile.get().getInt(uuid+"."+main.getConfig().getString(typepath));

        if(!main.dataFile.get().contains(uuid+"."+main.getConfig().getString(typepath))) return "0";

        DecimalFormat df = new DecimalFormat("#");
        double percent = amountHas*100/(double)amountNeed;
        if(percent > 100.0) percent = 100.0;

        return String.valueOf(df.format(percent));
    }
}
