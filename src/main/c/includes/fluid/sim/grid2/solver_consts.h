#ifndef FLUID_GRID2_SOLVER_CONSTS_H
#define FLUID_GRID2_SOLVER_CONSTS_H


/**
 * The number of times to relax most solvers
 */
#define FLUID_GRID2_LINEARSOLVERTIMES 10

/**
 * Convergence threshold for density diffusion
 */
#define FLUID_GRID2_DENSITY_DIFFUSE_THRESHOLD 0.001f

/**
 * The number of times to relax most solvers
 */
#define FLUID_GRID2_SOLVER_MULTIGRID_MAX_ITERATIONS 20

/**
 * Tolerance to target for multigrid precomputing in projection
 */
#define FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE 0.1f

/**
 * The number of times to relax most solvers
 */
#define FLUID_GRID2_SOLVER_CG_MAX_ITERATIONS 10

/**
 * Tolerance to target for conjugate gradient precomputing in projection
 */
#define FLUID_GRID2_SOLVER_CG_TOLERANCE 0.01f

/**
 * Width of a single grid cell
 */
#define FLUID_GRID2_H (1.0/(DIM-2))

/**
 * Timestep to simulate by
 */
#define FLUID_GRID2_SIM_STEP 0.01f

/**
 * Const to multiply the advection stages by to offset numeric instability
 */
#define FLUID_GRID2_CORRECTION_CONST 1.0005f

/**
 * Really small value used for something
 */
#define FLUID_GRID2_REALLY_SMALL_VALUE 0.00001f

/**
 * The tolerance for convergence of the projection operator
 */
#define FLUID_GRID2_PROJECTION_CONVERGENCE_TOLERANCE FLUID_GRID2_SOLVER_CG_TOLERANCE

/**
 * Diffusion constant
 */
#define FLUID_GRID2_DIFFUSION_CONSTANT 0.00001f

/**
 * Viscosity constant
 */
#define FLUID_GRID2_VISCOSITY_CONSTANT 0.00001f

#endif