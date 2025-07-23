@page collision Collision Engine


[TOC]

## High Level Overview
The goal of the collision engine system is to allow parallel collision detection of different classes of objects.

For instance, you could have a collision system dedicated to fire and things that are flammable, where only objects in one of those two categories are present. Or, you could have a collision system dedicated to interactible object prompts, where if the player entity is within one of these zones it performs some prompting logic.

The big case for this engine is the main physics system. The goal is to provide different classes of things for entities to collide with such that they can control the collision physics differently (think a tree vs the ground vs a slippery floor).



![](/docs/src/images/CollisionEngineEntityFlow.png)









## Major Usage Notes

 - All geometries are aligned along z by default in the library (ie your cylinders will be on their sides)
 - All functions that transform the scale of a DBody only transform the first shape within the rigid body. This should likely eventually be updated to support multiple shapes in a body.
 - Furthermore, these transform functions require the CollidableTemplate to modify scale. Ode4J doesn't provide a "scale" function, instead it lets you change the core params. To scale, must know the original size.























## Main Classes

[CollisionEngine.java](@ref #electrosphere.collision.CollisionEngine) - Represents a specific collision system. It may be helpful to think of it as viewing the world through a specific lens. Keeps track of all entities that do its type of collisions and fires callbacks on collision. Should be updated each tick.

[Collidable.java](@ref #electrosphere.collision.collidable.Collidable) - Contains the collision information for a single object in a given collision system. Stores both the description of the collidable (is it a tree, a frog, or the ground, etc) as well as a list of impulses to be applied.

[CollisionBodyCreation.java](@ref #electrosphere.collision.CollisionBodyCreation) - Contains functions for creating rigid bodies of different types (cube, sphere, trimesh, etc)










## Library Explanation

The library currently in use is Ode4J. There are a couple main classes that will be explained now.

 - DSpace - The main class representing the overall simulation
 - DWorld - A 'world' within the space that can have geometries inside it that collide. Must be used to create bodies. Probably stores stuff like gravity.
 - DBody - A rigid body that has some physical properties. Can contain many different geometries.
 - DGeom - A geometry shape (capsule, box, etc)












## Code Organization and Best Practices

#### Startup
Each client scene creates a collision engine for physics on connection. Each scene the server manages also creates a collision engine for physics.


#### Usage











## Terminology
 - Physics Entity
 - Dynamic Physics Entity
 - Collision Object - A collidable
 - ODE Category - Ode has bits that control which 'categories' a given geometry falls in to. This is used to control behaviors applied to it.
 - ODE Collision Bits - The mask of categories that a given geometry should collide with









## Known Bugs To Fix
 - There needs to be a better way to scale a collidable attached to an entity. Currently the best approach I see is write a custom behavior tree to update the collidable scale every frame (this is how the collidable tree works). At the very least a tree that doesn't move the entity when it collides. This then needs to be added to the trees lol



## Future Goals

 - Ability to turn off impulse generation for when we purely care about whether things are colliding or not (hitboxes, fire system, ui system, etc)
 - As always, code organization