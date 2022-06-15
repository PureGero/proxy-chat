package com.github.puregero.proxychat;

import com.github.puregero.multilib.MultiLib;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ProxyChatPlugin extends JavaPlugin implements Listener {

    public int distance = 100;

    @Override
    public void onEnable() {
        new SeeAllChatCommand(this);
        new SetProximityDistance(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    private boolean canSeeAllChat(Player player) {
        return "true".equals(MultiLib.getPersistentData(player, "proxychat.seeallchat"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChatGhosts(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) {
            String format = String.format(event.getFormat(), event.getPlayer().getName(), event.getMessage());
            for (int i = 0; (i = format.indexOf('§', i + 1)) >= 0; ) {
                format = format.substring(0, i) + ChatColor.GRAY + format.substring(i + 2);
            }
            event.setFormat(ChatColor.GRAY + format);

            event.getRecipients().removeIf(recipient -> recipient.getGameMode() != GameMode.SPECTATOR && !canSeeAllChat(recipient));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChatDistance(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        event.getRecipients().removeIf(recipient -> {
            boolean remove = recipient.getWorld() != player.getWorld() || horizontalDistanceSquared(recipient.getLocation(), player.getLocation()) > distance * distance;

            if (remove && canSeeAllChat(recipient)) {
                String format = String.format(event.getFormat(), event.getPlayer().getName(), event.getMessage());
                for (int i = 0; (i = format.indexOf('§', i + 1)) >= 0; ) {
                    format = format.substring(0, i) + ChatColor.DARK_GRAY + format.substring(i + 2);
                }
                recipient.sendMessage(ChatColor.DARK_GRAY + format);
            }

            return remove;
        });
    }

    public double horizontalDistanceSquared(Location loc1, Location loc2) {
        return (loc1.getX() - loc2.getX()) * (loc1.getX() - loc2.getX())
                + (loc1.getZ() - loc2.getZ()) * (loc1.getZ() - loc2.getZ());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMe(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().toLowerCase().startsWith("/me ") || event.getMessage().toLowerCase().startsWith("/minecraft:me ")) {
            event.getPlayer().sendMessage(ChatColor.RED + "/me has been disabled.");
            event.setCancelled(true);
        }
    }

}
