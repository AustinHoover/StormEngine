#ifndef MATH_ODE_DIFFUSION_ODE_H
#define MATH_ODE_DIFFUSION_ODE_H

#include "math/ode/ode.h"
#include "fluid/env/utilities.h"
#include "fluid/queue/chunk.h"
#include "fluid/sim/grid2/solver_consts.h"

/**
 * Data for computing the diffusion ode
 */
typedef struct {
    /**
     * Simulation timestep
     */
    float dt;
} OdeDiffuseData;


/**
 * Computes the residual of a given position in a diffusion ode
 */
float ode_diffusion_cg_stencil(float * phi, int x, int y, int z, OdeData * data);



#endif