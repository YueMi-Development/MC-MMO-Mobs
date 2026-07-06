package org.yuemi.mmomobs.plugin.mob.options;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
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

    public static void applySpecificOptions(@NotNull Entity entity, @Nullable MobOptionsDto options) {
        if (options == null) {
            return;
        }

        // Ageable / Breedable
        if (entity instanceof Ageable ageable) {
            if (options.age() != null) ageable.setAge(options.age());
            if (options.ageLock() != null) ageable.setAgeLock(options.ageLock());
            if (options.adult() != null && options.adult()) ageable.setAdult();
            if (options.baby() != null && options.baby()) ageable.setBaby();
        }

        // Tameable
        if (entity instanceof Tameable tameable) {
            if (options.tamed() != null) tameable.setTamed(options.tamed());
            // If option Tameable is false, we can handle it or let it default.
        }

        // Raider
        if (entity instanceof Raider raider) {
            if (options.canJoinRaid() != null) raider.setCanJoinRaid(options.canJoinRaid());
            if (options.patrolLeader() != null) raider.setPatrolLeader(options.patrolLeader());
        }

        // Slimes & Magma Cubes
        if (entity instanceof Slime slime) {
            if (options.size() != null) slime.setSize(options.size());
        }

        // Armor Stand
        if (entity instanceof ArmorStand armorStand) {
            if (options.canMove() != null) armorStand.setCanMove(options.canMove());
            if (options.canTick() != null) armorStand.setCanTick(options.canTick());
            if (options.hasArms() != null) armorStand.setArms(options.hasArms());
            if (options.hasBasePlate() != null) armorStand.setBasePlate(options.hasBasePlate());
            if (options.small() != null) armorStand.setSmall(options.small());
            if (options.marker() != null) armorStand.setMarker(options.marker());

            // Equipment on ArmorStand from custom items if defined
            try {
                org.yuemi.libs.api.items.ItemsApi itemsApi = org.yuemi.libs.api.items.ItemsApiProvider.getApi();
                if (itemsApi != null && armorStand.getEquipment() != null) {
                    if (options.itemHead() != null) armorStand.getEquipment().setHelmet(itemsApi.getItem(options.itemHead(), 1));
                    if (options.itemBody() != null) armorStand.getEquipment().setChestplate(itemsApi.getItem(options.itemBody(), 1));
                    if (options.itemLegs() != null) armorStand.getEquipment().setLeggings(itemsApi.getItem(options.itemLegs(), 1));
                    if (options.itemFeet() != null) armorStand.getEquipment().setBoots(itemsApi.getItem(options.itemFeet(), 1));
                    if (options.itemHand() != null) armorStand.getEquipment().setItemInMainHand(itemsApi.getItem(options.itemHand(), 1));
                    if (options.itemOffhand() != null) armorStand.getEquipment().setItemInOffHand(itemsApi.getItem(options.itemOffhand(), 1));
                }
            } catch (Throwable ignored) {}
        }

        // Bee
        if (entity instanceof Bee bee) {
            if (options.anger() != null) bee.setAnger(options.anger());
            if (options.hasNectar() != null) bee.setHasNectar(options.hasNectar());
            if (options.hasStung() != null) bee.setHasStung(options.hasStung());
        }

        // Saddleable (Pig, Strider, Horses, Camel)
        if (options.saddled() != null) {
            if (entity instanceof org.bukkit.entity.Pig pig) {
                pig.setSaddle(options.saddled());
            } else if (entity instanceof org.bukkit.entity.Strider strider) {
                strider.setSaddle(options.saddled());
            } else if (entity instanceof org.bukkit.entity.AbstractHorse horse) {
                if (options.saddled()) {
                    horse.getInventory().setSaddle(new org.bukkit.inventory.ItemStack(org.bukkit.Material.SADDLE));
                } else {
                    horse.getInventory().setSaddle(null);
                }
            }
        }

        // Cat
        if (entity instanceof Cat cat) {
            if (options.catType() != null) {
                try {
                    cat.setCatType(Cat.Type.valueOf(options.catType().toUpperCase()));
                } catch (Exception ignored) {}
            }
            if (options.collarColor() != null) {
                try {
                    cat.setCollarColor(org.bukkit.DyeColor.valueOf(options.collarColor().toUpperCase()));
                } catch (Exception ignored) {}
            }
        }

        // Creeper
        if (entity instanceof Creeper creeper) {
            if (options.explosionRadius() != null) creeper.setExplosionRadius(options.explosionRadius());
            if (options.fuseTicks() != null) creeper.setMaxFuseTicks(options.fuseTicks());
            if (options.superCharged() != null) creeper.setPowered(options.superCharged());
        }

        // Fox
        if (entity instanceof Fox fox) {
            if (options.foxType() != null) {
                try {
                    fox.setFoxType(Fox.Type.valueOf(options.foxType().toUpperCase()));
                } catch (Exception ignored) {}
            }
        }

        // Frog
        if (entity instanceof Frog frog) {
            if (options.variant() != null) {
                try {
                    frog.setVariant(Frog.Variant.valueOf(options.variant().toUpperCase()));
                } catch (Exception ignored) {}
            } else if (options.type() != null) {
                try {
                    frog.setVariant(Frog.Variant.valueOf(options.type().toUpperCase()));
                } catch (Exception ignored) {}
            }
        }

        // Goat
        if (entity instanceof Goat goat) {
            if (options.screaming() != null) goat.setScreaming(options.screaming());
        }

        // Hoglin / Piglin zombification & hunting
        if (entity instanceof Hoglin hoglin) {
            if (options.immuneToZombification() != null) hoglin.setImmuneToZombification(options.immuneToZombification());
            if (options.huntable() != null) hoglin.setIsAbleToBeHunted(options.huntable());
        }
        if (entity instanceof Piglin piglin) {
            if (options.immuneToZombification() != null) piglin.setImmuneToZombification(options.immuneToZombification());
            if (options.ableToHunt() != null) piglin.setIsAbleToHunt(options.ableToHunt());
        }

        // Horses / Donkeys / Mules
        if (entity instanceof Horse horse) {
            if (options.horseColor() != null) {
                try {
                    horse.setColor(Horse.Color.valueOf(options.horseColor().toUpperCase()));
                } catch (Exception ignored) {}
            }
            if (options.horseStyle() != null) {
                try {
                    horse.setStyle(Horse.Style.valueOf(options.horseStyle().toUpperCase()));
                } catch (Exception ignored) {}
            }
            if (options.horseArmor() != null) {
                try {
                    org.bukkit.Material mat = org.bukkit.Material.valueOf((options.horseArmor() + "_horse_armor").toUpperCase());
                    horse.getInventory().setArmor(new org.bukkit.inventory.ItemStack(mat));
                } catch (Exception ignored) {}
            }
        }
        if (entity instanceof ChestedHorse chested) {
            if (options.carryingChest() != null) chested.setCarryingChest(options.carryingChest());
        }
        if (entity instanceof Llama llama) {
            if (options.carryingChest() != null) llama.setCarryingChest(options.carryingChest());
            if (options.color() != null) {
                try {
                    llama.setColor(Llama.Color.valueOf(options.color().toUpperCase()));
                } catch (Exception ignored) {}
            }
        }

        // Interaction
        if (entity instanceof Interaction interaction) {
            if (options.height() != null) interaction.setInteractionHeight(options.height().floatValue());
            if (options.width() != null) interaction.setInteractionWidth(options.width().floatValue());
            if (options.responsive() != null) interaction.setResponsive(options.responsive());
        }

        // Iron Golem
        if (entity instanceof IronGolem golem) {
            if (options.playerCreated() != null) golem.setPlayerCreated(options.playerCreated());
        }

        // Panda
        if (entity instanceof Panda panda) {
            if (options.mainGene() != null) {
                try {
                    panda.setMainGene(Panda.Gene.valueOf(options.mainGene().toUpperCase()));
                } catch (Exception ignored) {}
            }
            if (options.hiddenGene() != null) {
                try {
                    panda.setHiddenGene(Panda.Gene.valueOf(options.hiddenGene().toUpperCase()));
                } catch (Exception ignored) {}
            }
        }

        // Parrot
        if (entity instanceof Parrot parrot) {
            if (options.variant() != null) {
                try {
                    parrot.setVariant(Parrot.Variant.valueOf(options.variant().toUpperCase()));
                } catch (Exception ignored) {}
            }
            // (Note: parrot.setFlyingSpeed is not directly standard API, so we skip or let it default)
        }

        // Rabbit
        if (entity instanceof Rabbit rabbit) {
            if (options.rabbitType() != null) {
                try {
                    rabbit.setRabbitType(Rabbit.Type.valueOf(options.rabbitType().toUpperCase()));
                } catch (Exception ignored) {}
            }
        }

        // Snow Golem
        if (entity instanceof Snowman snowman) {
            if (options.derp() != null) snowman.setDerp(options.derp());
        }

        // Villager
        if (entity instanceof Villager villager) {
            if (options.profession() != null) {
                try {
                    villager.setProfession(Villager.Profession.valueOf(options.profession().toUpperCase()));
                } catch (Exception ignored) {}
            }
            if (options.type() != null) {
                try {
                    villager.setVillagerType(Villager.Type.valueOf(options.type().toUpperCase()));
                } catch (Exception ignored) {}
            }
            if (options.level() != null) {
                villager.setVillagerLevel(options.level());
            }
        }

        // Zombie Villager
        if (entity instanceof ZombieVillager zombieVillager) {
            if (options.zombieVillagerProfession() != null) {
                try {
                    zombieVillager.setVillagerProfession(Villager.Profession.valueOf(options.zombieVillagerProfession().toUpperCase()));
                } catch (Exception ignored) {}
            }
        }

        // General Colorable Collar color
        if (entity instanceof org.bukkit.entity.Wolf wolf) {
            if (options.color() != null) {
                try {
                    wolf.setCollarColor(org.bukkit.DyeColor.valueOf(options.color().toUpperCase()));
                } catch (Exception ignored) {}
            }
            if (options.variant() != null) {
                try {
                    org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(options.variant().toLowerCase());
                    org.bukkit.entity.Wolf.Variant variantObj = org.bukkit.Registry.WOLF_VARIANT.get(key);
                    if (variantObj != null) {
                        wolf.setVariant(variantObj);
                    }
                } catch (Throwable ignored) {}
            }
        }
    }
}
