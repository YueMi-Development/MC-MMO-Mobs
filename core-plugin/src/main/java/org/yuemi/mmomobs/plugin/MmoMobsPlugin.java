package org.yuemi.mmomobs.plugin;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.yuemi.mmomobs.api.MmoMobsApi;

public final class MmoMobsPlugin extends JavaPlugin {

    private MmoMobsApi api;

    @Override
    public void onEnable() {
        this.api = new MmoMobsApiImpl();

        getServer().getServicesManager().register(
                MmoMobsApi.class,
                api,
                this,
                ServicePriority.Normal
        );
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregister(MmoMobsApi.class, api);
    }
}
