#include <stdio.h>
#include <stdlib.h>

#include "stb/stb_ds.h"

#include "fluid/queue/islandsolver.h"
#include "fluid/queue/chunk.h"







/**
 * Creates a fluid island solver
 */
LIBRARY_API FluidIslandSolver * fluid_island_solver_create(){
    FluidIslandSolver * rVal = (FluidIslandSolver *)calloc(1,sizeof(FluidIslandSolver));

    rVal->sparseArray = fluid_sparse_array_create();
    rVal->remaining = NULL;
    rVal->solved = NULL;
    rVal->currentChunks = 0;

    return rVal;
}

/**
 * Frees a fluid island solver
 */
LIBRARY_API void fluid_island_solver_free(FluidIslandSolver * solver){
    fluid_sparse_array_free(solver->sparseArray);
    free(solver);
}

/**
 * Adds a chunk to the fluid island solver
 */
LIBRARY_API void fluid_island_solver_add_chunk(FluidIslandSolver * solver, Chunk * chunk){
    stbds_arrput(solver->remaining,chunk);
}

/**
 * Adds a chunk to the fluid island solver
 */
LIBRARY_API void fluid_island_solver_remove_chunk(FluidIslandSolver * solver, Chunk * chunk){
    for(int i = 0; i < stbds_arrlen(solver->remaining); i++){
        if(solver->remaining[i] == chunk){
            stbds_arrdel(solver->remaining,i);
            i--;
        }
    }
    for(int i = 0; i < stbds_arrlen(solver->solved); i++){
        if(solver->solved[i] == chunk){
            stbds_arrdel(solver->solved,i);
            i--;
        }
    }
}

/**
 * Solves for the next available island
 */
LIBRARY_API void fluid_island_solver_solve_island(FluidIslandSolver * solver){
    if(fluid_island_solver_get_remaining(solver) < 1){
        printf("Tried to solve a solver with 0 remaining! \n");
        fflush(stdout);
        return;
    }

    //clear the existing values
    fluid_sparse_array_clean(solver->sparseArray);

    //fill in sparse array based on the first found chunk
    Chunk * target = stbds_arrpop(solver->remaining);
    fluid_sparse_array_add_chunk(
        solver->sparseArray,
        target,
        SPARSE_ARRAY_CHUNK_RADIUS,
        SPARSE_ARRAY_CHUNK_RADIUS,
        SPARSE_ARRAY_CHUNK_RADIUS
    );

    //add neighbors of the target
    for(int i = 0; i < stbds_arrlen(solver->remaining); i++){
        Chunk * current = solver->remaining[i];
        // printf("%d %d %d %d %d\n",
        //     abs(current->x - target->x) <= SPARSE_ARRAY_CHUNK_RADIUS,
        //     abs(current->y - target->y) <= SPARSE_ARRAY_CHUNK_RADIUS,
        //     abs(current->z - target->z) <= SPARSE_ARRAY_CHUNK_RADIUS,
        //     target->x,
        //     current->x
        // );
        if(
            abs(current->x - target->x) <= SPARSE_ARRAY_CHUNK_RADIUS &&
            abs(current->y - target->y) <= SPARSE_ARRAY_CHUNK_RADIUS &&
            abs(current->z - target->z) <= SPARSE_ARRAY_CHUNK_RADIUS
        ){  
            //remove this chunk from the remaining array
            stbds_arrdel(solver->remaining,i);
            i--;
            //add to sparse array
            int xPos = SPARSE_ARRAY_CHUNK_RADIUS + (current->x - target->x);
            int yPos = SPARSE_ARRAY_CHUNK_RADIUS + (current->y - target->y);
            int zPos = SPARSE_ARRAY_CHUNK_RADIUS + (current->z - target->z);
            if(
                xPos < 0 || xPos >= SPARSE_ARRAY_CHUNK_DIM ||
                yPos < 0 || yPos >= SPARSE_ARRAY_CHUNK_DIM ||
                zPos < 0 || zPos >= SPARSE_ARRAY_CHUNK_DIM
            ){
                printf("Invalid insertion position! %d %d %d \n",xPos,yPos,zPos);
                fflush(stdout);
            }
            fluid_sparse_array_add_chunk(solver->sparseArray,current,xPos,yPos,zPos);
        }
    }
}

/**
 * Gets the number of chunks in the current island
 */
LIBRARY_API int fluid_island_solver_get_chunk_count(FluidIslandSolver * solver){
    return fluid_sparse_array_get_chunk_count(solver->sparseArray);
}

/**
 * Gets the sparse array in the solver
 */
LIBRARY_API SparseChunkArray * fluid_island_solver_get_sparse_array(FluidIslandSolver * solver){
    return solver->sparseArray;
}

/**
 * Gets the number of chunks that still need to be solved for
 */
LIBRARY_API int fluid_island_solver_get_remaining(FluidIslandSolver * solver){
    return stbds_arrlen(solver->remaining);
}


