package com.github.puregero.proxychat;

import com.github.puregero.multilib.MultiLib;
import com.github.puregero.proxychat.soundproof.NoSoundproof;
import com.github.puregero.proxychat.soundproof.Soundproof;
import com.github.puregero.proxychat.soundproof.SoundproofImpl;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ProxyChatPlugin extends JavaPlugin implements Listener {

    private static ProxyChatPlugin instance;
    private SoundproofImpl soundproof;

    public int distance = 100;

    @Override
    public void onEnable() {
        instance = this;
        new SeeAllChatCommand(this);
        new SetProximityDistance(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onLoad() {
        setupSoundproof();
    }

    private void setupSoundproof() {
        Plugin worldguardPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldguardPlugin != null) {
            soundproof = new Soundproof();
            soundproof.initialize();
            getLogger().info("WorldGuard detected, enabling soundproof regions.");
        } else {
            soundproof = new NoSoundproof();
            getLogger().info("WorldGuard not detected, soundproof regions disabled.");
        }
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

        boolean isSoundproofed = soundproof.isSoundproofed(player);

        event.renderer(new DarkeningChatRenderer(event.renderer(), 0.33, (source, viewer) -> {
            if (!(viewer instanceof Player recipient)) {
                return false;
            }

            return horizontalDistanceSquared(recipient.getLocation(), source.getLocation()) > distance * distance;
        }));

        event.viewers().removeIf(viewer -> {
            boolean remove = false;

            if (!(viewer instanceof Player recipient)) {
                return remove;
            }

            if (canSeeAllChat(recipient)) {
                return remove;
            }

            remove = recipient.getWorld() != player.getWorld()
                    || horizontalDistanceSquared(recipient.getLocation(), player.getLocation()) > distance * distance;
            if (remove) {
                return remove;
            }

            if (isSoundproofed) {
                remove = !soundproof.canSendToPlayer(player, recipient);
                return remove;
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

    public SoundproofImpl getSoundproof() {
        return soundproof;
    }

    public static ProxyChatPlugin getInstance() {
        return instance;
    }
}
