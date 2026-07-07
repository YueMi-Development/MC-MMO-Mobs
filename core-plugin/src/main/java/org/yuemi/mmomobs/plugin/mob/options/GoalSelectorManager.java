package org.yuemi.mmomobs.plugin.mob.options;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.Goal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class GoalSelectorManager {

    private static final Map<String, GoalKey<Mob>> VANILLA_GOALS = new HashMap<>();

    static {
        registerVanilla("float", "FLOAT");
        registerVanilla("swim", "FLOAT");
        registerVanilla("lookatplayers", "LOOK_AT_PLAYER");
        registerVanilla("lookattarget", "LOOK_AT_TARGET");
        registerVanilla("randomlookaround", "RANDOM_LOOK_AROUND");
        registerVanilla("lookaround", "RANDOM_LOOK_AROUND");
        registerVanilla("opendoor", "OPEN_DOOR");
        registerVanilla("opendoors", "OPEN_DOOR");
        registerVanilla("breakdoors", "BREAK_DOOR");
        registerVanilla("eatgrass", "EAT_BLOCK");
        registerVanilla("meleeattack", "MELEE_ATTACK");
        registerVanilla("zombieattack", "ZOMBIE_ATTACK");
        registerVanilla("spiderattack", "SPIDER_ATTACK");
        registerVanilla("leapattarget", "LEAP_AT_TARGET");
        registerVanilla("breed", "BREED");
        registerVanilla("panic", "PANIC");
        registerVanilla("creeperswell", "CREEPER_SWELL");
        registerVanilla("creeperexplode", "CREEPER_SWELL");
        registerVanilla("rangedattack", "RANGED_ATTACK");
        registerVanilla("arrowattack", "RANGED_ATTACK");
        registerVanilla("bowattack", "SKELETON_BOW_ATTACK");
        registerVanilla("bowshoot", "SKELETON_BOW_ATTACK");
        registerVanilla("bowmaster", "SKELETON_BOW_ATTACK");
        registerVanilla("crossbowattack", "PIGLIN_CROSSBOW_ATTACK");
    }

    @SuppressWarnings("unchecked")
    private static void registerVanilla(String alias, String fieldName) {
        try {
            java.lang.reflect.Field field = VanillaGoal.class.getField(fieldName);
            GoalKey<Mob> key = (GoalKey<Mob>) field.get(null);
            VANILLA_GOALS.put(alias.toLowerCase(), key);
        } catch (Throwable ignored) {}
    }

    public static void applyGoalSelectors(@NotNull Mob mob, @Nullable List<String> selectors) {
        if (selectors == null || selectors.isEmpty()) {
            return;
        }

        // Check if clear/reset is first or present
        boolean clearAll = false;
        for (String sel : selectors) {
            String clean = sel.trim().toLowerCase();
            if (clean.endsWith("clear") || clean.endsWith("reset")) {
                clearAll = true;
                break;
            }
        }

        if (clearAll) {
            Bukkit.getMobGoals().removeAllGoals(mob);
        }

        // We will collect the GoalKeys that the user wants to KEEP.
        // If the user specifies a goal, we don't remove it.
        // All other vanilla goals currently on the mob will be removed.
        Set<GoalKey<Mob>> goalsToKeep = new HashSet<>();
        List<ParsedGoal> customGoals = new ArrayList<>();

        for (String sel : selectors) {
            String content = sel.trim();
            int priority = 3; // Default priority
            
            // Parse priority prefix if exists
            int spaceIdx = content.indexOf(' ');
            if (spaceIdx != -1) {
                String firstWord = content.substring(0, spaceIdx);
                try {
                    priority = Integer.parseInt(firstWord);
                    content = content.substring(spaceIdx + 1).trim();
                } catch (NumberFormatException ignored) {}
            }

            String lowerContent = content.toLowerCase();
            if (lowerContent.equals("clear") || lowerContent.equals("reset")) {
                continue;
            }

            // Check if it's a known vanilla goal key
            // For compound names (e.g. gotolocation 10,64,-20), extract the base command/alias
            int argsIdx = lowerContent.indexOf(' ');
            String baseAlias = argsIdx != -1 ? lowerContent.substring(0, argsIdx) : lowerContent;
            String arguments = argsIdx != -1 ? content.substring(argsIdx + 1).trim() : "";

            GoalKey<Mob> key = VANILLA_GOALS.get(baseAlias);
            if (key != null) {
                goalsToKeep.add(key);
            } else {
                customGoals.add(new ParsedGoal(priority, baseAlias, arguments));
            }
        }

        // If not cleared all, remove any vanilla goal that is not in goalsToKeep
        if (!clearAll) {
            Collection<Goal<Mob>> currentGoals = Bukkit.getMobGoals().getAllGoals(mob);
            for (Goal<Mob> goal : new ArrayList<>(currentGoals)) {
                GoalKey<Mob> key = goal.getKey();
                // If it's a vanilla goal (comes from minecraft namespace usually, or is in our VANILLA_GOALS map values)
                // and not in goalsToKeep, remove it.
                if (key.getNamespacedKey().getNamespace().equalsIgnoreCase("minecraft")) {
                    if (!goalsToKeep.contains(key)) {
                        Bukkit.getMobGoals().removeGoal(mob, goal);
                    }
                }
            }
        }

        // Apply custom goals (this will be implemented in Commit 2)
        applyCustomGoals(mob, customGoals);
    }

    private static void applyCustomGoals(Mob mob, List<ParsedGoal> customGoals) {
        if (customGoals.isEmpty()) return;

        org.bukkit.World world = mob.getWorld();
        var mobGoals = Bukkit.getMobGoals();

        for (ParsedGoal custom : customGoals) {
            String name = custom.name().toLowerCase();
            String args = custom.args().trim();
            int priority = custom.priority();

            try {
                if (name.equals("gotolocation") || name.equals("goto")) {
                    String[] parts = args.split(",");
                    if (parts.length >= 3) {
                        double x = Double.parseDouble(parts[0].trim());
                        double y = Double.parseDouble(parts[1].trim());
                        double z = Double.parseDouble(parts[2].trim());
                        double speed = parts.length >= 4 ? Double.parseDouble(parts[3].trim()) : 1.0D;
                        Location loc = new Location(world, x, y, z);
                        mobGoals.addGoal(mob, priority, new org.yuemi.mmomobs.plugin.mob.options.goals.GoToLocationGoal(mob, loc, speed));
                    }
                } else if (name.equals("gotoowner")) {
                    double dist = 5.0D;
                    double speed = 1.0D;
                    if (!args.isEmpty()) {
                        String[] parts = args.split(" ");
                        dist = Double.parseDouble(parts[0].trim());
                        if (parts.length >= 2) speed = Double.parseDouble(parts[1].trim());
                    }
                    mobGoals.addGoal(mob, priority, new org.yuemi.mmomobs.plugin.mob.options.goals.GoToOwnerGoal(mob, dist, speed));
                } else if (name.equals("patrol") || name.equals("patrolroute")) {
                    List<Location> points = new ArrayList<>();
                    String[] coordinateSets = args.split(";");
                    double speed = 1.0D;
                    for (String set : coordinateSets) {
                        String[] parts = set.split(",");
                        if (parts.length >= 3) {
                            double x = Double.parseDouble(parts[0].trim());
                            double y = Double.parseDouble(parts[1].trim());
                            double z = Double.parseDouble(parts[2].trim());
                            points.add(new Location(world, x, y, z));
                        }
                    }
                    if (!points.isEmpty()) {
                        mobGoals.addGoal(mob, priority, new org.yuemi.mmomobs.plugin.mob.options.goals.PatrolGoal(mob, points, speed));
                    }
                } else if (name.equals("fleeplayers") || name.equals("runfromplayers")) {
                    double radius = 10.0D;
                    double speed = 1.2D;
                    if (!args.isEmpty()) {
                        String[] parts = args.split(" ");
                        radius = Double.parseDouble(parts[0].trim());
                        if (parts.length >= 2) speed = Double.parseDouble(parts[1].trim());
                    }
                    mobGoals.addGoal(mob, priority, new org.yuemi.mmomobs.plugin.mob.options.goals.FleePlayersGoal(mob, radius, speed));
                } else if (name.equals("donothing")) {
                    mobGoals.addGoal(mob, priority, new org.yuemi.mmomobs.plugin.mob.options.goals.DoNothingGoal(mob));
                }
            } catch (Throwable ignored) {}
        }
    }

    public static record ParsedGoal(int priority, String name, String args) {}
}
