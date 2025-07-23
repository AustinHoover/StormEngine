
#ifndef FLUID_GRID2_DENSITY_H
#define FLUID_GRID2_DENSITY_H

#include <stdint.h>
#include "fluid/env/environment.h"



/**
 * Actually performs the advection
*/
void fluid_grid2_advect(Environment * environment, uint32_t chunk_mask, int b, float ** jrd, float ** jrd0, float * u, float * v, float * w, float dt);



/**
 * Adds density to the density array
 * @return The change in density within this chunk for this frame
*/
void fluid_grid2_addDensity(
    Environment * environment,
    float ** d,
    float ** d0,
    float dt
);




/*
 * A single iteration of the jacobi to solve density diffusion
 */
LIBRARY_API void fluid_grid2_solveDiffuseDensity(
    Environment * environment,
    float ** d,
    float ** d0,
    float dt
);





/**
 * Advects the density based on the vectors
*/
LIBRARY_API void fluid_grid2_advectDensity(Environment * environment, float ** d, float ** d0, float ** ur, float ** vr, float ** wr, float dt);



/**
 * Normalizes the density array with a given ratio
 */
void fluid_grid2_normalizeDensity(Environment * environment, float ** d, float ratio);


/**
 * Calculates the current sum of the chunk for normalization purposes
 */
double fluid_grid2_sum_for_normalization(Environment * environment, Chunk * chunk);



#endif