package org.yuemi.mmomobs.plugin.config.migration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public interface MigrationStep {
    int getTargetVersion();
    void migrate(@NotNull YamlConfiguration configuration);
}
