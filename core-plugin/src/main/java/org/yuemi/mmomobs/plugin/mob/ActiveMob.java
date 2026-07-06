package org.yuemi.mmomobs.plugin.mob;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;
import org.yuemi.mmomobs.plugin.mob.options.MobOptionsDto;

public final class ActiveMob {

    private final UUID uuid;
    private final Entity entity;
    private final String mobType;
    private final List<String> tags;
    private final List<MobSkillConfig> skills;
    private final MobOptionsDto options;

    public ActiveMob(
            @NotNull Entity entity,
            @NotNull String mobType,
            @NotNull List<String> tags,
            @NotNull List<MobSkillConfig> skills,
            @Nullable MobOptionsDto options
    ) {
        this.entity = entity;
        this.uuid = entity.getUniqueId();
        this.mobType = mobType;
        this.tags = List.copyOf(tags);
        this.skills = List.copyOf(skills);
        this.options = options;
    }

    @NotNull
    public UUID getUniqueId() {
        return uuid;
    }

    @NotNull
    public Entity getEntity() {
        return entity;
    }

    @NotNull
    public String getMobType() {
        return mobType;
    }

    @NotNull
    public List<String> getTags() {
        return tags;
    }

    @NotNull
    public List<MobSkillConfig> getSkills() {
        return skills;
    }

    @Nullable
    public MobOptionsDto getOptions() {
        return options;
    }
}
