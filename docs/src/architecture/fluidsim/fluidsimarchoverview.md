@page fluidsimarchoverview Fluid Simulation Architecture Overview

# Summary of Parts
TLDR IS ITS NOT READY YET
IT GOTTA COOK MORE
The fluid system is structured very similar to the terrain manager.

Make sure to explain:
setting iso levels based on neighbor levels
Client side interpreting 0 as -1 when receiving chunk from network (for rendering purposes)
Overwriting same cache value on client side
client side chunk destroying old model incl from asset manager on update
Generic fluid simulator and specifically cellular automata ver
How to add a fluid generator/destructor via the fluid simulator
Rules of the cellular automata currently