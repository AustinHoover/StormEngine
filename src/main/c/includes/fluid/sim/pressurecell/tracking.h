#ifndef FLUID_PRESSURECELL_TRACKING_H
#define FLUID_PRESSURECELL_TRACKING_H

#include "public.h"
#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"


/**
 * Updates the tracking data for this chunk
 */
LIBRARY_API void pressurecell_update_tracking(Environment * environment, Chunk * chunk);

/**
 * Updates the inflow/outflow for this chunk
 */
LIBRARY_API void pressurecell_update_flows(Environment * environment, Chunk * chunk);


#endif