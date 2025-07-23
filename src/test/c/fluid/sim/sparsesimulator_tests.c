#include <stdio.h>
#include <stdlib.h>

#include "fluid/queue/chunk.h"
#include "fluid/queue/sparse.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/utilities.h"
#include "fluid/queue/islandsolver.h"
#include "fluid/sim/sparse/sparsesimulator.h"
#include "../../util/test.h"
#include "../../util/chunk_test_utils.h"

#define TEST_DT 0.1
#define TEST_DIFF_CONST 0.0001











int test_sparse_array_add_source(){
    int rVal = 0;
    //allocate a sparse array
    FluidIslandSolver * islandSolver = fluid_island_solver_create();
    if(islandSolver == NULL){
        printf("Failed to allocate islandSolver!\n");
        return 1;
    }

    SparseChunkArray * sparseArray = islandSolver->sparseArray;

    //create chunks to add to the sparse array
    Chunk * chunk1 = chunk_create_pos(0,0,0);

    int testVal = 1;

    //add to island solver
    fluid_island_solver_add_chunk(islandSolver,chunk1);
    chunk_set_val(chunk1,testVal,ARR_ID_D0);

    //solve and simulate
    fluid_island_solver_solve_island(islandSolver);

    //add source test
    fluid_sparse_array_add_source(sparseArray->d,sparseArray->d0, TEST_DT);
    //indices are weird because this chunk will be at the center of the sparse array
    int voxelIndex = MAIN_ARRAY_DIM + MAIN_ARRAY_DIM + MAIN_ARRAY_DIM + (SPARSE_ARRAY_BORDER_SIZE / 2);
    int index = fluid_sparse_array_get_index(sparseArray,voxelIndex,voxelIndex,voxelIndex);
    printf("voxelIndex: %d \n",voxelIndex);
    rVal += assertEqualsFloat(sparseArray->d[index],testVal * TEST_DT,"add source did not properly add the source to the array! %f %f \n");
    


    //free the test chunks
    chunk_free(chunk1);
    //free a sparse array
    fluid_island_solver_free(islandSolver);

    return rVal;
}













int test_sparse_array_dens_step(){
    int rVal = 0;
    //allocate a sparse array
    FluidIslandSolver * islandSolver = fluid_island_solver_create();
    if(islandSolver == NULL){
        printf("Failed to allocate islandSolver!\n");
        return 1;
    }

    SparseChunkArray * sparseArray = islandSolver->sparseArray;

    //create chunks to add to the sparse array
    Chunk * chunk1 = chunk_create_pos(0,0,0);

    int testVal = 1;

    //add to island solver
    fluid_island_solver_add_chunk(islandSolver,chunk1);
    chunk_set_val(chunk1,testVal,ARR_ID_D0);

    //solve and simulate
    fluid_island_solver_solve_island(islandSolver);

    //add source test
    fluid_sparse_array_dens_step(sparseArray->d, sparseArray->d0, sparseArray->u, sparseArray->v, sparseArray->w, TEST_DIFF_CONST, TEST_DT);
    


    //free the test chunks
    chunk_free(chunk1);
    //free a sparse array
    fluid_island_solver_free(islandSolver);

    return rVal;
}

















int fluid_sim_sparsesimulator_tests(int argc, char **argv){
    int rVal = 0;
    
    rVal += test_sparse_array_add_source();
    rVal += test_sparse_array_dens_step();

    return rVal;
}
