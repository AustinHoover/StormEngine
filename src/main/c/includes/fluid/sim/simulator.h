#ifndef FLUID_SIMULATOR_H
#define FLUID_SIMULATOR_H

#include "public.h"
#include "fluid/env/environment.h"



/**
 * Simulates the various chunk queues in the fluid environment
 * @param environment The environment storing the simulation queues
 */
LIBRARY_API void fluid_simulate(Environment * environment);




#endif