#ifndef MATH_CONJUGATE_GRADIENT_H
#define MATH_CONJUGATE_GRADIENT_H

#include "math/ode/ode.h"



//
//
//   NAVIER STOKES SPECIFIC
//
//
/**
 * Iniitalizes the conjugate gradient solver with the phi values
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @return 1 if the system has already been solved, 0 otherwise
 */
int solver_conjugate_gradient_init(float * phi, float * phi0, float a, float c);

/**
 * Initializes the conjugate gradient solver
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 */
void solver_conjugate_gradient_init_navier_stokes_serial(float * phi, float * phi0, float a, float c);

/**
 * Iteratively solves an ODE matrix by 1 iteration of conjugate gradient method serially
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @return The residual
 */
float solver_conjugate_gradient_iterate_navier_stokes_serial(float * phi, float * phi0, float a, float c);

/**
 * Iteratively solves an ODE matrix by 1 iteration of conjugate gradient method
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @return The residual
 */
float solver_conjugate_gradient_iterate_parallel(float * phi, float * phi0, float a, float c);



//
//
//    GENERIC ODES
//
//


/**
 * Computes the stencil for the conjugate gradient solver
 */
typedef float (* ode_cg_search_direction_stencil)(float * phi, int x, int y, int z, OdeData * data);

/**
 * Initializes the conjugate gradient solver
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 */
void solver_conjugate_gradient_init_serial(float * phi, float * phi0);


/**
 * Iteratively solves an ODE matrix by 1 iteration of conjugate gradient method serially
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param stencil_func The stencil to compute the search direction
 * @param odeData The ode data
 * @return The residual
 */
float solver_conjugate_gradient_iterate_serial(float * phi, float * phi0, ode_cg_search_direction_stencil stencil_func, OdeData * odeData);

#endif