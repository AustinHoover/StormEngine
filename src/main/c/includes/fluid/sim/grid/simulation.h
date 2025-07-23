#ifndef SIMULATION_H
#define SIMULATION_H

#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"

/**
 * Performs the main simulation
 * @param numChunks The number of chunks
 * @param passedInChunks The chunks to simulate
 * @param environment The environment data
 * @param timestep The timestep to simulate by
 */
void fluid_grid_simulate(int numChunks, Chunk ** passedInChunks, Environment * environment, float timestep);

#endif