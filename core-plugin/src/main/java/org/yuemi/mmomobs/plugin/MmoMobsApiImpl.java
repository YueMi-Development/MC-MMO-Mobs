package org.yuemi.mmomobs.plugin;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.yuemi.mmomobs.api.MmoMobsApi;

final class MmoMobsApiImpl implements MmoMobsApi {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void sendMessage(
            @NotNull Player player,
            @NotNull String message
    ) {
        player.sendMessage(miniMessage.deserialize(message));
    }

    @Override
    public boolean isFeatureEnabled(@NotNull Player player) {
        return player.hasPermission("mmomobs.feature");
    }
}
