
#ifndef FLUID_GRID2_VELOCITY
#define FLUID_GRID2_vELOCITY

#include <stdint.h>
#include "fluid/env/environment.h"
#include "fluid/queue/chunk.h"




/**
 * Adds the sources to the destinations
 */
void fluid_grid2_addSourceToVectors(
  Environment * environment,
  float ** jru,
  float ** jrv,
  float ** jrw,
  float ** jru0,
  float ** jrv0,
  float ** jrw0,
  float dt
);




/**
 * Sets up a projection system of equations
 * It stores the first derivative of the field in pr, and zeroes out divr.
 * This allows you to calculate the second derivative into divr using the derivative stored in pr.
 * @param ur The x velocity grid
 * @param vr The y velocity grid
 * @param wr The z velocity grid
 * @param pr The grid that will contain the first derivative
 * @param divr The grid that will be zeroed out in preparation of the solver
 * @param dt The timestep for the simulation
 */
LIBRARY_API void fluid_grid2_setupProjection(
  Environment * environment,
  Chunk * chunk,
  float ** ur,
  float ** vr,
  float ** wr,
  float ** pr,
  float ** divr,
  float dt
);



/**
 * Solves a projection system of equations.
 * This performs a single iteration across a the p grid to approximate the gradient field.
 * @param jru0 The gradient field
 * @param jrv0 The first derivative field
 */
LIBRARY_API void fluid_grid2_solveProjection(
  Environment * environment,
  Chunk * chunk,
  float ** jru0,
  float ** jrv0,
  float dt
);


/**
 * Finalizes a projection.
 * This subtracts the difference delta along the approximated gradient field.
 * Thus we are left with an approximately mass-conserved field. 
 */
LIBRARY_API void fluid_grid2_finalizeProjection(
  Environment * environment,
  float ** jru,
  float ** jrv,
  float ** jrw,
  float ** jru0,
  float ** jrv0,
  float dt
);



/*
 * Advects u, v, and w
 */
LIBRARY_API void fluid_grid2_advectVectors(
  Environment * environment,
  float ** jru,
  float ** jrv,
  float ** jrw,
  float ** jru0,
  float ** jrv0,
  float ** jrw0,
  float dt
);

/**
 * Actually performs the advection
*/
void fluid_grid2_advect_velocity(Environment * environment, int b, float ** jrd, float ** jrd0, float * u, float * v, float * w, float dt);


/*
 * Solves vector diffusion along all axis
 */
LIBRARY_API void fluid_grid2_solveVectorDiffuse (
  Environment * environment,
  float ** jru,
  float ** jrv,
  float ** jrw,
  float ** jru0,
  float ** jrv0,
  float ** jrw0,
  float dt
);






#endif