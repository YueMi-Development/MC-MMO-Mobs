package org.yuemi.mmomobs.plugin;

import org.yuemi.mmomobs.plugin.listener.MobLifecycleListener;
import org.yuemi.mmomobs.plugin.mob.MobManager;
import org.yuemi.mmomobs.plugin.command.MmoMobsCommand;
import org.yuemi.mmomobs.plugin.command.subcommands.SpawnCommand;
import org.yuemi.mmomobs.plugin.command.subcommands.KillAllCommand;
import org.yuemi.mmomobs.plugin.command.subcommands.ReloadCommand;

import java.io.File;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.yuemi.mmomobs.api.MmoMobsApi;

public final class MmoMobsPlugin extends JavaPlugin {

    private MmoMobsApi api;
    private MobManager mobManager;

    @Override
    public void onEnable() {
        new org.yuemi.config.api.ConfigManager(this, "org.yuemi.mmomobs.plugin.config.migration").loadAndMigrate(this);
        reloadConfig();

        this.mobManager = new MobManager(this);
        this.mobManager.loadConfigs();

        new MobLifecycleListener(this, mobManager).register();

        MmoMobsCommand cmd = new MmoMobsCommand();
        cmd.registerSubCommand(new SpawnCommand(mobManager));
        cmd.registerSubCommand(new KillAllCommand(mobManager));
        cmd.registerSubCommand(new ReloadCommand(mobManager));
        var mmoCommand = getCommand("mmomobs");
        if (mmoCommand != null) {
            mmoCommand.setExecutor(cmd);
            mmoCommand.setTabCompleter(cmd);
        }

        this.api = new MmoMobsApiImpl(mobManager);

        getServer().getServicesManager().register(
                MmoMobsApi.class,
                api,
                this,
                ServicePriority.Normal
        );
    }

    @Override
    public void onDisable() {
        if (api != null) {
            getServer().getServicesManager().unregister(MmoMobsApi.class, api);
        }
    }
}
