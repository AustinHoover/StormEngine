#ifndef FLUID_PRESSURECELL_NORMALIZATION_H
#define FLUID_PRESSURECELL_NORMALIZATION_H

#include "public.h"
#include "fluid/env/environment.h"
#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"

/**
 * Calculates the expected density and pressure
 */
LIBRARY_API void fluid_pressurecell_calculate_expected_intake(Environment * env, Chunk * chunk);

/**
 * Normalizes the chunk
 */
LIBRARY_API void fluid_pressurecell_normalize_chunk(Environment * env, Chunk * chunk);

/**
 * Recaptures density that has flowed into boundaries
 */
LIBRARY_API void fluid_pressurecell_recapture_density(Environment * env, Chunk * chunk);


#endif