# MC-MMO-Mobs

Custom mob system for PaperMC servers. Spawns configured mobs with stats, equipment, AI goals, and skill integration.

## Stack
- PaperMC 1.21+, Java 21, Gradle (Kotlin DSL), JSON5, MiniMessage
- Dependencies: Paper API, YueMiLibs, MC-MMO-Mechanics

## API
- `MmoMobsApiProvider.getApi()` - Main API
- `isCustomMob()`, `getCustomMobType()`, `spawnCustomMob()`

## Conventions
- Packages: `org.yuemi.mmomobs.{api,plugin,plugin.command,plugin.listener,plugin.mob}`
- Classes: PascalCase, `*Dto`, `*Command`, `*Listener`, `*Goal`, `*Manager`
- Methods/vars: camelCase, CONSTANTS: UPPER_SNAKE_CASE

## AI Goals
- **Behavior**: PatrolGoal, GoToOwnerGoal, GoToLocationGoal, FleePlayersGoal, DoNothingGoal
- **Target**: NearestTargetGoal, OwnerTargetGoal, ParentTargetGoal, OwnerAttackerGoal

## Best Practices
- Check null for attributes
- Use Optional for nullable returns
- ConcurrentHashMap for mob tracking
- Validate entity types before spawn

## Build
```
./gradlew build
```
