#ifndef FLUID_PRESSURECELL_BOUNDS_H
#define FLUID_PRESSURECELL_BOUNDS_H

#include "public.h"
#include "fluid/env/environment.h"
#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"

/**
 * Used for signaling the bounds setting method to not use adjacent cells when evaluating borders
 */
#define FLUID_PRESSURECELL_BOUND_NO_DIR 0

/**
 * Used for signaling the bounds setting method to use adjacent cells when evaluating x axis borders
 */
#define FLUID_PRESSURECELL_DIRECTION_U 1

/**
 * Used for signaling the bounds setting method to use adjacent cells when evaluating y axis borders
 */
#define FLUID_PRESSURECELL_DIRECTION_V 2

/**
 * Used for signaling the bounds setting method to use adjacent cells when evaluating z axis borders
 */
#define FLUID_PRESSURECELL_DIRECTION_W 3

/**
 * Updates the bounds of the chunk based on its neighbors
*/
LIBRARY_API void fluid_pressurecell_update_bounds(Environment * environment, Chunk * chunk);

/**
 * Updates the interest tree for this chunk
 */
LIBRARY_API void pressurecell_update_interest(Environment * environment, Chunk * chunk);

/**
 * Enforces velocity not running into bounds
*/
LIBRARY_API void pressurecell_enforce_bounds(Environment * environment, Chunk * chunk);

#endif