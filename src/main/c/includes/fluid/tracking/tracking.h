#ifndef FLUID_TRACKING_H
#define FLUID_TRACKING_H

#include "public.h"
#include "fluid/env/environment.h"


/**
 * Resets the tracking state for this frame
 */
LIBRARY_API void fluid_tracking_reset(Environment * environment);



#endif