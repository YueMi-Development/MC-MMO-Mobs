package org.yuemi.mmomobs.plugin.mob.options.goals.target;

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

public final class OwnerTargetGoal implements Goal<Mob> {
    private final Mob mob;
    private final GoalKey<Mob> key;
    private LivingEntity target;

    public OwnerTargetGoal(Mob mob) {
        this.mob = mob;
        this.key = GoalKey.of(Mob.class, NamespacedKey.fromString("mmo_mobs:owner_target"));
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
        LivingEntity ownerTarget = null;
        if (owner instanceof Mob ownerMob) {
            ownerTarget = ownerMob.getTarget();
        } else if (owner instanceof org.bukkit.entity.Player player) {
            ownerTarget = player.getLastDamageCause() != null ? player.getKiller() : null;
        }
        if (ownerTarget != null && ownerTarget != mob && ownerTarget.isValid() && !ownerTarget.isDead()) {
            this.target = ownerTarget;
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldStayActive() {
        return target != null && target.isValid() && !target.isDead();
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
