@page structuresandbuildings Structures and Buildings

For this, I think we're going to try for 0.25 meter cube voxels in a separate mesh from the terrain. This will allow for LOD at long distance with good scaling.

0.25 meter cubed voxels are going to be 256kb/chunk uncompressed. That's 250mb/1000 chunks

Problems to be solved:
LOD alongside terrain marching cubes

When in building mode, have the ability to place 'parts', ie a section of wall, a section of roof, a beam, etc