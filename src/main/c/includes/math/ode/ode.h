#ifndef MATH_ODE_H
#define MATH_ODE_H


/**
 * Data for computing the ode (ie could hold timestep for instance)
 */
typedef void * OdeData;

/**
 * Computes the residual of a given position in an ode
 */
typedef float (* ode_approximate_stencil)(float * phi, float * phi0, int x, int y, int z, OdeData * data);

/**
 * Computes the residual of a given position in an ode
 */
typedef float (* ode_residual_stencil)(float * phi, float * phi0, int x, int y, int z, OdeData * data);


#endif