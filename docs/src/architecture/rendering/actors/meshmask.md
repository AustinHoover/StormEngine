@page meshmask Mesh Mask

# High Level Overview
The actor mesh mask primarily keeps track of two lists:
 - A list of all meshes within the original model of the actor that SHOULD NOT be drawn
 - A list of meshes that are NOT within the original model that SHOULD be drawn
By its very nature, this class is only client facing

# Relevant Classes

[ActorMeshMask.java](@ref #electrosphere.renderer.actor.ActorMeshMask) - The main class concerned with here. Essentially just an object that holds data. Does not perform complex operations.

[Actor.java](@ref #electrosphere.renderer.actor.Actor) - Utilizes the ActorMeskMask object while doing the main render call to determine what meshes to draw/not draw.

[ClientEquipState.java](@ref #electrosphere.entity.state.equip.ClientEquipState) - Behavior Tree that performs operations on ActorMeshMask for item equip/dequip.
Particularly look at the methods `clientAttemptEquip` and `clientTransformUnequipPoint`.


# Architecture Notes
When modifying the mesh mask, you are actually queueing changes. Because the new meshes to draw may not already be in memory, they sit in a queue until asset manager gets to them.

# Usage
## Turning off a mesh on an actor
```
//loop through the Mesh Mask and turn off the meshes (by name)
ActorMeshMask meshMask = parentActor.getMeshMask();
for(String toBlock : whitelistItem.getMeshMaskList()){
    meshMask.blockMesh(modelName, toBlock);
}
```


## Drawing a mesh on an actor
```
//make sure to queue the model in asset manager so we eventually load the mesh to draw
String modelName = whitelistItem.getModel();
Globals.assetManager.addModelPathToQueue(modelName);
//loop through the Mesh Mask and add the mesh names that we want to draw
ActorMeshMask meshMask = parentActor.getMeshMask();
for(String toDraw : whitelistItem.getMeshList()){
    meshMask.queueMesh(modelName, toDraw);
}
```

