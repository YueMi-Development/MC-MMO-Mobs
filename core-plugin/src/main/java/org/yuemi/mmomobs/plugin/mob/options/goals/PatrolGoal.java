package org.yuemi.mmomobs.plugin.mob.options.goals;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;

import java.util.EnumSet;
import java.util.List;

public final class PatrolGoal implements Goal<Mob> {
    private final Mob mob;
    private final GoalKey<Mob> key;
    private final List<Location> patrolPoints;
    private final double speed;
    private int currentIdx = 0;

    public PatrolGoal(Mob mob, List<Location> patrolPoints, double speed) {
        this.mob = mob;
        this.key = GoalKey.of(Mob.class, NamespacedKey.fromString("mmo_mobs:patrol"));
        this.patrolPoints = patrolPoints;
        this.speed = speed;
    }

    @Override
    public boolean shouldActivate() {
        return patrolPoints != null && !patrolPoints.isEmpty();
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void tick() {
        if (patrolPoints == null || patrolPoints.isEmpty()) return;
        Location currentTarget = patrolPoints.get(currentIdx);
        if (mob.getLocation().distanceSquared(currentTarget) < 4.0) {
            currentIdx = (currentIdx + 1) % patrolPoints.size();
            currentTarget = patrolPoints.get(currentIdx);
        }
        mob.getPathfinder().moveTo(currentTarget, speed);
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
