
#ifndef MATH_ODE_UTILS_H
#define MATH_ODE_UTILS_H


/**
 * Gets the index into the array
 */
static inline int ode_index(int x, int y, int z, int N){
    return (x + (N * y) +  (N * N * z));
}

/**
 * Computes the stencil of a given source array at a given position
 */
__m256 ode_poisson_stencil_parallel(float * source, int x, int y, int z);

#endif