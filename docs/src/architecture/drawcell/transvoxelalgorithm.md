@page transvoxelalgorithm Transvoxel Chunk Generation



# High Level


The goal of the transvoxel algorithm is to bridge the divide between a chunk of a higher resolution and a chunk of a lower resolution

![](/docs/src/images/architecture/drawcell/completevoxel.png)

For the voxels on the border between a low resolution chunk and a high resolution chunk, we split the voxels in half

![](/docs/src/images/architecture/drawcell/bisectedvoxel.png)

On the low-resolution half, we perform marching cubes

On the high resolution half, we generate a mesh that adapts the high resolution chunk to the low resolution voxel

![](/docs/src/images/architecture/drawcell/adaptedvoxel.png)

# Major files
[TransvoxelModelGeneration.java](@ref #electrosphere.renderer.meshgen.TransvoxelModelGeneration) - The main class that turns voxel data into mesh data

[TerrainChunk.java](@ref #electrosphere.entity.types.terrain.TerrainChunk) - The class that the DrawCellManager calls to generate chunk meshes

[ClientTerrainManager.java](@ref #electrosphere.client.terrain.manager.ClientTerrainManager) - Handles queueing the mesh data to the gpu

[TerrainChunkGenQueueItem.java](@ref #electrosphere.client.terrain.manager.TerrainChunkGenQueueItem) - A terrain mesh that has been queued to be added to the gpu




# Implementation Notes

The description of the algorithm always refers to the transition cells in terms of y pointing upwards and x pointing to the right.
This can be confusing to think about how it would apply to other orientations (ie what if we're working along the y axis or something and x is "up").
All these transforms are done implicitly in the `generateTerrainChunkData` function. When iterating across each axis, I've manually calculated what point should be where.
The inner polygonize functions always treat it as y-up, but that works because this transform has already been done before the data is passed in.




# Notes about table format
 - `transitionCellClass` is indexed into by the case index value generated from the high resolution face data
 - `transitionCellData` is indexed into by the class value from the `transitionCellClass`
 - `transitionVertexData` is ALSO indexed into by the CASE INDEX VALUE, NOT THE CLASS VALUE. You can figure this out because the `transitionVertexData` has 512 entries


