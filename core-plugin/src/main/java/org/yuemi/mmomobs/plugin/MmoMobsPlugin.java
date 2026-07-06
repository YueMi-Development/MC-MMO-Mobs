package org.yuemi.mmomobs.plugin;

import org.yuemi.mmomobs.plugin.config.migration.ConfigMigrator;
import org.yuemi.mmomobs.plugin.listener.MobLifecycleListener;
import org.yuemi.mmomobs.plugin.mob.MobManager;

import java.io.File;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.yuemi.mmomobs.api.MmoMobsApi;

public final class MmoMobsPlugin extends JavaPlugin {

    private MmoMobsApi api;
    private MobManager mobManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        migrateConfig();

        this.mobManager = new MobManager(this);
        this.mobManager.loadConfigs();

        new MobLifecycleListener(this, mobManager).register();

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

    private void migrateConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            ConfigMigrator migrator = new ConfigMigrator(this);
            migrator.migrate(configFile);
            reloadConfig();
        }
    }
}
