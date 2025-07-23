@page worldstorage World Storage

# Uses of Data
 - Generating actual terrain for player to walk around on
 - Pathfinding in macro phase
 - Placement of objects in macro phase

# On Disk

```
For things like elevation map, there are different sizes we can store
If 1000x1000 is approximately 1MB
10000x10000 is gonna be 100MB
4000x4000 is ~16mb
```
Types of data we need:
 - Elevation map for surface
 - hydration
 - temperature
 - wind speed
 - magic (?)


Also stored to disk is the zone map
This is stored in the format of a pixel map that maps pixels to zone IDs
These IDs map to a list of zone definitions


# In Memory

