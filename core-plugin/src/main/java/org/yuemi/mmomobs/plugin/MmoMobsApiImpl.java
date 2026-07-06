package org.yuemi.mmomobs.plugin;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.yuemi.mmomobs.api.MmoMobsApi;
import org.yuemi.mmomobs.plugin.mob.MobManager;

import java.util.Collection;
import java.util.Optional;

final class MmoMobsApiImpl implements MmoMobsApi {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final MobManager mobManager;

    public MmoMobsApiImpl(@NotNull MobManager mobManager) {
        this.mobManager = mobManager;
    }

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

    @Override
    public boolean isCustomMob(@NotNull Entity entity) {
        return mobManager.isCustomMob(entity);
    }

    @Override
    public @NotNull Optional<String> getCustomMobType(@NotNull Entity entity) {
        return mobManager.getCustomMobType(entity);
    }

    @Override
    public @NotNull Collection<String> getCustomMobTags(@NotNull Entity entity) {
        return mobManager.getCustomMobTags(entity);
    }

    @Override
    public @NotNull Entity spawnCustomMob(@NotNull String mobType, @NotNull Location location) {
        return mobManager.spawnCustomMob(mobType, location);
    }
}
