
#ifndef MATH_SOLVER_MULTIGRID_H
#define MATH_SOLVER_MULTIGRID_H

#include "immintrin.h"

#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"

/**
 * Relaxes an ODE matrix by 1 iteration of multigrid method
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @return The residual
 */
float solver_multigrid_iterate_serial(float * phi, float * phi0, float a, float c);

/**
 * Relaxes an ODE matrix by 1 iteration of multigrid method
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @return The residual
 */
float solver_multigrid_iterate_parallel(float * phi, float * phi0, float a, float c);

/**
 * Relaxes an ODE matrix by 1 iteration of multigrid method
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @param GRIDDIM The dimension of the phi grid
 * @return The residual
 */
void solver_multigrid_iterate_serial_recursive(float * phi, float * phi0, float a, float c, int GRIDDIM);

/**
 * Verifies that all grids are allocated
 */
void solver_multigrid_initialization_check();

/**
 * Calculates the residual of the grid
 * @return Returns the residual norm of the grid
 */
float solver_multigrid_calculate_residual_norm_serial(float * phi, float * phi0, float a, float c, int GRIDDIM);

/**
 * Serially restricts the current residual into the lower phi grid
 */
void solver_multigrid_restrict_serial(float * currResidual, int GRIDDIM, float * lowerPhi, float * lowerPhi0, int LOWERDIM);

/**
 * Prolongates a lower grid into a higher grid
 */
void solver_multigrid_prolongate_serial(float * phi, int GRIDDIM, float * lowerPhi, int LOWERDIM);

/**
 * Gets the phi to use for the current level of the multigrid solver
 */
float * solver_multigrid_get_current_phi(int dim);

/**
 * Gets the phi0 to use for the current level of the multigrid solver
 */
float * solver_multigrid_get_current_phi0(int dim);

/**
 * Gets the residual to use for the current level of the multigrid solver
 */
float * solver_multigrid_get_current_residual(int dim);

/**
 * Calculates the residual of the grid
 */
void solver_multigrid_store_residual_serial(float * phi, float * phi0, float * residualGrid, float a, float c, int GRIDDIM);



#endif