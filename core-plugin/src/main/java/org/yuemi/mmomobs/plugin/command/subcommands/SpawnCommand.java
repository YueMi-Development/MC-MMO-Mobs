package org.yuemi.mmomobs.plugin.command.subcommands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.yuemi.mmomobs.plugin.command.SubCommand;
import org.yuemi.mmomobs.plugin.mob.MobManager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class SpawnCommand implements SubCommand {

    private final MobManager mobManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SpawnCommand(@NotNull MobManager mobManager) {
        this.mobManager = mobManager;
    }

    @Override
    public @NotNull String getName() {
        return "spawn";
    }

    @Override
    public @NotNull String getDescription() {
        return "Spawn a custom mob at your location";
    }

    @Override
    public @NotNull String getPermission() {
        return "mmomobs.command.spawn";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Only players can spawn custom mobs.</red>"));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /mobs spawn <mobType></red>"));
            return;
        }

        String mobType = args[0];
        try {
            Entity spawned = mobManager.spawnCustomMob(mobType, player.getLocation());
            player.sendMessage(miniMessage.deserialize("<green>Successfully spawned custom mob '" + mobType + "' (UUID: " + spawned.getUniqueId() + ")</green>"));
        } catch (IllegalArgumentException e) {
            player.sendMessage(miniMessage.deserialize("<red>" + e.getMessage() + "</red>"));
        } catch (Exception e) {
            player.sendMessage(miniMessage.deserialize("<red>Failed to spawn mob: " + e.getMessage() + "</red>"));
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return mobManager.getRegisteredMobTypes().stream()
                    .filter(type -> type.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
