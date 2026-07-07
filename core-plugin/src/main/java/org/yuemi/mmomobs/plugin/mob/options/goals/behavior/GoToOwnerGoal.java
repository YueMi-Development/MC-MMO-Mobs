package org.yuemi.mmomobs.plugin.mob.options.goals.behavior;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Tameable;

import java.util.EnumSet;
import java.util.UUID;

public final class GoToOwnerGoal implements Goal<Mob> {
    private final Mob mob;
    private final GoalKey<Mob> key;
    private final double minDistanceSq;
    private final double speed;

    public GoToOwnerGoal(Mob mob, double minDistance, double speed) {
        this.mob = mob;
        this.key = GoalKey.of(Mob.class, NamespacedKey.fromString("mmo_mobs:goto_owner"));
        this.minDistanceSq = minDistance * minDistance;
        this.speed = speed;
    }

    private LivingEntity getOwner() {
        if (mob instanceof Tameable tameable) {
            org.bukkit.entity.AnimalTamer tamer = tameable.getOwner();
            if (tamer instanceof LivingEntity living) {
                return living;
            }
        }
        if (mob.hasMetadata("owner")) {
            try {
                Object meta = mob.getMetadata("owner").get(0).value();
                if (meta instanceof UUID uuid) {
                    Entity entity = org.bukkit.Bukkit.getEntity(uuid);
                    if (entity instanceof LivingEntity living) {
                        return living;
                    }
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    @Override
    public boolean shouldActivate() {
        LivingEntity owner = getOwner();
        if (owner == null || !owner.isValid()) return false;
        return mob.getLocation().distanceSquared(owner.getLocation()) > minDistanceSq;
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void tick() {
        LivingEntity owner = getOwner();
        if (owner != null) {
            mob.getPathfinder().moveTo(owner, speed);
        }
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
