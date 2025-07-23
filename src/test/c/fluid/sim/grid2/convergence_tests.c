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
 * Error margin for tests
 */
#define FLUID_GRID2_PROJECTION_ERROR_MARGIN 0.005f

/**
 * Maximum convergence we want to see in any chunk
 */
#define FLUID_GRID2_CONVERGENCE_MAX_ITERATIONS 20

#define FLUID_GRID2_CONVERVGENCE_FAILURE_THRESHOLD 0.1f



/**
 * Testing full sim routine
 */
int fluid_sim_grid2_convergence_test1(){
    printf("fluid_sim_grid2_convergence_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    float beforeSum = chunk_queue_sum_density(queue);

    //actually simulate
    int frameCount = 1;
    for(int frame = 0; frame < frameCount; frame++){
        //sim
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);
    }

    //test results
    for(int i = 0; i < chunkCount; i++){
        Chunk * chunk = queue[i];
        if(chunk->projectionIterations > FLUID_GRID2_CONVERGENCE_MAX_ITERATIONS || chunk->projectionResidual > FLUID_GRID2_CONVERVGENCE_FAILURE_THRESHOLD){
            printf("Chunk took too many iterations or had too high of a residual! (frames: %d,  chunk count: %d, chunk: %d) \n", frameCount, chunkCount, i);
            printf("Residual:   %f  \n",chunk->projectionResidual);
            printf("Iterations: %d  \n",chunk->projectionIterations);
            printf("\n");
            rVal++;
            break;
        }
    }

    return rVal;
}

/**
 * Testing full sim routine
 */
int fluid_sim_grid2_convergence_test2(){
    printf("fluid_sim_grid2_convergence_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    float beforeSum = chunk_queue_sum_density(queue);

    //actually simulate
    int frameCount = 1;
    for(int frame = 0; frame < frameCount; frame++){
        //sim
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);
    }

    //test results
    for(int i = 0; i < chunkCount; i++){
        Chunk * chunk = queue[i];
        if(chunk->projectionIterations > FLUID_GRID2_CONVERGENCE_MAX_ITERATIONS || chunk->projectionResidual > FLUID_GRID2_CONVERVGENCE_FAILURE_THRESHOLD){
            printf("Chunk took too many iterations or had too high of a residual! (frames: %d,  chunk count: %d, chunk: %d) \n", frameCount, chunkCount, i);
            printf("Residual:   %f  \n",chunk->projectionResidual);
            printf("Iterations: %d  \n",chunk->projectionIterations);
            printf("\n");
            rVal++;
            break;
        }
    }

    return rVal;
}

/**
 * Testing full sim routine
 */
int fluid_sim_grid2_convergence_test3(){
    printf("fluid_sim_grid2_convergence_test3\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,5,5,5);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    chunk_fill_real(queue[13]->d[CENTER_LOC],MAX_FLUID_VALUE);
    chunk_fill_real(queue[13]->u[CENTER_LOC],MAX_FLUID_VALUE);
    float beforeSum = chunk_queue_sum_density(queue);

    //actually simulate
    int frameCount = 1;
    for(int frame = 0; frame < frameCount; frame++){
        //sim
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);
    }

    //test results
    for(int i = 0; i < chunkCount; i++){
        Chunk * chunk = queue[i];
        if(chunk->projectionIterations > FLUID_GRID2_CONVERGENCE_MAX_ITERATIONS || chunk->projectionResidual > FLUID_GRID2_CONVERVGENCE_FAILURE_THRESHOLD){
            printf("Chunk took too many iterations or had too high of a residual! (frames: %d,  chunk count: %d, chunk: %d) \n", frameCount, chunkCount, i);
            printf("Residual:   %f  \n",chunk->projectionResidual);
            printf("Iterations: %d  \n",chunk->projectionIterations);
            printf("\n");
            rVal++;
            break;
        }
    }

    return rVal;
}

/**
 * Testing full sim routines
 */
int fluid_sim_grid2_convergence_tests(){
    int rVal = 0;

    // rVal += fluid_sim_grid2_convergence_test1();
    // rVal += fluid_sim_grid2_convergence_test2();
    // rVal += fluid_sim_grid2_convergence_test3();

    return rVal;
}