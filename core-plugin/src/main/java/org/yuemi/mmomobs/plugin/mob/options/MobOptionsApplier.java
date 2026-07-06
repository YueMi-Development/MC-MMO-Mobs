package org.yuemi.mmomobs.plugin.mob.options;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MobOptionsApplier {

    public static void applyUniversalOptions(@NotNull Entity entity, @Nullable MobOptionsDto options) {
        if (options == null) {
            return;
        }

        // AlwaysShowName
        if (options.alwaysShowName() != null) {
            entity.setCustomNameVisible(options.alwaysShowName());
        }

        // Glowing
        if (options.glowing() != null) {
            entity.setGlowing(options.glowing());
        }

        // Invincible
        if (options.invincible() != null) {
            entity.setInvulnerable(options.invincible());
        }

        // Silent
        if (options.silent() != null) {
            entity.setSilent(options.silent());
        }

        // Gravity
        if (options.noGravity() != null) {
            entity.setGravity(!options.noGravity());
        }

        // Invisible / VisibleByDefault
        if (options.invisible() != null && options.invisible()) {
            entity.setInvisible(true);
        } else if (options.visibleByDefault() != null && !options.visibleByDefault()) {
            entity.setInvisible(true);
        }

        if (entity instanceof LivingEntity living) {
            // NoAI
            if (options.noAI() != null) {
                living.setAI(!options.noAI());
            }

            // Collidable
            if (options.collidable() != null) {
                living.setCollidable(options.collidable());
            }

            // AttackSpeed
            if (options.attackSpeed() != null) {
                var attr = living.getAttribute(Attribute.ATTACK_SPEED);
                if (attr != null) {
                    attr.setBaseValue(options.attackSpeed());
                }
            }

            // FollowRange
            if (options.followRange() != null) {
                var attr = living.getAttribute(Attribute.FOLLOW_RANGE);
                if (attr != null) {
                    attr.setBaseValue(options.followRange());
                }
            }

            // KnockbackResistance
            if (options.knockbackResistance() != null) {
                var attr = living.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
                if (attr != null) {
                    attr.setBaseValue(options.knockbackResistance());
                }
            }

            // MovementSpeed
            if (options.movementSpeed() != null) {
                var attr = living.getAttribute(Attribute.MOVEMENT_SPEED);
                if (attr != null) {
                    attr.setBaseValue(options.movementSpeed());
                }
            }

            // Scale
            if (options.scale() != null && options.scale() != -1) {
                var attr = living.getAttribute(Attribute.SCALE);
                if (attr != null) {
                    attr.setBaseValue(options.scale());
                }
            }

            // Despawn
            if (options.despawn() != null) {
                String d = options.despawn().toUpperCase();
                if (d.equals("FALSE") || d.equals("NO") || d.equals("NEVER") || d.equals("PERSISTENT")) {
                    living.setRemoveWhenFarAway(false);
                } else if (d.equals("TRUE") || d.equals("YES") || d.equals("NORMAL")) {
                    living.setRemoveWhenFarAway(true);
                }
            }

            // NoDamageTicks
            if (options.noDamageTicks() != null) {
                living.setNoDamageTicks(options.noDamageTicks());
                living.setMaximumNoDamageTicks(options.noDamageTicks());
            }

            // PreventItemPickup
            if (options.preventItemPickup() != null) {
                living.setCanPickupItems(!options.preventItemPickup());
            }
        }
    }
}
