package com.github.puregero.proxychat;

import com.github.puregero.multilib.MultiLib;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChatGhosts(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) {
            event.renderer(new DarkeningChatRenderer(event.renderer(), 0.66, (source, viewer) -> true));
            event.viewers().removeIf(viewer -> {
                if (viewer instanceof Player recipient) {
                    return recipient.getGameMode() != GameMode.SPECTATOR && !canSeeAllChat(recipient);
                } else {
                    return false;
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChatDistance(AsyncChatEvent event) {
        Player player = event.getPlayer();

        event.renderer(new DarkeningChatRenderer(event.renderer(), 0.33, (source, viewer) -> {
            if (viewer instanceof Player recipient) {
                return horizontalDistanceSquared(recipient.getLocation(), source.getLocation()) > distance * distance;
            } else {
                return false;
            }
        }));

        event.viewers().removeIf(viewer -> {
            boolean remove = false;
            if (viewer instanceof Player recipient && !canSeeAllChat(recipient)) {
                remove = recipient.getWorld() != player.getWorld() || horizontalDistanceSquared(recipient.getLocation(), player.getLocation()) > distance * distance;
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
