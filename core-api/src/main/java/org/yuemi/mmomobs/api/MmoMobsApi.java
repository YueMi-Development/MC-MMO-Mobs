package org.yuemi.mmomobs.api;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.Optional;

/**
 * The public API for managing, querying, and spawning custom mobs in MMO-Mobs.
 */
public interface MmoMobsApi {

    /**
     * Sends a MiniMessage styled message to the player.
     *
     * @param player  the recipient player
     * @param message the MiniMessage formatted message string
     */
    void sendMessage(
            @NotNull Player player,
            @NotNull String message
    );

    /**
     * Checks if the MMO-Mobs admin features are enabled for the player.
     *
     * @param player the player to check permissions for
     * @return true if features are enabled, false otherwise
     */
    boolean isFeatureEnabled(@NotNull Player player);

    /**
     * Checks if the given Entity is a custom MMO-Mob.
     *
     * @param entity the entity to check
     * @return true if it contains the custom mob type PDC tag, false otherwise
     */
    boolean isCustomMob(@NotNull Entity entity);

    /**
     * Retrieves the custom mob configuration ID associated with the given Entity.
     *
     * @param entity the entity to query
     * @return an Optional containing the custom mob type if present, otherwise empty
     */
    @NotNull Optional<String> getCustomMobType(@NotNull Entity entity);

    /**
     * Retrieves a collection of scoreboard and custom tags associated with the given Entity.
     *
     * @param entity the entity to query
     * @return a collection of custom tag strings
     */
    @NotNull Collection<String> getCustomMobTags(@NotNull Entity entity);

    /**
     * Spawns a custom mob at the specified location.
     *
     * @param mobType  the custom mob registration ID
     * @param location the location to spawn the entity at
     * @return the spawned Bukkit Entity
     * @throws IllegalArgumentException if the mob type configuration is unknown or base entity type is invalid
     */
    @NotNull Entity spawnCustomMob(@NotNull String mobType, @NotNull Location location);
}
