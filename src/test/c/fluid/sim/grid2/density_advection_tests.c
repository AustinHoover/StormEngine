#include <math.h>

#include "stb/stb_ds.h"

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/grid2/density.h"
#include "fluid/sim/grid2/solver_consts.h"
#include "fluid/sim/grid2/utilities.h"
#include "math/ode/multigrid.h"
#include "../../../util/chunk_test_utils.h"
#include "../../../util/test.h"


/**
 * Amount to place in density advection tests
 */
#define FLUID_GRID2_DENSITY_ADVECTION_TESTS_PLACEMENT_VAL 1.0f

/**
 * Center of the advection cell
 */
#define FLUID_GRID2_DENSITY_ADVECTION_CELL_CENTER 24

/**
 * Testing density diffusion
 */
int fluid_sim_grid2_density_advection_test1(){
    printf("fluid_sim_grid2_density_advection_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(2,2,2)] = MAX_FLUID_VALUE;
    float beforeSum = chunk_queue_sum_density(queue);
    chunk_fill_real(queue[0]->u[CENTER_LOC],FLUID_GRID2_DENSITY_ADVECTION_TESTS_PLACEMENT_VAL);

    //actually advect
    fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
    fluid_grid2_advectDensity(env,currentChunk->d,currentChunk->d0,currentChunk->u,currentChunk->v,currentChunk->w,FLUID_GRID2_SIM_STEP);

    //sum the result
    float afterSum = chunk_queue_sum_density(queue);

    if(fabs(beforeSum - afterSum) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSum,afterSum,"Density advection step changed density sum!  %f %f  \n");
    }

    return rVal;
}

/**
 * Testing density diffusion
 */
int fluid_sim_grid2_density_advection_test2(){
    printf("fluid_sim_grid2_density_advection_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(2,2,2)] = MAX_FLUID_VALUE;
    float beforeSum = chunk_queue_sum_density(queue);
    advection_setup_convection_cell(queue, FLUID_GRID2_DENSITY_ADVECTION_CELL_CENTER);

    //actually simulate
    int frameCount = 50;
    for(int frame = 0; frame < frameCount; frame++){
        int chunkCount = arrlen(queue);
        for(int chunkIndex = 0; chunkIndex < 1; chunkIndex++){
            currentChunk = queue[chunkIndex];
            fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
            fluid_grid2_advectDensity(env,currentChunk->d,currentChunk->d0,currentChunk->u,currentChunk->v,currentChunk->w,FLUID_GRID2_SIM_STEP);
            fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
        }
    }

    //test the result
    float afterSum = chunk_queue_sum_density(queue);
    if(fabs(beforeSum - afterSum) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSum,afterSum,"Density advection step changed density sum!  %f %f  \n");
    }

    return rVal;
}

/**
 * Testing density diffusion
 */
int fluid_sim_grid2_density_advection_test3(){
    printf("fluid_sim_grid2_density_advection_test3\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);




    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(2,2,2)] = MAX_FLUID_VALUE;
    float beforeSum = chunk_queue_sum_density(queue);
    advection_setup_convection_cell(queue, FLUID_GRID2_DENSITY_ADVECTION_CELL_CENTER);

    //actually simulate
    int frameCount = 400;
    for(int frame = 0; frame < frameCount; frame++){
        int chunkCount = arrlen(queue);
        for(int chunkIndex = 0; chunkIndex < 1; chunkIndex++){
            currentChunk = queue[chunkIndex];
            fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
            fluid_grid2_advectDensity(env,currentChunk->d,currentChunk->d0,currentChunk->u,currentChunk->v,currentChunk->w,FLUID_GRID2_SIM_STEP);
            fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
        }
    }

    //test the result
    float afterSum = chunk_queue_sum_density(queue);
    if(fabs(beforeSum - afterSum) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSum,afterSum,"Density advection step changed density sum!  %f %f  \n");
    }

    return rVal;
}

/**
 * Testing density diffusion
 */
int fluid_sim_grid2_density_advection_test4(){
    printf("fluid_sim_grid2_density_advection_test4\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);




    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(2,2,2)] = MAX_FLUID_VALUE;
    currentChunk->d[CENTER_LOC][IX(2,4,2)] = MAX_FLUID_VALUE;
    currentChunk->d[CENTER_LOC][IX(2,2,7)] = MAX_FLUID_VALUE;
    currentChunk->d[CENTER_LOC][IX(12,2,2)] = MAX_FLUID_VALUE;
    float beforeSum = chunk_queue_sum_density(queue);
    advection_setup_convection_cell(queue, FLUID_GRID2_DENSITY_ADVECTION_CELL_CENTER);

    //actually simulate
    int frameCount = 400;
    for(int frame = 0; frame < frameCount; frame++){
        int chunkCount = arrlen(queue);
        for(int chunkIndex = 0; chunkIndex < 1; chunkIndex++){
            currentChunk = queue[chunkIndex];
            fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
            fluid_grid2_advectDensity(env,currentChunk->d,currentChunk->d0,currentChunk->u,currentChunk->v,currentChunk->w,FLUID_GRID2_SIM_STEP);
            fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
        }
    }

    //test the result
    float afterSum = chunk_queue_sum_density(queue);
    if(fabs(beforeSum - afterSum) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSum,afterSum,"Density advection step changed density sum!  %f %f  \n");
    }

    return rVal;
}

/**
 * Testing density diffusion
 */
int fluid_sim_grid2_density_advection_test5(){
    printf("fluid_sim_grid2_density_advection_test4\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);




    //setup chunk values
    Chunk * currentChunk = queue[3 * 3 + 3];
    chunk_fill_real(currentChunk->d[CENTER_LOC],MAX_FLUID_VALUE);
    float beforeSum = chunk_queue_sum_density(queue);
    advection_setup_convection_cell(queue, FLUID_GRID2_DENSITY_ADVECTION_CELL_CENTER);

    //actually simulate
    int frameCount = 400;
    for(int frame = 0; frame < frameCount; frame++){
        int chunkCount = arrlen(queue);
        for(int chunkIndex = 0; chunkIndex < 1; chunkIndex++){
            currentChunk = queue[chunkIndex];
            fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
            fluid_grid2_advectDensity(env,currentChunk->d,currentChunk->d0,currentChunk->u,currentChunk->v,currentChunk->w,FLUID_GRID2_SIM_STEP);
            fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
        }
    }

    //test the result
    float afterSum = chunk_queue_sum_density(queue);
    if(fabs(beforeSum - afterSum) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSum,afterSum,"Density advection step changed density sum!  %f %f  \n");
    }

    return rVal;
}




/**
 * Testing density diffusion
 */
int fluid_sim_grid2_density_advection_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_grid2_density_advection_test1();
    rVal += fluid_sim_grid2_density_advection_test2();
    rVal += fluid_sim_grid2_density_advection_test3();
    rVal += fluid_sim_grid2_density_advection_test4();
    rVal += fluid_sim_grid2_density_advection_test5();

    return rVal;
}