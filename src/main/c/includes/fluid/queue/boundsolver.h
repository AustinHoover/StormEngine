#ifndef FLUID_BOUNDSOLVER_H
#define FLUID_BOUNDSOLVER_H

#include "public.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"



/**
 * Should check the neighbor when filling
 */
#define fluid_solve_bounds_SKIP_NEIGHBOR 0

/**
 * Should not check the neighbor when filling
 */
#define fluid_solve_bounds_CHECK_NEIGHBOR 1


/**
 * Sets the boundary values for each chunk
 * @param numReadIn The number of chunks
 * @param chunkViewC The array of chunks
 * @param environment The environment storing the simulation queues
 */
LIBRARY_API void fluid_solve_bounds(int numReadIn, Chunk ** chunkViewC, Environment * environment);


/**
 * Sets the bounds of an array to a provided value
 * @param arrays The array to set
 * @param fillVal The value to fill with
 */
LIBRARY_API void fluid_solve_bounds_set_bounds(float * arrays, float fillVal);


#endif