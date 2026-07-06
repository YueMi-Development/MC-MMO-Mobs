package org.yuemi.mmomobs.plugin.command.subcommands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.yuemi.mmomobs.plugin.command.SubCommand;
import org.yuemi.mmomobs.plugin.mob.MobManager;

import java.util.Collections;
import java.util.List;

public final class ReloadCommand implements SubCommand {

    private final MobManager mobManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ReloadCommand(@NotNull MobManager mobManager) {
        this.mobManager = mobManager;
    }

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public @NotNull String getDescription() {
        return "Reload custom mob configurations";
    }

    @Override
    public @NotNull String getPermission() {
        return "mmomobs.command.reload";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        try {
            mobManager.loadConfigs();
            sender.sendMessage(miniMessage.deserialize("<green>Custom mob configurations reloaded successfully!</green>"));
        } catch (Exception e) {
            sender.sendMessage(miniMessage.deserialize("<red>Failed to reload configurations: " + e.getMessage() + "</red>"));
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
