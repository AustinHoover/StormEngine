@page instancingarch Instancing Architecture

Idea is to have two parallel approaches to buffers that are pushed into gpu
One is traditional actor architecture where you call draw on an actor and it puts just its info into InstanceData object (think close trees, rocks, etc)
Other approach is to have an object that represents a bucket of data. You call draw on the bucket and it pushes an array of info a texture (think grass)
 - This texture is then iterated over by vertex shader

Both push data into InstanceData, which is then iterated over in draw calls by instanceManager