package org.yuemi.mmomobs.plugin.command.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.yuemi.mmomobs.plugin.command.BaseSubCommand;
import org.yuemi.mmomobs.plugin.mob.ActiveMob;
import org.yuemi.mmomobs.plugin.mob.MobManager;

import java.util.ArrayList;
import java.util.List;

public final class KillAllCommand extends BaseSubCommand {

    private final MobManager mobManager;

    public KillAllCommand(@NotNull MobManager mobManager) {
        this.mobManager = mobManager;
    }

    @Override
    public @NotNull String getName() {
        return "killall";
    }

    @Override
    public @NotNull String getDescription() {
        return "Kill all currently active custom mobs";
    }

    @Override
    public @NotNull String getPermission() {
        return "mmomobs.command.killall";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        List<ActiveMob> mobs = new ArrayList<>(mobManager.getActiveMobs());
        int count = 0;

        for (ActiveMob mob : mobs) {
            Entity entity = mob.getEntity();
            if (entity.isValid()) {
                entity.remove();
                count++;
            }
            mobManager.unregisterMob(mob.getUniqueId());
        }

        sendMessage(sender, "<green>Successfully removed " + count + " active custom mob(s).</green>");
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}
