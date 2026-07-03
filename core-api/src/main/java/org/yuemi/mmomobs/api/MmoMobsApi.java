package org.yuemi.mmomobs.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface MmoMobsApi {

    void sendMessage(
            @NotNull Player player,
            @NotNull String message
    );

    boolean isFeatureEnabled(@NotNull Player player);
}
