@page fluidsimnotes Fluid Sim Notes


approaches to improve speed
 - Multithreading
 - Caching phi values between evaluations to precompute multigrid or whatever solver we're using
 - When precomputing phi, factor in density/gravity/whatever to get a better guess




multigrid improvements
 loading/unloading
 https://stackoverflow.com/questions/46468026/fast-copy-every-second-byte-to-new-memory-area


Pressure caching
 Cache value of phi from projection on previous frame (gonna have to do this per chunk (yikes!))
 Use this to populate neighbors for next frame


internal boundaries approach 1
 calculate a normal mask from border values
 normal uses 3x3x3 sample of border mask to generate an index into a lookup table that contains the normal
 (think marching cubes)
 could probably parallelize this by calculating it in parts
 ie, grab -1,-1,-1 for a whole bunch, then -1,-1,0 for a whole bunch, etc
 

edge boundaries
 "warm up" empty chunks by adding velocity to the edges of an empty chunk where it borders a non-empty chunk

extension of the "warm up" idea
 Run minified solver that just performs velocity step (no density)
 when advecting, check if density is at the advection position
 if there is density, pull it along as well and fully activate the empty chunk

==current frame==
newest u,v,w depend on density (for grav)
neighboring divergences depend on newest u,v,w
pressure field depends on neighboring divergences
final u,v,w depends on pressure field
final density depends on final u,v,w