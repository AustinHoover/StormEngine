#ifndef FLUID_PRESSURECELL_PRESSURE_H
#define FLUID_PRESSURECELL_PRESSURE_H

#include "public.h"
#include "fluid/env/environment.h"
#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"


/**
 * Approximates the pressure for this chunk
*/
LIBRARY_API void pressurecell_approximate_pressure(Environment * environment, Chunk * chunk);

/**
 * Approximates the divergence for this chunk
*/
LIBRARY_API void pressurecell_approximate_divergence(Environment * environment, Chunk * chunk);


#endif