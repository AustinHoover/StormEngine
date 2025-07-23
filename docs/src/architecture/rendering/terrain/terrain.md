@page terrain Terrain

# Triplanar Mapping with Texture Atlas

## Texture Atlas Theory

It's really easy to use a single texture for a plane of geometry. Think of a heightmap that has a dirt texture applied to it. If the edges of each quad are clamped to (0,1), it will show endlessly tiling dirt.

The basic idea of the atlas texture is to pack a whole bunch of smaller textures into a larger texture and then sample based on what you want at a particular point. The engine goes through all voxel types on startup and constructs a texture atlas based on the textures provided in the data.

Here's an example of what it looks like:

![](/docs/src/images/renderer/terrain/textureAtlasExample.png)

Notice the two small parts in the bottom left. Those are the textures for two different types of voxels.

## Triplanar Mapping Theory

The basic idea of triplanar mapping is we're going to use the vertex coordinates to take "fake" photos of the camera from the x, y, and z angles and then blend them together.
![](/docs/src/images/renderer/terrain/triplanarTheoryCameras.png)

We use the normals of each vertex to determine how much to sample of the x, y, and z photos respectively. IE, if the normal points straight up, you know to only sample from the Y-aligned photo.
![](/docs/src/images/renderer/terrain/triplanarTheoryNormals.png)

These samples aren't literally photos. We're not using framebuffers to somehow get textures.

Instead, what we're technically calculating is the UVs to sample with. The idea is that we want to blend from a horizontal view to a vertical view as the texture gently curves in a hill.

This gets complicated once you want to have multiple textures.

With a single triangle of terrain, you may be sampling from three separate textures based on the terrain values at each point.
![](/docs/src/images/renderer/terrain/triplanarTheoryMultiTexture.png)

In order to make both of these systems work, we need to sample nine separate times. We have to sample:
 - For each vertex's potentially unique texture
 - For each camera angle (x, y, z)

Reference: https://catlikecoding.com/unity/tutorials/advanced-rendering/triplanar-mapping/


## Using Arrays instead of Elements

An inviolable rule of working with opengl is that to get to get the textures showing on all sides of a vertex, we will need to send duplicate vertices. Unfortunately this means we don't get to use glDrawElements.

With a normal mesh, we might send data that looks like:

Vert X  | Vert Y | Vert Z | Normal X | Normal Y | Normal Z | UV X | UV Y
------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | -------------
0  |  0  |  0  |  0  |  1  |  0  |  0  |  0
0  |  0  |  2  |  0  |  1  |  0  |  0  |  1
2  |  0  |  2  |  0  |  1  |  0  |  1  |  1

There would then be a separate table of indices into the first table where groups of three indices in the second table correspond to a single triangle

Element 1 | Element 2 | Element 3
 --- | --- | ---
0 | 1 | 2
1 | 2 | 3

However, given the note at the top of this section, we can't use this element-index scheme because we want to have the textures blend seamlessly on all sides of a given vertex.

So instead, our data may look like

Vert X  | Vert Y | Vert Z | Normal X | Normal Y | Normal Z | UV X | UV Y
------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | -------------
0  |  0  |  0  |  0  |  1  |  0  |  0  |  0
0  |  0  |  2  |  0  |  1  |  0  |  0  |  1
2  |  0  |  2  |  0  |  1  |  0  |  1  |  1
0  |  0  |  2  |  0  |  1  |  0  |  0  |  1

(Notice rows two and four are duplicate data)


## Atlas Data

### Indices

Another component required to make this scheme work is extra data to tell the mesh what texture in the atlas to sample at what point. Alongside the Vert, Normal, and UV data we also send the atlas location of the three textures to sample for the current triangle.

This would look something like the following table.
Sample Index 1 | Sample Index 2 | Sample Index 3
 --- | --- | ---
 0 | 0 | 0
 0 | 0 | 0
 0 | 0 | 0
 1 | 0 | 0
 1 | 0 | 0
 1 | 0 | 0

The first triangle would be uniformly the same texture. The second triangle would blend from the texture at atlas location "1" to the texture at atlas location "0".


### Weights

Another piece of information the fragment shader needs to know is how close to each vertex in the triangle it is. This lets the shader determine how much of each texture to pull from.

The data for this would look like
Weight 1 | Weight 2 | Weight 3
 --- | --- | ---
 1.0 | 0.0 | 0.0
 0.0 | 1.0 | 0.0
 0.0 | 0.0 | 1.0
 1.0 | 0.0 | 0.0
 0.0 | 1.0 | 0.0
 0.0 | 0.0 | 1.0

The idea is that the vertex shader will automatically interpolate between the groups of three vectors and give us a single vector of weights.

## Shader Math

The math that does all texture calculations is pretty complicated and has three main parts


This first block calculates the UVs to sample from for the camera view along the X axis. We need to sample a texture for each of the vertices. The idea is that vert1 could be a dirt texture, vert2 is grass, vert3 is stone, and we need to know how to blend between all three. The following code block calculates UVs for a single of the three textures.

``` glsl
vec2 vert1_x_uv = vec2(
    (fract(texPlane1.x) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (mod(samplerIndexVec.x,ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL),
    (fract(texPlane1.y) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (round(samplerIndexVec.x / ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL)
);
```
The naming format is vert\<which vert \>_\<which camera (x,y,z)\>_uv.

`texPlane1` is a base UV value that is passed into the fragment shader. It's calculated based on the vertex position in the mesh itself (model matrix not applied). IE In the image below, the vertex a 0,0 and 1,0 would generate UVs from 0 to 1 along the X axis.

![](/docs/src/images/renderer/terrain/polygonOverflowExplanation.png)

!!IMPORTANT POINT!! Because we are passing these values that are greated than 1 as we go along the model (2->3, 3->4, etc), when the UVs translate from the vertex to fragment shader they are re-normalized into the range [0,1]. This is important for later.

`ATLAS_NORMALIZED_ELEMENT_WIDTH` is the width of a single texture within the atlas image. Because OpenGL images all have dimensions [0,1], this variable has the value 1/32. Except, it's slightly less than 1/32. This is to keep us from sampling just past the edge of the atlas entry and into the next one (was creating weird lines).

`samplerIndexVec` Contains the index into the atlas that we want to sample for this particular vertex. It is some single integer that is modulo wrapped up the atlas.

`ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL` is precisely 1/32. This is so we properly move along the atlas. If we used `ATLAS_NORMALIZED_ELEMENT_WIDTH`, we wouldn't be sampling far enough along and would gradually fall back into previous elements.



Now that we have calculated the UVs for each of the vertices along the X axis, we actually sample for each texture and combine them to get an overall x-axis texture. The vertWeights come from data pased into the GPU as an array attrib. (this is that second table that goes 1,0,0 - 0,1,0 - 0,0,1 - 1,0,0   etc)

``` glsl
vec3 albedoX = texture(material.diffuse, vert1_x_uv).rgb * vert1Weight + texture(material.diffuse, vert2_x_uv).rgb * vert2Weight + texture(material.diffuse, vert3_x_uv).rgb * vert3Weight;
```







Finally, we sum the three different axis colors (x,y,z) and weight them based on the normals.

``` glsl
return (albedoX * weights.x + albedoY * weights.y + albedoZ * weights.z);
```

