package org.yuemi.mmomobs.plugin.listener;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.yuemi.libs.api.event.EventApi;
import org.yuemi.libs.api.event.EventApiProvider;
import org.yuemi.mmomechanics.api.MmoMechanicsApi;
import org.yuemi.mmomobs.plugin.mob.MobManager;
import org.yuemi.mmomobs.plugin.mob.MobSkillConfig;

public final class MobLifecycleListener {

    private final JavaPlugin plugin;
    private final MobManager mobManager;

    public MobLifecycleListener(@NotNull JavaPlugin plugin, @NotNull MobManager mobManager) {
        this.plugin = plugin;
        this.mobManager = mobManager;
    }

    public void register() {
        EventApi eventApi = EventApiProvider.getApi();
        if (eventApi == null) {
            plugin.getLogger().warning("YueMiLibs EventApi service not found. Custom mob listeners will not be registered!");
            return;
        }

        plugin.getLogger().info("Registering custom mob event listeners using YueMiLibs EventApi...");

        // Chunk Entities Load
        eventApi.bukkit().subscribe(EntitiesLoadEvent.class)
                .priority(EventPriority.MONITOR)
                .handler(event -> {
                    for (Entity entity : event.getEntities()) {
                        mobManager.registerLoadedMob(entity);
                    }
                });

        // Chunk Entities Unload
        eventApi.bukkit().subscribe(EntitiesUnloadEvent.class)
                .priority(EventPriority.MONITOR)
                .handler(event -> {
                    for (Entity entity : event.getEntities()) {
                        mobManager.unregisterMob(entity.getUniqueId());
                    }
                });

        // Entity Add To World
        eventApi.bukkit().subscribe(EntityAddToWorldEvent.class)
                .priority(EventPriority.MONITOR)
                .handler(event -> {
                    mobManager.registerLoadedMob(event.getEntity());
                });

        // Entity Remove From World
        eventApi.bukkit().subscribe(EntityRemoveFromWorldEvent.class)
                .priority(EventPriority.MONITOR)
                .handler(event -> {
                    mobManager.unregisterMob(event.getEntity().getUniqueId());
                });

        // Entity Death
        eventApi.bukkit().subscribe(EntityDeathEvent.class)
                .priority(EventPriority.MONITOR)
                .handler(event -> {
                    Entity entity = event.getEntity();
                    triggerSkills(entity, "onDeath");
                    mobManager.unregisterMob(entity.getUniqueId());
                });

        // Entity Damage by Entity (Attacking / Damaged)
        eventApi.bukkit().subscribe(EntityDamageByEntityEvent.class)
                .priority(EventPriority.MONITOR)
                .ignoreCancelled(true)
                .handler(event -> {
                    Entity victim = event.getEntity();
                    Entity attacker = event.getDamager();

                    if (attacker instanceof Projectile proj && proj.getShooter() instanceof Entity shooter) {
                        attacker = shooter;
                    }

                    triggerSkills(attacker, "onAttack");
                    triggerSkills(victim, "onDamaged");
                });

        // Entity Environmental Damage
        eventApi.bukkit().subscribe(EntityDamageEvent.class)
                .priority(EventPriority.MONITOR)
                .ignoreCancelled(true)
                .handler(event -> {
                    if (event instanceof EntityDamageByEntityEvent) {
                        return;
                    }
                    triggerSkills(event.getEntity(), "onDamaged");
                });
    }

    private void triggerSkills(@NotNull Entity entity, @NotNull String triggerType) {
        mobManager.getActiveMob(entity).ifPresent(activeMob -> {
            MmoMechanicsApi mechanicsApi = Bukkit.getServicesManager().load(MmoMechanicsApi.class);
            if (mechanicsApi == null) {
                return;
            }
            for (MobSkillConfig skillConfig : activeMob.getSkills()) {
                if (skillConfig.trigger() != null && skillConfig.trigger().equalsIgnoreCase(triggerType)) {
                    mechanicsApi.castSkill(activeMob.getUniqueId(), skillConfig.skill());
                }
            }
        });
    }
}
