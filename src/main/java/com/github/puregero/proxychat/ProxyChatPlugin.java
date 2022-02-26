package com.github.puregero.proxychat;

import com.github.puregero.multilib.MultiLib;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ProxyChatPlugin extends JavaPlugin implements Listener {

    public int distance = 50;

    @Override
    public void onEnable() {
        new SeeAllChatCommand(this);
        new SetProximityDistance(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        event.getRecipients().removeIf(recipient -> {
            boolean remove = recipient.getWorld() != player.getWorld() || recipient.getLocation().distanceSquared(player.getLocation()) > distance * distance;

            if (remove && "true".equals(MultiLib.getPersistentData(recipient, "proxychat.seeallchat"))) {
                recipient.sendMessage(ChatColor.GRAY + String.format(event.getFormat(), event.getPlayer().getName(), event.getMessage()));
            }

            return remove;
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onMe(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().toLowerCase().startsWith("/me ") || event.getMessage().toLowerCase().startsWith("/minecraft:me ")) {
            event.getPlayer().sendMessage(ChatColor.RED + "/me has been disabled.");
            event.setCancelled(true);
        }
    }

}
