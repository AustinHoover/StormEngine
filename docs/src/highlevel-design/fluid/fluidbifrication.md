@page fluidbifrication Fluid Bifrication

Goals is to have at least two systems that perform fluid dynamics. System 1 is a full CFD. System 2 is a simpler cellular automata.




# System 1 (Computational Fluid Dynamics (CFD))

As this is visually and gameplay wise the most interesting, all chunks closest to players will have this calculation running.






# System 2 (Cellular Automata)

@subpage fluidcellularautomata

For chunks that are just chunkloaded or otherwise not close to the player, rely on a cellular automata approach.
This can also dynamically be used to salvage fps. If the player has a low end machine and is hosting a multiplayer world,
the engine can try to remedy that by swapping active chunks into cellular automata lane. It should end up being substantially
faster to simulate given its nature.

