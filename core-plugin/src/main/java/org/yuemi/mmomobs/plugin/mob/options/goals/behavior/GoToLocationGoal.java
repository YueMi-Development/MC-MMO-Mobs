package org.yuemi.mmomobs.plugin.mob.options.goals.behavior;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;

import java.util.EnumSet;

public final class GoToLocationGoal implements Goal<Mob> {
    private final Mob mob;
    private final GoalKey<Mob> key;
    private final Location targetLocation;
    private final double speed;

    public GoToLocationGoal(Mob mob, Location targetLocation, double speed) {
        this.mob = mob;
        this.key = GoalKey.of(Mob.class, NamespacedKey.fromString("mmo_mobs:goto_location_" + targetLocation.hashCode()));
        this.targetLocation = targetLocation;
        this.speed = speed;
    }

    @Override
    public boolean shouldActivate() {
        return mob.getLocation().distanceSquared(targetLocation) > 2.25; // > 1.5 blocks away
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void tick() {
        mob.getPathfinder().moveTo(targetLocation, speed);
    }

    @Override
    public GoalKey<Mob> getKey() {
        return key;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
    }
}
