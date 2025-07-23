@page terrainediting Terrain Editing
electrosphere.client.terrain.editing.TerrainEditing
 - Client static interface for editing terrain
 - The idea is that this provides functions you can call anywhere from client side to trigger a request to perform a terrain edit

Which leads to

electrosphere.server.terrain.editing.TerrainEditing
 - Server utility functions for actually editing terrain
 - Does the calculations of a real coordinate + radius to determine which cells to edit and how much
 - This then updates the server terrain manager with edits via the VoxelCellManager interface

VoxelCellManager interface
 - Provides an interface on top of DataCellManager to update terrain functions
 - Makes functions that must be implemented on data cell manager so implementation specific to cell manager
 - For GriddedDataCellManager, this uses a lock and updates values
 - As values are updated, they should be send 1-by-1 over the network via individual update packets to the client

When client receives voxel update packet in ClientTerrainManager, it triggers the cell to update that specific drawcell
This should also update all ambient foliage

