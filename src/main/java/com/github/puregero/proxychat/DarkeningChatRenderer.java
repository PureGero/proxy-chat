package com.github.puregero.proxychat;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class DarkeningChatRenderer implements ChatRenderer {

    private final ChatRenderer superRenderer;
    private final double darkness;
    private final BiFunction<Player, Audience, Boolean> shouldDarken;

    public DarkeningChatRenderer(ChatRenderer superRenderer, double darkness, BiFunction<Player, Audience, Boolean> shouldDarken) {
        this.superRenderer = superRenderer;
        this.darkness = darkness;
        this.shouldDarken = shouldDarken;
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        Component component = superRenderer.render(source, sourceDisplayName, message, viewer);

        if (shouldDarken.apply(source, viewer)) {
            System.out.println(component);
            if (component.color() == null) {
                component = component.color(NamedTextColor.WHITE);
            }
            component = recursivelyDarken(component);
            System.out.println(component);
        }

        return component;
    }

    private Component recursivelyDarken(Component component) {
        component = component.children(component.children().stream().map(this::recursivelyDarken).toList());

        if (component instanceof TranslatableComponent translatableComponent) {
            component = translatableComponent.args(translatableComponent.args().stream().map(this::recursivelyDarken).toList());
        }

        TextColor color = component.color();
        if (color != null) {
            component = component.color(darken(color));
        }

        return component;
    }

    private TextColor darken(TextColor color) {
        int r = color.red();
        int g = color.green();
        int b = color.blue();

        r = (int) (r * darkness);
        g = (int) (g * darkness);
        b = (int) (b * darkness);

        return TextColor.color(r, g, b);
    }
}
