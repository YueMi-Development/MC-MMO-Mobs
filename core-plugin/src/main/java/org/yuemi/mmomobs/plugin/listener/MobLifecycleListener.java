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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class MobLifecycleListener {

    private final JavaPlugin plugin;
    private final MobManager mobManager;
    private final Set<UUID> activeCasters = new HashSet<>();

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
                    Entity entity = event.getEntity();
                    triggerSkills(entity, "onDamaged");

                    // DigOutOfGround option
                    if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                        mobManager.getActiveMob(entity).ifPresent(activeMob -> {
                            var options = activeMob.getOptions();
                            if (options != null && options.digOutOfGround() != null && options.digOutOfGround()) {
                                entity.teleport(entity.getLocation().add(0, 2, 0));
                            }
                        });
                    }
                });

        // PreventTeleport option
        eventApi.bukkit().subscribe(org.bukkit.event.entity.EntityTeleportEvent.class)
                .priority(EventPriority.NORMAL)
                .ignoreCancelled(true)
                .handler(event -> {
                    mobManager.getActiveMob(event.getEntity()).ifPresent(activeMob -> {
                        var options = activeMob.getOptions();
                        if (options != null && options.preventTeleport() != null && options.preventTeleport()) {
                            event.setCancelled(true);
                        }
                    });
                });

        // PreventSlimeSplit option
        eventApi.bukkit().subscribe(org.bukkit.event.entity.SlimeSplitEvent.class)
                .priority(EventPriority.NORMAL)
                .ignoreCancelled(true)
                .handler(event -> {
                    mobManager.getActiveMob(event.getEntity()).ifPresent(activeMob -> {
                        var options = activeMob.getOptions();
                        if (options != null && options.preventSlimeSplit() != null && options.preventSlimeSplit()) {
                            event.setCancelled(true);
                        }
                    });
                });

        // PreventSunburn option
        eventApi.bukkit().subscribe(org.bukkit.event.entity.EntityCombustEvent.class)
                .priority(EventPriority.NORMAL)
                .ignoreCancelled(true)
                .handler(event -> {
                    if (event instanceof org.bukkit.event.entity.EntityCombustByEntityEvent || event instanceof org.bukkit.event.entity.EntityCombustByBlockEvent) {
                        return;
                    }
                    mobManager.getActiveMob(event.getEntity()).ifPresent(activeMob -> {
                        var options = activeMob.getOptions();
                        if (options != null && options.preventSunburn() != null && options.preventSunburn()) {
                            event.setCancelled(true);
                        }
                    });
                });

        // PreventTransformation option
        eventApi.bukkit().subscribe(org.bukkit.event.entity.EntityTransformEvent.class)
                .priority(EventPriority.NORMAL)
                .ignoreCancelled(true)
                .handler(event -> {
                    mobManager.getActiveMob(event.getEntity()).ifPresent(activeMob -> {
                        var options = activeMob.getOptions();
                        if (options != null && options.preventTransformation() != null && options.preventTransformation()) {
                            event.setCancelled(true);
                        }
                    });
                });

        // PreventLeashing option
        eventApi.bukkit().subscribe(org.bukkit.event.entity.PlayerLeashEntityEvent.class)
                .priority(EventPriority.NORMAL)
                .ignoreCancelled(true)
                .handler(event -> {
                    mobManager.getActiveMob(event.getEntity()).ifPresent(activeMob -> {
                        var options = activeMob.getOptions();
                        if (options != null && options.preventLeashing() != null && options.preventLeashing()) {
                            event.setCancelled(true);
                        }
                    });
                });

        // PreventRenaming & Interactable options
        eventApi.bukkit().subscribe(org.bukkit.event.player.PlayerInteractEntityEvent.class)
                .priority(EventPriority.NORMAL)
                .ignoreCancelled(true)
                .handler(event -> {
                    mobManager.getActiveMob(event.getRightClicked()).ifPresent(activeMob -> {
                        var options = activeMob.getOptions();
                        if (options != null) {
                            if (options.interactable() != null && !options.interactable()) {
                                event.setCancelled(true);
                                return;
                            }
                            if (options.preventRenaming() != null && options.preventRenaming()) {
                                org.bukkit.inventory.ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
                                if (item != null && item.getType() == org.bukkit.Material.NAME_TAG) {
                                    event.setCancelled(true);
                                }
                            }
                        }
                    });
                });

        eventApi.bukkit().subscribe(org.bukkit.event.player.PlayerInteractAtEntityEvent.class)
                .priority(EventPriority.NORMAL)
                .ignoreCancelled(true)
                .handler(event -> {
                    mobManager.getActiveMob(event.getRightClicked()).ifPresent(activeMob -> {
                        var options = activeMob.getOptions();
                        if (options != null && options.interactable() != null && !options.interactable()) {
                            event.setCancelled(true);
                        }
                    });
                });

        // MaxCombatDistance & PreventVanillaDamage options
        eventApi.bukkit().subscribe(EntityDamageByEntityEvent.class)
                .priority(EventPriority.NORMAL)
                .ignoreCancelled(true)
                .handler(event -> {
                    Entity victim = event.getEntity();
                    Entity damager = event.getDamager();
                    if (damager instanceof Projectile proj && proj.getShooter() instanceof Entity shooter) {
                        damager = shooter;
                    }

                    // MaxCombatDistance check (player damaging custom mob)
                    if (damager instanceof org.bukkit.entity.Player player) {
                        mobManager.getActiveMob(victim).ifPresent(activeMob -> {
                            var options = activeMob.getOptions();
                            if (options != null && options.maxCombatDistance() != null) {
                                double dist = player.getLocation().distance(victim.getLocation());
                                if (dist > options.maxCombatDistance()) {
                                    event.setCancelled(true);
                                }
                            }
                        });
                    }

                    // PreventVanillaDamage check (custom mob damaging victim)
                    mobManager.getActiveMob(damager).ifPresent(activeMob -> {
                        var options = activeMob.getOptions();
                        if (options != null && options.preventVanillaDamage() != null && options.preventVanillaDamage()) {
                            event.setCancelled(true);
                        }
                    });
                });

        // PreventOtherDrops & PreventMobKillDrops options
        eventApi.bukkit().subscribe(EntityDeathEvent.class)
                .priority(EventPriority.NORMAL)
                .handler(event -> {
                    Entity victim = event.getEntity();

                    // PreventOtherDrops (custom mob dies)
                    mobManager.getActiveMob(victim).ifPresent(activeMob -> {
                        var options = activeMob.getOptions();
                        if (options != null && options.preventOtherDrops() != null && options.preventOtherDrops()) {
                            event.getDrops().clear();
                        }
                    });

                    // PreventMobKillDrops (victim killed by custom mob)
                    if (victim.getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) {
                        Entity killer = damageEvent.getDamager();
                        if (killer instanceof Projectile proj && proj.getShooter() instanceof Entity shooter) {
                            killer = shooter;
                        }
                        mobManager.getActiveMob(killer).ifPresent(activeMob -> {
                            var options = activeMob.getOptions();
                            if (options != null && options.preventMobKillDrops() != null && options.preventMobKillDrops()) {
                                event.getDrops().clear();
                            }
                        });
                    }
                });
    }

    private void triggerSkills(@NotNull Entity entity, @NotNull String triggerType) {
        UUID uuid = entity.getUniqueId();
        if (activeCasters.contains(uuid)) {
            return; // Prevent infinite event loops from self-inflicted skill damage
        }

        activeCasters.add(uuid);
        try {
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
        } finally {
            activeCasters.remove(uuid);
        }
    }
}
