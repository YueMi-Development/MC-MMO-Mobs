package org.yuemi.mmomobs.plugin.command;

import org.jetbrains.annotations.NotNull;
import org.yuemi.mmomobs.plugin.command.subcommands.KillAllCommand;
import org.yuemi.mmomobs.plugin.command.subcommands.ReloadCommand;
import org.yuemi.mmomobs.plugin.command.subcommands.SpawnCommand;
import org.yuemi.mmomobs.plugin.mob.MobManager;

public final class MmoMobsCommand extends BaseCommand {

    public MmoMobsCommand(@NotNull MobManager mobManager) {
        register(new ReloadCommand(mobManager));
        register(new SpawnCommand(mobManager));
        register(new KillAllCommand(mobManager));
    }

    @Override
    protected String getCommandLabel() {
        return "MMO Mobs";
    }
}
