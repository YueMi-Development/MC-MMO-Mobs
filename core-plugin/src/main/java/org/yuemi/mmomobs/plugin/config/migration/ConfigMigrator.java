package org.yuemi.mmomobs.plugin.config.migration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ConfigMigrator {
    private final int latestVersion;
    private final List<MigrationStep> steps = new ArrayList<>();
    private final Logger logger;

    public ConfigMigrator(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        
        List<MigrationStep> discovered = discoverSteps(plugin);
        
        int maxVersion = 1;
        for (MigrationStep step : discovered) {
            steps.add(step);
            if (step.getTargetVersion() > maxVersion) {
                maxVersion = step.getTargetVersion();
            }
        }
        steps.sort(Comparator.comparingInt(MigrationStep::getTargetVersion));
        
        this.latestVersion = maxVersion;
    }

    private List<MigrationStep> discoverSteps(JavaPlugin plugin) {
        List<MigrationStep> found = new ArrayList<>();
        String pkg = getClass().getPackageName();
        String path = pkg.replace('.', '/');
        
        try {
            URI uri = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            File srcFile = new File(uri);
            
            if (srcFile.isDirectory()) {
                File pkgDir = new File(srcFile, path);
                if (pkgDir.exists() && pkgDir.isDirectory()) {
                    File[] files = pkgDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                                String className = pkg + "." + file.getName().substring(0, file.getName().length() - 6);
                                tryLoadStep(plugin, className, found);
                            }
                        }
                    }
                }
            } else if (srcFile.isFile() && srcFile.getName().endsWith(".jar")) {
                try (ZipFile zip = new ZipFile(srcFile)) {
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(path + "/") && name.endsWith(".class") && !name.contains("$")) {
                            String className = name.substring(0, name.length() - 6).replace('/', '.');
                            tryLoadStep(plugin, className, found);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to auto-discover configuration migration steps: " + e.getMessage());
        }
        return found;
    }

    private void tryLoadStep(JavaPlugin plugin, String className, List<MigrationStep> list) {
        try {
            Class<?> clazz = Class.forName(className, true, plugin.getClass().getClassLoader());
            if (MigrationStep.class.isAssignableFrom(clazz) && !clazz.isInterface() && !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                MigrationStep step = (MigrationStep) clazz.getDeclaredConstructor().newInstance();
                list.add(step);
                logger.fine("Discovered config migration step: " + clazz.getSimpleName() + " (Target version: " + step.getTargetVersion() + ")");
            }
        } catch (Exception ignored) {
        }
    }

    public void migrate(File configFile) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        int currentVersion = config.getInt("config-version", 1);
        
        if (currentVersion >= latestVersion) return;
        
        boolean modified = false;
        for (MigrationStep step : steps) {
            if (currentVersion == step.getTargetVersion() - 1) {
                int oldVersion = currentVersion;
                logger.fine("Applying config migration step: " + step.getClass().getSimpleName() + " (Target version: " + step.getTargetVersion() + ")");
                step.migrate(config);
                currentVersion = step.getTargetVersion();
                config.set("config-version", currentVersion);
                logger.info("Migrated " + configFile.getName() + " from version " + oldVersion + " to " + currentVersion);
                modified = true;
            }
        }
        
        if (modified) {
            try {
                config.save(configFile);
            } catch (Exception e) {
                logger.severe("Failed to save migrated configuration: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
