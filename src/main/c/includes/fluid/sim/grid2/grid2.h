
#ifndef FLUID_GRID2_MAINFUNC
#define FLUID_GRID2_MAINFUNC

#include "public.h"
#include "fluid/env/environment.h"








/**
 * Performs the main simulation
 * @param numChunks The number of chunks
 * @param passedInChunks The chunks to simulate
 * @param environment The environment data
 * @param timestep The timestep to simulate by
 */
LIBRARY_API void fluid_grid2_simulate(int numChunks, Chunk ** passedInChunks, Environment * environment, float timestep);




















#endif