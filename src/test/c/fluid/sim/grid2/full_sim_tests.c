#include <math.h>

#include "stb/stb_ds.h"

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/grid2/density.h"
#include "fluid/sim/grid2/grid2.h"
#include "fluid/sim/grid2/solver_consts.h"
#include "fluid/sim/grid2/utilities.h"
#include "fluid/sim/grid2/velocity.h"
#include "math/ode/multigrid.h"
#include "../../../util/chunk_test_utils.h"
#include "../../../util/test.h"

/**
 * Center of the advection cell
 */
#define FLUID_GRID2_PROJECTION_CELL_CENTER 24

/**
 * Error margin for tests
 */
#define FLUID_GRID2_PROJECTION_ERROR_MARGIN 0.00001f

/**
 * Number of chunks
 */
#define CHUNK_DIM 4

/**
 * Testing full sim routine
 */
int fluid_sim_grid2_full_sim_test1(){
    printf("fluid_sim_grid2_full_sim_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    float beforeSum = chunk_queue_sum_density(queue);

    //actually simulate
    int frameCount = 1;
    for(int frame = 0; frame < frameCount; frame++){
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);
    }

    //test the result
    float afterSum = chunk_queue_sum_density(queue);
    if(fabs(beforeSum - afterSum) > FLUID_GRID2_PROJECTION_ERROR_MARGIN){
        rVal += assertEqualsFloat(beforeSum,afterSum,"Simulation changed density!  %f %f  \n");
    }

    return rVal;
}

/**
 * Testing full sim routine
 */
int fluid_sim_grid2_full_sim_test2(){
    printf("fluid_sim_grid2_full_sim_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    float beforeSum = chunk_queue_sum_density(queue);

    //actually simulate
    int frameCount = 1;
    for(int frame = 0; frame < frameCount; frame++){
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);
        printf("Existing sum: %lf\n", env->state.existingDensity);
        printf("Added density: %lf\n", env->state.newDensity);
        printf("Adjustment Ratio: %f\n", env->state.normalizationRatio);
        float afterSum = chunk_queue_sum_density(queue);
        printf("AFter transform sum: %f\n",afterSum);
        printf("\n");
    }

    //test the result
    float afterSum = chunk_queue_sum_density(queue);
    if(fabs(beforeSum - afterSum) > FLUID_GRID2_PROJECTION_ERROR_MARGIN){
        rVal += assertEqualsFloat(beforeSum,afterSum,"Simulation changed density!  %f %f  \n");
    }

    return rVal;
}

/**
 * Testing full sim routine
 */
int fluid_sim_grid2_full_sim_test3(){
    printf("fluid_sim_grid2_full_sim_test3\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    chunk_fill_real(queue[CENTER_LOC]->d[CENTER_LOC],MAX_FLUID_VALUE);
    chunk_fill_real(queue[CENTER_LOC]->u[CENTER_LOC],MAX_FLUID_VALUE);
    queue[CENTER_LOC]->d[DIM*DIM*3+DIM*3+3] = 0;
    float beforeSum = chunk_queue_sum_density(queue);

    //actually simulate
    int frameCount = 1;
    for(int frame = 0; frame < frameCount; frame++){
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);
        // printf("Existing sum: %lf\n", env->state.existingDensity);
        // printf("Added density: %lf\n", env->state.newDensity);
        // printf("Adjustment Ratio: %f\n", env->state.normalizationRatio);
        // float afterSum = chunk_queue_sum_density(queue);
        // printf("AFter transform sum: %f\n",afterSum);
        // printf("\n");
    }

    //test the result
    float afterSum = chunk_queue_sum_density(queue);
    if(fabs(beforeSum - afterSum) > FLUID_GRID2_PROJECTION_ERROR_MARGIN){
        rVal += assertEqualsFloat(beforeSum,afterSum,"Advection changed density!  %f %f  \n");
    }

    return rVal;
}

/**
 * Testing full sim routines
 */
int fluid_sim_grid2_full_sim_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_grid2_full_sim_test1();
    rVal += fluid_sim_grid2_full_sim_test2();
    rVal += fluid_sim_grid2_full_sim_test3();

    return rVal;
}