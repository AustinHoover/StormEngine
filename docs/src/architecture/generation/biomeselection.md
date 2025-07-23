@page biomeselection Biome Selection


Details on how biome selection works

The world is segmented into "zones" via voronoi partitioning

These zones have themes attached to them

The themes filter the biomes that can be picked from the overall biome pool

The biomes are also filtered by terrain attributes (elevation, moisture, temperature, magic count, etc)

The biome pool is then mapped to regions which are indexed into based on noise functions

Once the biome is selected, a fine grain surface value is calculated

All other generation proceeds based on the specific biome selected from the pool via noise

This biome selection should be supercede-able in certain situations
 - Rivers