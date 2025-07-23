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
 * Difference in density between chunks that is allowed
 */
#define FLUID_GRID_BORDER_DIFFUSION_CHUNK_TEST4_THRESOLD 0.1f

/**
 * Testing full sim routine
 */
int fluid_sim_grid2_border_diffusion_test1(){
    printf("fluid_sim_grid2_border_diffusion_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);
    int chunkCount = arrlen(queue);



    //setup chunk values
    queue[0]->d[CENTER_LOC][IX(DIM-2,1,DIM-2)] = MAX_FLUID_VALUE; //this should immediately bleed into the [0 0 1] chunk

    //set bounds according to neighbors
    fluid_solve_bounds(chunkCount,queue,env);

    //test the result
    float neighboreVal = queue[1]->d0[CENTER_LOC][IX(DIM-2,1,0)];
    if(neighboreVal != MAX_FLUID_VALUE){
        rVal += assertEqualsFloat(MAX_FLUID_VALUE,neighboreVal,"Simulation did not properly add density! expected: %f     actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing full sim routine
 */
int fluid_sim_grid2_border_diffusion_test2(){
    printf("fluid_sim_grid2_border_diffusion_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);
    int chunkCount = arrlen(queue);



    //setup chunk values
    queue[0]->d[CENTER_LOC][IX(DIM-2,1,DIM-2)] = MAX_FLUID_VALUE; //this should immediately bleed into the [0 0 1] chunk

    //set bounds according to neighbors
    fluid_solve_bounds(chunkCount,queue,env);
    fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);
    fluid_solve_bounds(chunkCount,queue,env);

    //test the result
    float newVal = queue[0]->d[CENTER_LOC][IX(DIM-2,1,DIM-2)];
    float originDiff = MAX_FLUID_VALUE - newVal;

    float neighboreVal = queue[1]->d[CENTER_LOC][IX(DIM-2,1,0)];
    float neighborDiff = MAX_FLUID_VALUE - neighboreVal;
    if(originDiff != neighborDiff){
        printf("Failed to set bounds after simulation! \n");
        printf("neighborVal: %f  \n", neighboreVal);
        printf("neighborDiff: %f  \n", neighborDiff);
        printf("newVal: %f  \n", newVal);
        printf("originDiff: %f  \n", originDiff);
        printf("\n");
        rVal++;
    }

    return rVal;
}

/**
 * Testing full sim routine
 */
int fluid_sim_grid2_border_diffusion_test3(){
    printf("fluid_sim_grid2_border_diffusion_test3\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);
    int chunkCount = arrlen(queue);



    //setup chunk values
    queue[0]->d[CENTER_LOC][IX(DIM-2,1,DIM-2)] = MAX_FLUID_VALUE; //this should immediately bleed into the [0 0 1] chunk
    float afterSum, lastSum = 0;
    int frame;

    //set bounds according to neighbors
    int frameCount = 1;
    for(frame = 0; frame < frameCount; frame++){
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);
        fluid_solve_bounds(chunkCount,queue,env);
        afterSum = chunk_sum_density(queue[1]);
        if(lastSum > afterSum){
            break;
        }
    }

    //test the result
    if(afterSum <= lastSum){
        printf("Neighbor sum did not increase on frame %d\n",frame);
        printf("lastSum:   %f   \n", lastSum);
        printf("afterSum:   %f   \n", afterSum);
        printf("\n");
        rVal++;
    }

    return rVal;
}

/**
 * Testing full sim routine
 */
int fluid_sim_grid2_border_diffusion_test4(){
    printf("fluid_sim_grid2_border_diffusion_test4\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);
    int chunkCount = arrlen(queue);



    //setup chunk values
    queue[0]->d[CENTER_LOC][IX(DIM-2,1,DIM-2)] = MAX_FLUID_VALUE; //this should immediately bleed into the [0 0 1] chunk
    float chunk0Sum, chunk1Sum = 0;
    int frame;

    //set bounds according to neighbors
    int frameCount = 1;
    for(frame = 0; frame < frameCount; frame++){
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);
        fluid_solve_bounds(chunkCount,queue,env);
    }

    //test the result
    chunk0Sum = chunk_sum_density(queue[0]);
    chunk1Sum = chunk_sum_density(queue[1]);
    if(fabs(chunk0Sum - chunk1Sum) > FLUID_GRID_BORDER_DIFFUSION_CHUNK_TEST4_THRESOLD){
        printf("Neighbor has significantly different density\n"); 
        printf("chunk0Sum:   %f   \n", chunk0Sum);
        printf("chunk1Sum:   %f   \n", chunk1Sum);
        printf("\n");
        rVal++;
    }

    return rVal;
}

/**
 * Testing mass conservation
 */
int fluid_sim_grid2_border_diffusion_test5(){
    printf("fluid_sim_grid2_border_diffusion_test5\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);
    int chunkCount = arrlen(queue);



    //setup chunk values
    queue[0]->d[CENTER_LOC][IX(DIM-2,1,DIM-2)] = MAX_FLUID_VALUE; //this should immediately bleed into the [0 0 1] chunk
    float beforeSum = chunk_queue_sum_density(queue);
    int frame;

    //set bounds according to neighbors
    int frameCount = 1;
    for(frame = 0; frame < frameCount; frame++){
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);
        fluid_solve_bounds(chunkCount,queue,env);
    }

    //test the result
    float afterSum = chunk_queue_sum_density(queue);
    if(fabs(beforeSum - afterSum) > FLUID_GRID2_PROJECTION_ERROR_MARGIN){
        printf("Significantly different density between start and end! \n");
        printf("beforeSum:   %f   \n", beforeSum);
        printf("afterSum:   %f   \n", afterSum);
        printf("\n");
        rVal++;
    }

    return rVal;
}

/**
 * Testing mass conservation
 */
int fluid_sim_grid2_border_diffusion_test6(){
    printf("fluid_sim_grid2_border_diffusion_test6\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);
    int chunkCount = arrlen(queue);



    //setup chunk values
    queue[0]->d[CENTER_LOC][IX(3,1,DIM-2)] = MAX_FLUID_VALUE; //this should immediately bleed into the [0 0 1] chunk
    float invalidSum;
    int frame;

    //set bounds according to neighbors
    int frameCount = 1;
    for(frame = 0; frame < frameCount; frame++){
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);
        fluid_solve_bounds(chunkCount,queue,env);
        invalidSum = 0;
        for(int i = 2; i < chunkCount; i++){
            float currSum = chunk_sum_density(queue[i]);
            invalidSum = invalidSum + currSum;
            if(currSum != 0){
                printf("Sum in: %d   %f    \n",i,currSum);
            }
        }
        if(invalidSum > 0){
            break;
        }
    }

    //test the result
    if(invalidSum > FLUID_GRID2_PROJECTION_ERROR_MARGIN){
        printf("Significant amounts of fluid in invalid chunks! %d\n", frame);
        printf("invalidSum:   %f   \n", invalidSum);
        printf("\n");
        rVal++;
    }

    return rVal;
}

/**
 * Testing full sim routines
 */
int fluid_sim_grid2_border_diffusion_tests(int argc, char **argv){
    int rVal = 0;

    // rVal += fluid_sim_grid2_border_diffusion_test1();
    rVal += fluid_sim_grid2_border_diffusion_test2();
    // rVal += fluid_sim_grid2_border_diffusion_test3();
    // rVal += fluid_sim_grid2_border_diffusion_test4();
    rVal += fluid_sim_grid2_border_diffusion_test5();
    rVal += fluid_sim_grid2_border_diffusion_test6();

    return rVal;
}