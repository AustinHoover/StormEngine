@page physics Physics Engine

## High Level Overview
The goal of the physics engine is to wrap around the collision engine to allow physics to occur within the game.




## Major Usage Notes
Check Collision Engine Usage Notes as well. They are pretty relevant typically.



## Main Classes
[PhysicsEntityUtils.java](@ref #electrosphere.collision.PhysicsEntityUtils) - Contains functions for creating and attaching rigid bodies to entities so that they obey physics.

[PhysicsUtils.java](@ref #electrosphere.collision.PhysicsUtils) - Contains utilities for physics operations (unit transforms, etc)



## Code Organization and Best Practices



## Examples








## Future Goals

 - Library Scoped Physics Objects - Create an item, ball, etc, that uses the Ode4J physics calculations to figure out its next position.