package org.yuemi.mmomobs.plugin.mob.options;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.Goal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.mmomobs.plugin.mob.options.goals.behavior.*;
import org.yuemi.mmomobs.plugin.mob.options.goals.target.*;

import java.util.*;

public final class GoalSelectorManager {

    private static final Map<String, GoalKey<Mob>> VANILLA_GOALS = new HashMap<>();
    private static final Map<String, GoalKey<Mob>> VANILLA_TARGETS = new HashMap<>();

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

        registerVanillaTarget("hurtbytarget", "HURT_BY");
        registerVanillaTarget("attacker", "HURT_BY");
        registerVanillaTarget("damager", "HURT_BY");
    }

    @SuppressWarnings("unchecked")
    private static void registerVanillaTarget(String alias, String fieldName) {
        try {
            java.lang.reflect.Field field = VanillaGoal.class.getField(fieldName);
            GoalKey<Mob> key = (GoalKey<Mob>) field.get(null);
            VANILLA_TARGETS.put(alias.toLowerCase(), key);
        } catch (Throwable ignored) {}
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
                        mobGoals.addGoal(mob, priority, new GoToLocationGoal(mob, loc, speed));
                    }
                } else if (name.equals("gotoowner")) {
                    double dist = 5.0D;
                    double speed = 1.0D;
                    if (!args.isEmpty()) {
                        String[] parts = args.split(" ");
                        dist = Double.parseDouble(parts[0].trim());
                        if (parts.length >= 2) speed = Double.parseDouble(parts[1].trim());
                    }
                    mobGoals.addGoal(mob, priority, new GoToOwnerGoal(mob, dist, speed));
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
                        mobGoals.addGoal(mob, priority, new PatrolGoal(mob, points, speed));
                    }
                } else if (name.equals("fleeplayers") || name.equals("runfromplayers")) {
                    double radius = 10.0D;
                    double speed = 1.2D;
                    if (!args.isEmpty()) {
                        String[] parts = args.split(" ");
                        radius = Double.parseDouble(parts[0].trim());
                        if (parts.length >= 2) speed = Double.parseDouble(parts[1].trim());
                    }
                    mobGoals.addGoal(mob, priority, new FleePlayersGoal(mob, radius, speed));
                } else if (name.equals("donothing")) {
                    mobGoals.addGoal(mob, priority, new DoNothingGoal(mob));
                }
            } catch (Throwable ignored) {}
        }
    }

    public static void applyTargetSelectors(@NotNull Mob mob, @Nullable List<String> selectors) {
        if (selectors == null || selectors.isEmpty()) {
            return;
        }

        boolean clearAll = false;
        for (String sel : selectors) {
            String clean = sel.trim().toLowerCase();
            if (clean.endsWith("clear") || clean.endsWith("reset")) {
                clearAll = true;
                break;
            }
        }

        if (clearAll) {
            Collection<Goal<Mob>> currentGoals = Bukkit.getMobGoals().getAllGoals(mob);
            for (Goal<Mob> goal : new ArrayList<>(currentGoals)) {
                if (goal.getTypes().contains(com.destroystokyo.paper.entity.ai.GoalType.TARGET)) {
                    Bukkit.getMobGoals().removeGoal(mob, goal);
                }
            }
        }

        Set<GoalKey<Mob>> targetsToKeep = new HashSet<>();
        List<ParsedGoal> customTargets = new ArrayList<>();

        for (String sel : selectors) {
            String content = sel.trim();
            int priority = 3; // Default priority
            
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

            int argsIdx = lowerContent.indexOf(' ');
            String baseAlias = argsIdx != -1 ? lowerContent.substring(0, argsIdx) : lowerContent;
            String arguments = argsIdx != -1 ? content.substring(argsIdx + 1).trim() : "";

            GoalKey<Mob> key = VANILLA_TARGETS.get(baseAlias);
            if (key != null) {
                targetsToKeep.add(key);
            } else {
                customTargets.add(new ParsedGoal(priority, baseAlias, arguments));
            }
        }

        if (!clearAll) {
            Collection<Goal<Mob>> currentGoals = Bukkit.getMobGoals().getAllGoals(mob);
            for (Goal<Mob> goal : new ArrayList<>(currentGoals)) {
                if (goal.getTypes().contains(com.destroystokyo.paper.entity.ai.GoalType.TARGET)) {
                    GoalKey<Mob> key = goal.getKey();
                    if (!targetsToKeep.contains(key)) {
                        Bukkit.getMobGoals().removeGoal(mob, goal);
                    }
                }
            }
        }

        applyCustomTargets(mob, customTargets);
    }

    private static void applyCustomTargets(Mob mob, List<ParsedGoal> customTargets) {
        if (customTargets.isEmpty()) return;

        var mobGoals = Bukkit.getMobGoals();

        for (ParsedGoal custom : customTargets) {
            String name = custom.name().toLowerCase();
            String args = custom.args().trim();
            int priority = custom.priority();

            try {
                if (name.equals("players") || name.equals("player")) {
                    double radius = 16.0D;
                    if (!args.isEmpty()) {
                        radius = Double.parseDouble(args);
                    }
                    mobGoals.addGoal(mob, priority, new NearestTargetGoal(mob, "players", org.bukkit.entity.Player.class, radius, null));
                } else if (name.equals("villagers") || name.equals("villager")) {
                    double radius = 16.0D;
                    if (!args.isEmpty()) {
                        radius = Double.parseDouble(args);
                    }
                    mobGoals.addGoal(mob, priority, new NearestTargetGoal(mob, "villagers", org.bukkit.entity.Villager.class, radius, null));
                } else if (name.equals("irongolem") || name.equals("iron_golems") || name.equals("iron_golem")) {
                    double radius = 16.0D;
                    if (!args.isEmpty()) {
                        radius = Double.parseDouble(args);
                    }
                    mobGoals.addGoal(mob, priority, new NearestTargetGoal(mob, "irongolem", org.bukkit.entity.IronGolem.class, radius, null));
                } else if (name.equals("monsters") || name.equals("monster")) {
                    double radius = 16.0D;
                    if (!args.isEmpty()) {
                        radius = Double.parseDouble(args);
                    }
                    mobGoals.addGoal(mob, priority, new NearestTargetGoal(mob, "monsters", org.bukkit.entity.LivingEntity.class, radius, le -> le instanceof org.bukkit.entity.Enemy));
                } else if (name.equals("ownerattacker") || name.equals("ownerhurtby") || name.equals("ownerhurtbytarget") || name.equals("ownerdamager")) {
                    mobGoals.addGoal(mob, priority, new OwnerAttackerGoal(mob));
                } else if (name.equals("ownertarget") || name.equals("ownerattack") || name.equals("ownerhurt")) {
                    mobGoals.addGoal(mob, priority, new OwnerTargetGoal(mob));
                } else if (name.equals("parenthurtby") || name.equals("parenthurtbytarget") || name.equals("parentdamager") || name.equals("parentattacker")) {
                    mobGoals.addGoal(mob, priority, new ParentAttackerGoal(mob));
                } else if (name.equals("parenttarget") || name.equals("parenthurt") || name.equals("parentattack")) {
                    mobGoals.addGoal(mob, priority, new ParentTargetGoal(mob));
                }
            } catch (Throwable ignored) {}
        }
    }

    public static record ParsedGoal(int priority, String name, String args) {}
}
