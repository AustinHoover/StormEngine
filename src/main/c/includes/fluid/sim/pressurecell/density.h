#ifndef FLUID_PRESSURECELL_DENSITY_H
#define FLUID_PRESSURECELL_DENSITY_H

#include "public.h"
#include "fluid/env/environment.h"
#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"


/**
 * Adds density from the delta buffer to this chunk
*/
LIBRARY_API void fluid_pressurecell_add_density(Environment * environment, Chunk * chunk);

/**
 * Diffuses the density in this chunk
*/
LIBRARY_API void fluid_pressurecell_diffuse_density(Environment * environment, Chunk * chunk);

/**
 * Advects the density of this chunk
*/
LIBRARY_API void fluid_pressurecell_advect_density(Environment * environment, Chunk * chunk);


#endif