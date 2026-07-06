package org.yuemi.mmomobs.plugin.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SubCommand {
    @NotNull String getName();

    @NotNull String getDescription();

    @NotNull String getPermission();

    void execute(@NotNull CommandSender sender, @NotNull String[] args);

    @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args);
}
