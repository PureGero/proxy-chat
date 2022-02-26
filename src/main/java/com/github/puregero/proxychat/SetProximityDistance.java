package com.github.puregero.proxychat;

import com.github.puregero.multilib.MultiLib;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SetProximityDistance implements CommandExecutor {
    private final ProxyChatPlugin plugin;

    public SetProximityDistance(ProxyChatPlugin plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("setproximitydistance")).setExecutor(this);

        MultiLib.onString(plugin, "setproximitydistance", string -> {
            plugin.distance = Integer.parseInt(string);
        });

        MultiLib.onString(plugin, "getproximitydistance", (string, reply) -> {
            reply.accept("setproximitydistance", Integer.toString(plugin.distance));
        });

        MultiLib.notify("getproximitydistance", "");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <distance>");
            return false;
        }

        int distance = Integer.parseInt(args[0]);
        plugin.distance = distance;
        MultiLib.notify("setproximitydistance", Integer.toString(distance));
        sender.sendMessage(ChatColor.GREEN + "Set proximity distance to " + distance);

        return true;
    }
}
