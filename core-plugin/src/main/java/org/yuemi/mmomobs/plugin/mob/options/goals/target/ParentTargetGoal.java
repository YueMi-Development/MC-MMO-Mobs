package org.yuemi.mmomobs.plugin.mob.options.goals.target;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

import java.util.EnumSet;
import java.util.UUID;

public final class ParentTargetGoal implements Goal<Mob> {
    private final Mob mob;
    private final GoalKey<Mob> key;
    private LivingEntity target;

    public ParentTargetGoal(Mob mob) {
        this.mob = mob;
        this.key = GoalKey.of(Mob.class, NamespacedKey.fromString("mmo_mobs:parent_target"));
    }

    private LivingEntity getParent() {
        if (mob.hasMetadata("parent")) {
            try {
                Object meta = mob.getMetadata("parent").get(0).value();
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
        LivingEntity parent = getParent();
        if (parent == null || !parent.isValid()) return false;
        LivingEntity parentTarget = null;
        if (parent instanceof Mob parentMob) {
            parentTarget = parentMob.getTarget();
        }
        if (parentTarget != null && parentTarget != mob && parentTarget.isValid() && !parentTarget.isDead()) {
            this.target = parentTarget;
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
