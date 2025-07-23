@page terraingenerationideas Terrain Organization

```
generate 200 x 200
interpolate x 20 in each direction
this map will be 0.25 km resolution and ~62mb in size
Useful for macro sim
```

pick points to place mountains at

use curves to generate rivers in data

for gameplay:
 - interpolate x 100 in each direction in memory for local areas to player
 - finally chunks are 16 x 16 x 16


```
sines going in x
cosines going in z
at different frequencies, with modifiers based on the percentage of each biome in the current coordinate
add the sines + cosines to the current interpolated height
Have a couple different instances of simplex noise at different frequencies and amplitudes that get phased in/out based on biome


2d perlin noise where "activation zone" is only values >0.9
when in activation zone x,z, have 3d perlin noise down to a given point that carves cavities
^should generate earthen pillars
```
