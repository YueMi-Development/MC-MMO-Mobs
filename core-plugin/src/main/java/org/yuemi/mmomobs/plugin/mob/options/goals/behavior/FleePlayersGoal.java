package org.yuemi.mmomobs.plugin.mob.options.goals.behavior;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.EnumSet;

public final class FleePlayersGoal implements Goal<Mob> {
    private final Mob mob;
    private final GoalKey<Mob> key;
    private final double radius;
    private final double speed;

    public FleePlayersGoal(Mob mob, double radius, double speed) {
        this.mob = mob;
        this.key = GoalKey.of(Mob.class, NamespacedKey.fromString("mmo_mobs:flee_players"));
        this.radius = radius;
        this.speed = speed;
    }

    private Player getNearbyPlayer() {
        Collection<Player> players = mob.getWorld().getNearbyEntitiesByType(Player.class, mob.getLocation(), radius, radius, radius);
        if (players.isEmpty()) return null;
        Player closest = null;
        double closestDistSq = Double.MAX_VALUE;
        for (Player p : players) {
            double distSq = mob.getLocation().distanceSquared(p.getLocation());
            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = p;
            }
        }
        return closest;
    }

    @Override
    public boolean shouldActivate() {
        return getNearbyPlayer() != null;
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void tick() {
        Player player = getNearbyPlayer();
        if (player != null) {
            Vector dir = mob.getLocation().toVector().subtract(player.getLocation().toVector());
            if (dir.lengthSquared() == 0) {
                dir = new Vector(1, 0, 0);
            }
            dir.normalize().multiply(5);
            org.bukkit.Location target = mob.getLocation().add(dir);
            mob.getPathfinder().moveTo(target, speed);
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
