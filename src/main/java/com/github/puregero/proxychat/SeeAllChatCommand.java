package com.github.puregero.proxychat;

import com.github.puregero.multilib.MultiLib;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SeeAllChatCommand implements CommandExecutor {

    public SeeAllChatCommand(ProxyChatPlugin plugin) {
        Objects.requireNonNull(plugin.getCommand("seeallchat")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        if ("true".equals(MultiLib.getPersistentData(player, "proxychat.seeallchat"))) {
            sender.sendMessage(ChatColor.RED + "You are no longer seeing all chat");
            MultiLib.setPersistentData(player, "proxychat.seeallchat", null);
        } else {
            sender.sendMessage(ChatColor.GREEN + "You are now seeing all chat");
            MultiLib.setPersistentData(player, "proxychat.seeallchat", "true");
        }

        return true;
    }
}
