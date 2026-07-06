package org.yuemi.mmomobs.api;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.Optional;

public interface MmoMobsApi {

    void sendMessage(
            @NotNull Player player,
            @NotNull String message
    );

    boolean isFeatureEnabled(@NotNull Player player);

    boolean isCustomMob(@NotNull Entity entity);

    @NotNull Optional<String> getCustomMobType(@NotNull Entity entity);

    @NotNull Collection<String> getCustomMobTags(@NotNull Entity entity);

    @NotNull Entity spawnCustomMob(@NotNull String mobType, @NotNull Location location);
}
