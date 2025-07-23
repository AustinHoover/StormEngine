@page optimizationideas Optimization Ideas

Automatically transition between instanced actors and non-instanced actors for non-animated models
 - Need to keep track of number of entities drawing a model, once that total passes a threshold convert them all to instanced actors
 - name it something fun like "HybridActor" or "ShapeshiftActor"
 - Have a plane-based SUPER LOD version of models (like trees)
 - Actor transitions to just plane when far enough away

Merge all kinematic bodies in a scene into one
 - Need to keep track of individual entities' shapes after the merge
 - When an individual entity dies, remove its shape from the main body

Second frustum full on shadow map pipeline
 - only draw what is both in front of camera AND visible from sky (currently ignores sky's view)

Move hitbox position updates to behavior tree
 - If an entity is not moving (ie a tree), don't have to update the hitbox's position

On client, only colide hitboxes closest to the player
 - Currently collide all hitboxes and it's costly

Simplify collidable logic
 - IE trees don't need to worry about gravity

In GriddedDataCellManager, skip ServerDataCells if they aren't doing anything interesting

In GriddedDataCellManager, run unload logic at the same time we're iterating through all cells to simulate the

