#ifndef MATH_ODE_TUNED_NAVIER_STOKES_H
#define MATH_ODE_TUNED_NAVIER_STOKES_H

#include "public.h"
#include "fluid/queue/chunk.h"

/**
 * Calculates the residual for the approximation
 */
LIBRARY_API float solver_navier_stokes_get_residual(float * phi, float * phi0, int x, int y, int z, int GRIDDIM);

/**
 * Calculates the residual for the approximation
 */
LIBRARY_API void solver_navier_stokes_approximate(float * phi, float * phi0, int x, int y, int z, int GRIDDIM);


/**
 * Iterates the navier stokes solver
 * @return The cummulative normalized residual
 */
LIBRARY_API float solver_navier_stokes_iterate(float * phi, float * phi0, int GRIDDIM);


#endif