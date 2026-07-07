package org.yuemi.mmomobs.plugin.mob.options.goals.behavior;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;

import java.util.EnumSet;

public final class DoNothingGoal implements Goal<Mob> {
    private final Mob mob;
    private final GoalKey<Mob> key;

    public DoNothingGoal(Mob mob) {
        this.mob = mob;
        this.key = GoalKey.of(Mob.class, NamespacedKey.fromString("mmo_mobs:do_nothing"));
    }

    @Override
    public boolean shouldActivate() {
        return true;
    }

    @Override
    public boolean shouldStayActive() {
        return true;
    }

    @Override
    public void tick() {
        mob.getPathfinder().stopPathfinding();
        mob.setTarget(null);
    }

    @Override
    public GoalKey<Mob> getKey() {
        return key;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.LOOK, GoalType.TARGET);
    }
}
