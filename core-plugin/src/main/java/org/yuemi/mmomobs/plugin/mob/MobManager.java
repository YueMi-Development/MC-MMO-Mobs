package org.yuemi.mmomobs.plugin.mob;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.attribute.Attribute;
import org.bukkit.NamespacedKey;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class MobManager {

    private final JavaPlugin plugin;
    private final Map<UUID, ActiveMob> activeMobs = new ConcurrentHashMap<>();
    private final Map<String, MobConfigDto> loadedConfigs = new HashMap<>();
    private final ObjectMapper mapper;

    private final NamespacedKey typeKey;
    private final NamespacedKey tagsKey;
    private final NamespacedKey mechanicsSkillsKey;

    public MobManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.typeKey = new NamespacedKey(plugin, "type");
        this.tagsKey = new NamespacedKey(plugin, "tags");
        this.mechanicsSkillsKey = new NamespacedKey("mmomechanics", "skills");

        this.mapper = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                .disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    public void loadConfigs() {
        loadedConfigs.clear();

        // 1. Auto-extract default configs from Jar resources
        try {
            URI jarUri = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            File jarFile = new File(jarUri);
            if (jarFile.exists() && jarFile.isFile()) {
                try (ZipFile zip = new ZipFile(jarFile)) {
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith("mobs/") && name.endsWith(".json5")) {
                            File outFile = new File(plugin.getDataFolder(), name);
                            if (!outFile.exists()) {
                                try {
                                    plugin.saveResource(name, false);
                                } catch (IllegalArgumentException ignored) {
                                    // Already exists
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to extract default mobs: " + e.getMessage());
        }

        // 2. Load configurations from mobs/ directory
        File mobsFolder = new File(plugin.getDataFolder(), "mobs");
        if (!mobsFolder.exists()) {
            mobsFolder.mkdirs();
        }

        File[] files = mobsFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".json5")) {
                    try {
                        MobConfigDto dto = mapper.readValue(file, MobConfigDto.class);
                        String mobTypeId = file.getName().substring(0, file.getName().lastIndexOf('.')).toLowerCase();
                        loadedConfigs.put(mobTypeId, dto);
                        plugin.getLogger().info("Loaded mob configuration: " + dto.name() + " (" + file.getName() + ")");
                    } catch (Exception e) {
                        plugin.getLogger().severe("Failed to load mob file " + file.getName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @NotNull
    public Entity spawnCustomMob(@NotNull String mobType, @NotNull Location location) {
        String cleanType = mobType.toLowerCase();
        MobConfigDto config = loadedConfigs.get(cleanType);
        if (config == null) {
            throw new IllegalArgumentException("Unknown custom mob type: " + mobType);
        }

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(config.type().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid entity type '" + config.type() + "' in configuration for mob: " + mobType);
        }

        Entity entity = location.getWorld().spawnEntity(location, entityType);

        // Apply Custom Display Name
        if (config.name() != null) {
            entity.customName(MiniMessage.miniMessage().deserialize(config.name()));
            entity.setCustomNameVisible(true);
        }

        // Apply Stats if LivingEntity
        if (entity instanceof LivingEntity living) {
            if (config.health() != null) {
                var maxHealthAttr = living.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealthAttr != null) {
                    maxHealthAttr.setBaseValue(config.health());
                    living.setHealth(config.health());
                }
            }
            if (config.speed() != null) {
                var speedAttr = living.getAttribute(Attribute.MOVEMENT_SPEED);
                if (speedAttr != null) {
                    speedAttr.setBaseValue(config.speed());
                }
            }
            if (config.equipment() != null) {
                try {
                    org.yuemi.libs.api.items.ItemsApi itemsApi = org.yuemi.libs.api.items.ItemsApiProvider.getApi();
                    if (itemsApi != null) {
                        var eq = config.equipment();
                        var inv = living.getEquipment();
                        if (inv != null) {
                            if (eq.hand() != null) inv.setItemInMainHand(itemsApi.getItem(eq.hand(), 1));
                            if (eq.offhand() != null) inv.setItemInOffHand(itemsApi.getItem(eq.offhand(), 1));
                            if (eq.helmet() != null) inv.setHelmet(itemsApi.getItem(eq.helmet(), 1));
                            if (eq.chestplate() != null) inv.setChestplate(itemsApi.getItem(eq.chestplate(), 1));
                            if (eq.leggings() != null) inv.setLeggings(itemsApi.getItem(eq.leggings(), 1));
                            if (eq.boots() != null) inv.setBoots(itemsApi.getItem(eq.boots(), 1));
                        }
                    }
                } catch (Throwable t) {
                    plugin.getLogger().warning("Could not apply equipment to custom mob: " + t.getMessage());
                }
            }
        }

        // Register Mob & write PDC data
        registerMob(entity, cleanType, config);
        return entity;
    }

    public void registerMob(@NotNull Entity entity, @NotNull String mobType, @NotNull MobConfigDto config) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(typeKey, PersistentDataType.STRING, mobType);

        List<String> tags = config.tags() != null ? config.tags() : Collections.emptyList();
        if (!tags.isEmpty()) {
            pdc.set(tagsKey, PersistentDataType.STRING, String.join(",", tags));
            for (String tag : tags) {
                entity.addScoreboardTag(tag);
            }
        }

        List<MobSkillConfig> skills = config.skills() != null ? config.skills() : Collections.emptyList();
        if (!skills.isEmpty()) {
            List<String> skillNames = skills.stream().map(MobSkillConfig::skill).toList();
            pdc.set(mechanicsSkillsKey, PersistentDataType.STRING, String.join(",", skillNames));
            for (String skillName : skillNames) {
                String cleanSkillKey = skillName.toLowerCase().replace(" ", "_");
                entity.addScoreboardTag("mmo-skill-" + cleanSkillKey);
            }
        }

        ActiveMob activeMob = new ActiveMob(entity, mobType, tags, skills);
        activeMobs.put(entity.getUniqueId(), activeMob);
    }

    public void registerLoadedMob(@NotNull Entity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        String mobType = pdc.get(typeKey, PersistentDataType.STRING);
        if (mobType == null) {
            return;
        }

        String storedTags = pdc.get(tagsKey, PersistentDataType.STRING);
        List<String> tags = Collections.emptyList();
        if (storedTags != null && !storedTags.isEmpty()) {
            tags = Arrays.stream(storedTags.split(",")).map(String::trim).toList();
        }

        List<MobSkillConfig> skills = Collections.emptyList();
        MobConfigDto config = loadedConfigs.get(mobType.toLowerCase());
        if (config != null && config.skills() != null) {
            skills = config.skills();
        } else {
            String storedSkills = pdc.get(mechanicsSkillsKey, PersistentDataType.STRING);
            if (storedSkills != null && !storedSkills.isEmpty()) {
                skills = Arrays.stream(storedSkills.split(","))
                        .map(String::trim)
                        .map(s -> new MobSkillConfig(s, null))
                        .toList();
            }
        }

        ActiveMob activeMob = new ActiveMob(entity, mobType, tags, skills);
        activeMobs.put(entity.getUniqueId(), activeMob);
    }

    public void unregisterMob(@NotNull UUID uuid) {
        activeMobs.remove(uuid);
    }

    @NotNull
    public Optional<ActiveMob> getActiveMob(@NotNull UUID uuid) {
        return Optional.ofNullable(activeMobs.get(uuid));
    }

    @NotNull
    public Optional<ActiveMob> getActiveMob(@NotNull Entity entity) {
        return getActiveMob(entity.getUniqueId());
    }

    public boolean isCustomMob(@NotNull Entity entity) {
        return entity.getPersistentDataContainer().has(typeKey, PersistentDataType.STRING);
    }

    @NotNull
    public Optional<String> getCustomMobType(@NotNull Entity entity) {
        return Optional.ofNullable(entity.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING));
    }

    @NotNull
    public Collection<String> getCustomMobTags(@NotNull Entity entity) {
        ActiveMob activeMob = activeMobs.get(entity.getUniqueId());
        if (activeMob != null) {
            return activeMob.getTags();
        }
        String storedTags = entity.getPersistentDataContainer().get(tagsKey, PersistentDataType.STRING);
        if (storedTags != null && !storedTags.isEmpty()) {
            return Arrays.stream(storedTags.split(",")).map(String::trim).toList();
        }
        return Collections.emptyList();
    }

    @NotNull
    public Collection<ActiveMob> getActiveMobs() {
        return activeMobs.values();
    }

    @NotNull
    public Collection<String> getRegisteredMobTypes() {
        return loadedConfigs.keySet();
    }
}
