#ifndef SPARSESIMULATOR_H
#define SPARSESIMULATOR_H

//Must be included for public functions to be imported/exported on windows
#include "public.h"

#include "fluid/queue/chunk.h"

/**
 * Simulates a sparse array
 */
LIBRARY_API int fluid_sparse_array_simulate(SparseChunkArray * array, float dt);

/**
 * Adds values from a source array to a current frame array (eg more density to the main density array)
 * @param x The array to store into
 * @param s The source array to pull from
 * @param dt The delta time of the simulation
*/
void fluid_sparse_array_add_source(float * x, float * s, float dt);

/**
 * Diffuses an array
 * @param b The axis to diffuse along
 * @param x The array to store the diffuse values
 * @param x0 The array that contains the first order derivatives
 * @param diff The diffuse constant
 * @param dt The delta time of the simulation
 */
LIBRARY_API void fluid_sparse_array_diffuse(int b, float * x, float * x0, float diff, float dt);

/**
 * Advects an array
 */
LIBRARY_API void fluid_sparse_array_advect(int b, float * d, float * d0, float * u, float * v, float * w, float dt);

/**
 * Projects an array
 */
LIBRARY_API void fluid_sparse_array_project(float * u, float * v, float * w, float * p, float * div);

/**
 * Sets the bounds of the simulation
 * @param b The axis to set bounds along
 * @param target The array to set the bounds of
*/
LIBRARY_API void fluid_sparse_array_set_bnd(int b, float * target);

/**
 * Performs the density step
 * @param x The density array
 * @param x0 The delta-density array
 * @param u The x velocity array
 * @param v The y velocity array
 * @param w THe z velocity array
 * @param diff The diffuse constant
 * @param dt The delta time for the simulation
 */
LIBRARY_API void fluid_sparse_array_dens_step(float * x, float * x0, float * u, float * v, float * w, float diff, float dt);

/**
 * Performs the velocity step
 */
LIBRARY_API void fluid_sparse_array_vel_step(float * u, float * v, float * w, float * u0, float * v0, float * w0, float visc, float dt);

/**
 * Solves a linear system of equations in a vectorized manner
 * @param b The axis to set the bounds along
 * @param x The array that will contain the solved equations
 * @param x0 The array containing the first order derivatives
*/
LIBRARY_API void fluid_sparse_array_lin_solve_diffuse(int b, float * x, float * x0);

/**
 * Solves a linear system of equations in a vectorized manner
 * @param b The axis to set the bounds along
 * @param x The array that will contain the solved equations
 * @param x0 The array containing the first order derivatives
*/
LIBRARY_API void fluid_sparse_array_lin_solve_project(int b, float * x, float * x0);

#endif