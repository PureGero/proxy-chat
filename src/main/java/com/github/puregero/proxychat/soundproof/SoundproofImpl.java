package com.github.puregero.proxychat.soundproof;

import org.bukkit.entity.Player;

public interface SoundproofImpl {
    default void initialize() {
    };

    default boolean isSoundproofed(Player player) {
        return false;
    };

    default boolean canSendToPlayer(Player origin, Player target) {
        return true;
    };
}
