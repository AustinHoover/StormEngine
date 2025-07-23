@page WeavingMeshes Weaving Meshes



## High Level Overview
The idea of this page is to track notes about creating multi-mesh models where the meshes are supposed to behave as if they're a singular mesh (think the main human model).




## Removing seams in the meshes
### Mirroring weights
The Data Transfer modifier can be used to copy weights from one mesh to another.

![](/docs/src/images/blender/blenderDataTransferOverview.png)
The basic idea is that, for each mesh in the "client" mesh, it looks for the closest vertex in the "main" mesh that is within a certain radius of the current client vertex. If a close vertex is found, for each vertex group in the client mesh, it copies the weight value of that group from the main mesh vertex into the client mesh vertex.

There are a few settings that are important to get right.








### Setting up the data transfer modifier
Set the source mesh to the one that weights should be copied from.

![](/docs/src/images/blender/blenderDataTransferSource.png)
Set mix mode to Replace

![](/docs/src/images/blender/blenderDataTransferMixMode.png)
Select the Vertex Data checkbox

![](/docs/src/images/blender/blenderDataTransferVertexData.png)
Select the Vertex Groups badge

![](/docs/src/images/blender/blenderDataTransferVertexGroups.png)
Make sure the Mapping is set to "Nearest Vertex"

![](/docs/src/images/blender/blenderDataTransferNearest.png)
Lastly, check the Max Distance box and set the distance to a reasonable value. 0.0005 has worked pretty well thusfar

![](/docs/src/images/blender/blenderDataTransferDistance.png)
Fin







### Weights work
One thing to keep in mind is that the engine supports maximum 4 bones per mesh. If you have more than 4 bones in Blender, it should automatically minify it on export. However, if you want to make what you see in blender EXACTLY what you get in engine, it's best to cull the bones on all meshes to be a maximum of four.

#### !!DANGER!!
If you cull all the bones, make SURE the weights on the main mesh along the seam with a client mesh ONLY have bones that are the same as the client mesh.

#### Example
In this example, we have a shoulder mesh and a bicep mesh. They have overlap, and the requisite modifiers; however, the vertex groups for the shoulder have not been modified to account for the bicep being a child of it.

As shown here, the shoulder has four bones

![](/docs/src/images/blender/blenderWeightsworkShoulderGroups.png)
Meanwhile, the bicep only has three bones

![](/docs/src/images/blender/blenderWeightsworkBicepGroups.png)
The Breast bone has weights along the seam of the shoulder, but the bicep doesn't have the breast bone as a modifier

![](/docs/src/images/blender/blenderWeightsWorkShoulderWeightsView.png)
Because the bicep doesn't have the breast bone weights, it doesn't deform quite the same way as the shoulder bone and it creates a seam

![](/docs/src/images/blender/blenderWeightsworkSeam.png)
Once these edge vertices in the shoulder are updated to not use the breast bone, the bicep seam disappears

![](/docs/src/images/blender/blenderWeightsworkNoSeam.png)
Fin

