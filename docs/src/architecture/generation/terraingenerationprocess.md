@page terraingenerationprocess Terrain Generation Process

have a heightmap generated by tectonic sim
is interpolated to 2000x2000
saved as a byte per pixel
~~~~~this map is used to determine what type of noise to sample from (ie mountain noise vs ocean noise vs plains noise)~~~~~
generate a separate map for biomes that is also 2000x2000
this is stored as shorts, where value is the id of the biome
"civilization" gradient map generated with simplex noise (1byte resolution)
"magic" gradient map generated with simplex noise (1 byte)
hydration gradient map (1byte)
"element" (ie fire, water, etc) map (1byte) (??)
start biome gen by just using simplex noise to pick a biome
filter selectable biomes based on elevation (precompute buckets to pull from)


generate a biome map for each underground level (3 * 2 * 2000 * 2000 ~ 24mb)



generate a biome map for each sky level (another 24mb)
for sky biome, floodfill non empty biomes and calculate centroid
then, have distance from centroid determine vertical width of island at that point
centroid distance mixed with jagged noise to not make it uniform
can have a dedicated file for storing all islands


once biomes are selected, can precompute and cache a rough lookup map of elevations (every 500 meters)
assuming elevation is an int, size should be 4 * 4 * 2000 * 2000 ~ 64mb
this rough lookup map is probably good enough for placing towns, roads, features, etc
unfortunately, probably want to do this for all sky levels and underground levels
for underground levels, want quick lookup for both ceiling and floor


i want to solve for "zones" for political tracking
zone must be contiguous and at least 4km squared
max width of 64km squared
pick random points and flood fill based on biome
once most area is designated, attach straggler pixels to nearest zone
store details (ie centroid) in advanced datastructure
this will be beefy (prob 50mb) because need to store pixel positions


when generating actual voxels, take biome values from four corners and weight how much they effect the generation algo based om proximity to corner
need definition of adjacency cases
ie, if highlands is next to ocean, take both of their weights and use it to generate cliffs instead of a gradual gradient
adjacency cases can be in biome definition
define hard codes generation techniques (ie cliffs)


resources
ores - each biome defines ores that can aplear within. the noise algo samples from closes biome to select ores
vegetation - use 2d noise to select along the surface based on a pool stored in the biome
game trails - precomputed at the 1kmX1km level


when divying up biomes, store list of pixels that have biomes that support different generation techniques
ie, store plains pixels as "good for towns"
store fire pixels as "good for fire civilization"


when a pixel changes biome, must evict it from all these structures and recalculate which ones it should be within (except maybe zone)





Solve for the heightmap at a given x-z for the surface
Then, have "cells" placed above the surface based on the base height offset of the biome
These cells can be populated with larger structures



