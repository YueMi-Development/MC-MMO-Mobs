# MC-MMO-Mobs

Custom mob system for PaperMC servers. Creates custom mobs with configurable stats, equipment, AI behaviors, and skill integration.

## Stack
- PaperMC 1.21+, Java 21, Gradle (Kotlin DSL)
- Soft-depends: YueMiLibs, MmoMechanics

## Mob Configuration (JSON5)
```json5
{
  name: "<yellow>Topaz Knight",
  type: "SKELETON",
  health: 120,
  speed: 0.30,
  tags: ["topaz", "knight"],
  skills: [{ skill: "lightning_radius_storm", trigger: "onAttack" }],
  equipment: {
    hand: "minecraft:iron_sword"
  }
}
```

## Key Features
- 100+ configurable mob options (Universal + Entity-Specific)
- Custom AI Goals (Patrol, GoToOwner, FleePlayers, etc.)
- Target Goals (NearestTarget, OwnerTarget, ParentTarget, etc.)
- Skill triggers via MmoMechanics

## API Access
```java
MmoMobsApi api = MmoMobsApiProvider.getApi();
api.isCustomMob(entity);
api.spawnCustomMob("topaz_knight", location);
api.getCustomMobTags(entity);
```

## Conventions
- Packages: `org.yuemi.mmomobs.{api,plugin}`
- Mob configs in `mobs/*.json5`
- AI goals in `plugin.mob.options.goals/{behavior,target}/`
