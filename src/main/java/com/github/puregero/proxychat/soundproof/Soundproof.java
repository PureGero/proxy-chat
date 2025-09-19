package com.github.puregero.proxychat.soundproof;

import java.util.List;

import org.bukkit.entity.Player;

import com.github.puregero.proxychat.ProxyChatPlugin;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Soundproof implements SoundproofImpl {
    public static StateFlag SOUNDPROOF_FLAG;

    @Override
    public void initialize() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("soundproof", false);
            registry.register(flag);
            SOUNDPROOF_FLAG = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("soundproof");
            if (!(existing instanceof StateFlag)) {
                ProxyChatPlugin.getInstance().getLogger()
                        .severe("A flag named 'soundproof' already exists but is not a StateFlag flag!");
                return;
            }

            SOUNDPROOF_FLAG = (StateFlag) existing;
        }
    }

    @Override
    public boolean isSoundproofed(Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(localPlayer.getLocation());
        return set.testState(localPlayer, SOUNDPROOF_FLAG);
    }

    @Override
    public boolean canSendToPlayer(Player origin, Player target) {
        LocalPlayer localOrigin = WorldGuardPlugin.inst().wrapPlayer(origin);
        LocalPlayer localTarget = WorldGuardPlugin.inst().wrapPlayer(target);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet originSet = query.getApplicableRegions(localOrigin.getLocation());
        ApplicableRegionSet targetSet = query.getApplicableRegions(localTarget.getLocation());

        List<ProtectedRegion> originRegions = originSet.getRegions().stream()
                .filter(region -> region.getFlag(SOUNDPROOF_FLAG) == StateFlag.State.ALLOW)
                .toList();

        for (ProtectedRegion originRegion : originRegions) {
            if (!targetSet.getRegions().contains(originRegion)) {
                continue;
            }

            return true;
        }

        return false;
    }
}
