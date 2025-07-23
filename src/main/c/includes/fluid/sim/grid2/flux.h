#ifndef FLUID_GRID2_FLUX_H
#define FLUID_GRID2_FLUX_H

#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"

/**
 * Value where there is no pressure
 */
#define NO_PRESSURE 0

/**
 * Updates the flux stored in the ghost cells of this chunk
 */
void fluid_grid2_update_ghost_flux(Environment * environment, Chunk * chunk);


#endif