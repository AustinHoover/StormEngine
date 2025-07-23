

//Must be included for public functions to be imported/exported on windows
#include "public.h"

#ifndef ISLANDSOLVER_H
#define ISLANDSOLVER_H

#include "fluid/queue/chunk.h"
#include "fluid/queue/sparse.h"

/**
 * A set of sparse matricies for simulating fluids
 */
typedef struct {

    /**
     * The chunks that still need to be solved
     */
    Chunk ** remaining;

    /**
     * The chunks that have already been solved for
     */
    Chunk ** solved;

    /**
     * The sparse array
     */
    SparseChunkArray * sparseArray;

    /**
     * The number of chunks in the currently solved island
     */
    int currentChunks;

} FluidIslandSolver;

/**
 * Creates a fluid island solver
 */
LIBRARY_API FluidIslandSolver * fluid_island_solver_create();

/**
 * Frees a fluid island solver
 */
LIBRARY_API void fluid_island_solver_free(FluidIslandSolver * solver);

/**
 * Adds a chunk to the fluid island solver
 */
LIBRARY_API void fluid_island_solver_add_chunk(FluidIslandSolver * solver, Chunk * chunk);

/**
 * Adds a chunk to the fluid island solver
 */
LIBRARY_API void fluid_island_solver_remove_chunk(FluidIslandSolver * solver, Chunk * chunk);

/**
 * Solves for the next available island
 */
LIBRARY_API void fluid_island_solver_solve_island(FluidIslandSolver * solver);

/**
 * Gets the number of chunks in the current island
 */
LIBRARY_API int fluid_island_solver_get_chunk_count(FluidIslandSolver * solver);

/**
 * Gets the sparse array in the solver
 */
LIBRARY_API SparseChunkArray * fluid_island_solver_get_sparse_array(FluidIslandSolver * solver);

/**
 * Gets the number of chunks that still need to be solved for
 */
LIBRARY_API int fluid_island_solver_get_remaining(FluidIslandSolver * solver);


#endif