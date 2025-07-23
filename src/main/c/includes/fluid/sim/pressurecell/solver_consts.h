#ifndef FLUID_PRESSURECELL_CONSTS_H
#define FLUID_PRESSURECELL_CONSTS_H


/**
 * Timestep to simulate by
 */
#define FLUID_PRESSURECELL_SIM_STEP 0.1f

/**
 * Force of gravity in unit tests
 */
#define FLUID_PRESSURECELL_GRAVITY -100.0f

/**
 * Spacing of cells
 */
#define FLUID_PRESSURECELL_SPACING (1.0f)

/**
 * Multiplier applied to pressure calculations to encourage advection
 */
#define FLUID_PRESSURECELL_PRESSURE_MULTIPLIER 1.0f

/**
 * Maximum allowed velocity of the pressurecell simulator
 */
#define FLUID_PRESSURECELL_MAX_VELOCITY 1.0f

/**
 * Diffusion constant
 */
#define FLUID_PRESSURECELL_DIFFUSION_CONSTANT 0.3f

/**
 * Viscosity constant
 */
#define FLUID_PRESSURECELL_VISCOSITY_CONSTANT 0.3f

/**
 * Amount of the residual to add to the pressure field each frame
 */
#define FLUID_PRESSURECELL_RESIDUAL_MULTIPLIER 0.1f

/**
 * Constant applied to divergence to get new pressure
 */
#define FLUID_PRESSURECELL_DIV_PRESSURE_CONST 5.0f

/**
 * Cutoff after which density is clamped to zero while diffusing
 */
#define FLUID_PRESSURECELL_MIN_DENSITY_CLAMP_CUTOFF 0.0001f

/**
 * The number of times to relax most solvers
 */
#define FLUID_PRESSURECELL_SOLVER_MULTIGRID_MAX_ITERATIONS 5

/**
 * The tolerance to shoot for when approximating pressure
 */
#define FLUID_PRESSURECELL_PROJECTION_CONVERGENCE_TOLERANCE 0.01f

/**
 * The maximum pressure allowed
 */
#define FLUID_PRESSURECELL_MAX_PRESSURE 1000.0f

/**
 * The minimum pressure allowed
 */
#define FLUID_PRESSURECELL_MIN_PRESSURE -FLUID_PRESSURECELL_MAX_PRESSURE

/**
 * Percentage of presure to keep from last frame
 */
#define FLUID_PRESSURECELL_PRESSURE_BACKDOWN_FACTOR 0.0f

/**
 * Percentage of divergence to keep from last frame
 */
#define FLUID_PRESSURECELL_DIVERGENCE_BACKDOWN_FACTOR 1.0f

/**
 * Pressure added on recapturing fluid pushed into borders
 */
#define FLUID_PRESSURECELL_RECAPTURE_PRESSURE 0.0f

/**
 * Pressure of bounds
 */
#define FLUID_PRESSURECELL_BOUND_PRESSURE FLUID_PRESSURECELL_MAX_PRESSURE

/**
 * Maximum divergence allowed
 */
#define FLUID_PRESSURECELL_MAX_DIVERGENCE 3.0f





/**
 * Enables recapture of density when velocity pushes it outside of bounds
 */
#define FLUID_PRESSURECELL_ENABLE_RECAPTURE 0

/**
 * Enables clamping small density values to 0
 */
#define FLUID_PRESSURECELL_ENABLE_CLAMP_MIN_DENSITY 1

/**
 * Enables renormalizing the velocity field to a max value of 1 during projection
 */
#define FLUID_PRESSURECELL_ENABLE_VELOCITY_FIELD_NORMALIZAITON 0


#endif