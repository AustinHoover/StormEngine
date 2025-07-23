#include <stdio.h>
#include <stdlib.h>

#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/utilities.h"
#include "fluid/queue/islandsolver.h"
#include "../../util/test.h"
#include "../../util/chunk_test_utils.h"



/**
 * Sets a chunk's value
 * @param chunk The chunk
 * @param i The value to set the array to
 * @param arr THe array to set
 */
void chunk_set_val(Chunk * chunk, int i, int arr){
    for(int x = 0; x < DIM; x++){
        for(int y = 0; y < DIM; y++){
            for(int z = 0; z < DIM; z++){
                switch(arr){
                    case ARR_ID_D: {
                        chunk->d[CENTER_LOC][x * DIM * DIM + y * DIM + z] = i;
                    } break;
                    case ARR_ID_D0: {
                        chunk->d0[CENTER_LOC][x * DIM * DIM + y * DIM + z] = i;
                    } break;
                    case ARR_ID_U: {
                        chunk->u[CENTER_LOC][x * DIM * DIM + y * DIM + z] = i;
                    } break;
                    case ARR_ID_V: {
                        chunk->v[CENTER_LOC][x * DIM * DIM + y * DIM + z] = i;
                    } break;
                    case ARR_ID_W: {
                        chunk->w[CENTER_LOC][x * DIM * DIM + y * DIM + z] = i;
                    } break;
                    case ARR_ID_U0: {
                        chunk->u0[CENTER_LOC][x * DIM * DIM + y * DIM + z] = i;
                    } break;
                    case ARR_ID_V0: {
                        chunk->v0[CENTER_LOC][x * DIM * DIM + y * DIM + z] = i;
                    } break;
                    case ARR_ID_W0: {
                        chunk->w0[CENTER_LOC][x * DIM * DIM + y * DIM + z] = i;
                    } break;
                }
            }
        }
    }
}


int fluid_queue_islandsolver_tests(int argc, char **argv){
    int rVal = 0;
    //allocate a sparse array
    FluidIslandSolver * islandSolver = fluid_island_solver_create();
    if(islandSolver == NULL){
        printf("Failed to allocate islandSolver!\n");
        return 1;
    }

    //create chunks to add to the sparse array
    Chunk * chunk1 = chunk_create_pos(0,0,0);
    Chunk * chunk2 = chunk_create_pos(1,0,0);
    Chunk * chunk3 = chunk_create_pos(7,0,0);

    //test adding chunks
    fluid_island_solver_add_chunk(islandSolver,chunk1);
    fluid_island_solver_add_chunk(islandSolver,chunk2);
    rVal += assertEqualsPtr((void *)islandSolver->remaining[0],(void *)chunk1,"Failed to insert chunk1 \n");
    rVal += assertEqualsPtr((void *)islandSolver->remaining[1],(void *)chunk2,"Failed to insert chunk1 \n");
    rVal += assertNotEqualsPtr((void *)islandSolver->remaining[1],(void *)chunk1,"Failed to insert chunk1 \n");
    rVal += assertNotEqualsPtr((void *)islandSolver->remaining[0],(void *)chunk2,"Failed to insert chunk1 \n");

    //test removing
    fluid_island_solver_remove_chunk(islandSolver,chunk1);
    rVal += assertEquals(fluid_island_solver_get_remaining(islandSolver),1,"remaining array should NOT be empty -- %d %d \n");
    fluid_island_solver_remove_chunk(islandSolver,chunk2);
    rVal += assertEquals(fluid_island_solver_get_remaining(islandSolver),0,"remaining array should be empty -- %d %d \n");

    //solve an island
    fluid_island_solver_add_chunk(islandSolver,chunk1);
    fluid_island_solver_add_chunk(islandSolver,chunk2);
    fluid_island_solver_add_chunk(islandSolver,chunk3);
    //this should solve for the lone chunk 3 island
    fluid_island_solver_solve_island(islandSolver);
    rVal += assertEquals(fluid_sparse_array_get_chunk_count(islandSolver->sparseArray),1,"sparse array should contain one element %d %d \n");
    //this should solve for the conjoined chunk1-chunk2 island
    fluid_island_solver_solve_island(islandSolver);
    rVal += assertEquals(fluid_sparse_array_get_chunk_count(islandSolver->sparseArray),2,"sparse array should contain two elements %d %d \n");
    rVal += assertEquals(fluid_island_solver_get_remaining(islandSolver),0,"there should be no remaining chunks %d %d \n");

    //should be able to grab the sparse array from here still
    rVal += assertEqualsPtr(fluid_island_solver_get_sparse_array(islandSolver),islandSolver->sparseArray,"should be able to fetch the sparse array from the solver \n");



    //free the test chunks
    chunk_free(chunk1);
    chunk_free(chunk2);
    chunk_free(chunk3);
    //free a sparse array
    fluid_island_solver_free(islandSolver);

    return rVal;
}
