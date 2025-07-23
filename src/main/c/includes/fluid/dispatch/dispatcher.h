#ifndef FLUID_DISPATCH_H
#define FLUID_DISPATCH_H

#include "public.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"


/**
 * Do not override dispatcher
 */
#define FLUID_DISPATCHER_OVERRIDE_NONE 0

/**
 * Only dispatch to cellular
 */
#define FLUID_DISPATCHER_OVERRIDE_CELLULAR 1

/**
 * Only dispatch to grid2
 */
#define FLUID_DISPATCHER_OVERRIDE_GRID2 2

/**
 * Only dispatch to pressurecell
 */
#define FLUID_DISPATCHER_OVERRIDE_PRESSURECELL 3


/**
 * Dispatches chunks to different simulation queues based on the chunk's properties
 * @param numReadIn The number of chunks
 * @param chunkViewC The array of chunks
 * @param environment The environment storing the simulation queues
 * @param override Overrides the queueing system to force all chunks into a given queue (ie for testing)
 */
LIBRARY_API void fluid_dispatch(int numReadIn, Chunk ** chunkViewC, Environment * environment, int override);




#endif