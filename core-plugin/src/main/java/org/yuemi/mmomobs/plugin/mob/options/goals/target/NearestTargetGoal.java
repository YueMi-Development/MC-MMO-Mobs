package org.yuemi.mmomobs.plugin.mob.options.goals.target;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Predicate;

public final class NearestTargetGoal implements Goal<Mob> {
    private final Mob mob;
    private final GoalKey<Mob> key;
    private final Class<? extends LivingEntity> targetClass;
    private final double radius;
    private final Predicate<LivingEntity> predicate;
    private LivingEntity target;

    public NearestTargetGoal(Mob mob, String name, Class<? extends LivingEntity> targetClass, double radius, Predicate<LivingEntity> predicate) {
        this.mob = mob;
        this.key = GoalKey.of(Mob.class, NamespacedKey.fromString("mmo_mobs:nearest_target_" + name));
        this.targetClass = targetClass;
        this.radius = radius;
        this.predicate = predicate;
    }

    @Override
    public boolean shouldActivate() {
        Collection<? extends LivingEntity> entities = mob.getWorld().getNearbyEntitiesByType(targetClass, mob.getLocation(), radius, radius, radius);
        LivingEntity closest = null;
        double closestDistSq = Double.MAX_VALUE;
        for (LivingEntity le : entities) {
            if (le == mob || !le.isValid() || le.isDead()) continue;
            if (predicate != null && !predicate.test(le)) continue;
            if (!mob.hasLineOfSight(le)) continue;
            double distSq = mob.getLocation().distanceSquared(le.getLocation());
            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = le;
            }
        }
        if (closest != null) {
            this.target = closest;
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldStayActive() {
        return target != null && target.isValid() && !target.isDead() && mob.getLocation().distanceSquared(target.getLocation()) < radius * radius * 2;
    }

    @Override
    public void start() {
        mob.setTarget(target);
    }

    @Override
    public void stop() {
        mob.setTarget(null);
        this.target = null;
    }

    @Override
    public void tick() {
        if (mob.getTarget() != target) {
            mob.setTarget(target);
        }
    }

    @Override
    public GoalKey<Mob> getKey() {
        return key;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.TARGET);
    }
}
