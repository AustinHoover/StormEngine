
#ifndef FLUID_GRID2_UTILITIES_H
#define FLUID_GRID2_UTILITIES_H

#include <stdint.h>

#include "fluid/env/environment.h"


/**
 * Used for signaling the bounds setting method to not use adjacent cells when evaluating borders
 */
#define FLUID_GRID2_BOUND_NO_DIR 0

/**
 * Used for signaling the bounds setting method to use adjacent cells when evaluating x axis borders
 */
#define FLUID_GRID2_DIRECTION_U 1

/**
 * Used for signaling the bounds setting method to use adjacent cells when evaluating y axis borders
 */
#define FLUID_GRID2_DIRECTION_V 2

/**
 * Used for signaling the bounds setting method to use adjacent cells when evaluating z axis borders
 */
#define FLUID_GRID2_DIRECTION_W 3


/**
 * Setting the bounds when relaxing density diffusion
 */
#define BOUND_SET_DENSITY_PHI 4

/**
 * Setting the hard bounds of the world
 */
#define BOUND_SET_DENSITY 5

/**
 * Setting the bounds of phi when diffusing the u vector
 */
#define BOUND_SET_VECTOR_DIFFUSE_PHI_U 6

/**
 * Setting the bounds of phi when diffusing the v vector
 */
#define BOUND_SET_VECTOR_DIFFUSE_PHI_V 7

/**
 * Setting the bounds of phi when diffusing the w vector
 */
#define BOUND_SET_VECTOR_DIFFUSE_PHI_W 8

/**
 * Setting the bounds of phi when projecting
 */
#define BOUND_SET_PROJECTION_PHI 9

/**
 * Setting the bounds of phi0 projecting
 */
#define BOUND_SET_PROJECTION_PHI_0 10

/**
 * Setting the bounds of the x-vector field
 */
#define BOUND_SET_VECTOR_U 11

/**
 * Setting the bounds of the y-vector field
 */
#define BOUND_SET_VECTOR_V 12

/**
 * Setting the bounds of the z-vector field
 */
#define BOUND_SET_VECTOR_W 13


/**
 * Adds from a source array to a destination array
*/
void fluid_grid2_add_source(float * x, float * s, float dt);



/**
 * Sets the bounds of this cube to those of its neighbor
*/
LIBRARY_API void fluid_grid2_set_bounds(
    Environment * environment,
    int vector_dir,
    float * target
);



/**
 * Sums the density of the chunk
 */
double fluid_grid2_calculateSum(float ** d);



/**
 * Flips two array matricies
 */
LIBRARY_API void fluid_grid2_flip_arrays(float ** array1, float ** array2);






#endif