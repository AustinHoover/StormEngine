@page entitySpawning Entity Spawning

Recommended flow and tips for spawning entities from the server


## High Level Overview

#### Spawning a creature
```
CreatureTemplate template = <get creature template somehow>;
String raceName = template.getCreatureType();
//spawn creature in world
Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
Entity newCreature = CreatureUtils.serverSpawnBasicCreature(realm,new Vector3d(Globals.spawnPoint.x,Globals.spawnPoint.y,Globals.spawnPoint.z),raceName,template);
```

#### Spawning a plant
```
CreatureTemplate template = <get creature template somehow>;
String raceName = template.getCreatureType();
//spawn creature in world
Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
Entity newCreature = CreatureUtils.serverSpawnBasicCreature(realm,new Vector3d(Globals.spawnPoint.x,Globals.spawnPoint.y,Globals.spawnPoint.z),raceName,template);
```



## Major Usage Notes
 - TODO












## Main Classes

[CreatureUtils.java](@ref #electrosphere.entity.types.creature.CreatureUtils) - Provides utilities for spawning creatures into the game world for both server and client




## Code Organization and Best Practices

#### Startup


#### Usage











## Terminology













## Future Goals
