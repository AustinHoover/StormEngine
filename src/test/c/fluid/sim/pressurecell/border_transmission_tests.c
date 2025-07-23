#include <math.h>

#include "stb/stb_ds.h"

#include "fluid/dispatch/dispatcher.h"
#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/simulator.h"
#include "fluid/sim/pressurecell/normalization.h"
#include "fluid/sim/pressurecell/solver_consts.h"
#include "fluid/tracking/tracking.h"
#include "math/ode/multigrid.h"
#include "../../../util/chunk_test_utils.h"
#include "../../../util/test.h"

/**
 * Error margin for tests
 */
#define FLUID_PRESSURE_CELL_ERROR_MARGIN 0.01f

/**
 * Number of chunks
 */
#define CHUNK_DIM 2

/**
 * Testing normalizing values
 */
int fluid_sim_pressurecell_border_transmission_test1(){
    printf("fluid_sim_pressurecell_border_transmission_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(DIM-2,1,DIM-2)] = 0.2;

    //actually simulate
    for(int i = 0; i < 10; i++){
        fluid_tracking_reset(env);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_PRESSURECELL);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
    }

    //test the result
    float expected, actual;

    //should be chunk at 0,0,1
    Chunk * furtherX = queue[1];
    rVal += assertEquals(furtherX->x,0,"Chunk is not at 0,0,1  x: %d \n");
    rVal += assertEquals(furtherX->y,0,"Chunk is not at 0,0,1  y: %d \n");
    rVal += assertEquals(furtherX->z,1,"Chunk is not at 0,0,1  z: %d \n");

    //
    // chunk in z direction should have density > 0 at 1,1,1
    //
    expected = MAX_FLUID_VALUE;
    actual = furtherX->d[CENTER_LOC][IX(DIM-2,1,1)];
    if(actual < FLUID_PRESSURE_CELL_ERROR_MARGIN){
        printf("Failed to gain density at (DIM-2,1,1)! actual: %f  \n", actual);
        rVal++;
    }

    expected = MAX_FLUID_VALUE;
    actual = furtherX->d[CENTER_LOC][IX(DIM-2,1,0)];
    if(actual < FLUID_PRESSURE_CELL_ERROR_MARGIN){
        printf("Failed to gain density at (DIM-2,1,0)! actual: %f  \n", actual);
        rVal++;
    }

    //
    // origin chunk should have density > 0 at DIM-1,1,DIM-1
    //
    actual = currentChunk->d[CENTER_LOC][IX(DIM-2,1,DIM-2)];
    if(actual < FLUID_PRESSURE_CELL_ERROR_MARGIN){
        printf("Failed to retain density at (DIM-2,1,DIM-2)! actual: %f  \n", actual);
        rVal++;
    }

    return rVal;
}

/**
 * Testing normalizing values
 */
int fluid_sim_pressurecell_border_transmission_test2(){
    printf("fluid_sim_pressurecell_border_transmission_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(DIM-2,1,DIM-2)] = 0.2;

    //actually simulate
    fluid_solve_bounds(chunkCount,queue,env);

    //test the result
    float expected, actual;

    //should be chunk at 0,0,1
    Chunk * furtherX = queue[1];
    rVal += assertEquals(furtherX->x,0,"Chunk is not at 0,0,1  x: %d \n");
    rVal += assertEquals(furtherX->y,0,"Chunk is not at 0,0,1  y: %d \n");
    rVal += assertEquals(furtherX->z,1,"Chunk is not at 0,0,1  z: %d \n");

    //
    // chunk in z direction should have density > 0 at 1,1,1
    //
    expected = MAX_FLUID_VALUE;
    actual = furtherX->d[CENTER_LOC][IX(DIM-2,1,0)];
    if(actual <= MIN_FLUID_VALUE){
        printf("Failed to gain density at (0,1,0)! actual: %f  \n", actual);
        rVal++;
    }

    //
    // origin chunk should have density > 0 at DIM-1,1,DIM-1
    //
    actual = currentChunk->d[CENTER_LOC][IX(DIM-2,1,DIM-2)];
    if(actual <= MIN_FLUID_VALUE){
        printf("Failed to retain density at (DIM-2,1,DIM-2)! actual: %f  \n", actual);
        rVal++;
    }

    return rVal;
}

/**
 * Testing normalizing values
 */
int fluid_sim_pressurecell_border_transmission_test3(){
    printf("fluid_sim_pressurecell_border_transmission_test3\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];

    //should be chunk at 0,0,1
    Chunk * furtherZ = queue[1];
    rVal += assertEquals(furtherZ->x,0,"Chunk is not at 0,0,1  x: %d \n");
    rVal += assertEquals(furtherZ->y,0,"Chunk is not at 0,0,1  y: %d \n");
    rVal += assertEquals(furtherZ->z,1,"Chunk is not at 0,0,1  z: %d \n");

    //actually simulate
    for(int i = 0; i < 100; i++){
        //set bounds of 0,0,1 in particular
        for(int x = 1; x < DIM-1; x++){
            currentChunk->d[CENTER_LOC][IX(x,1,DIM-2)] = 0.03f;
            currentChunk->divergenceCache[CENTER_LOC][IX(x,1,DIM-2)] = 0.5f;
            currentChunk->pressureCache[CENTER_LOC][IX(x,1,DIM-2)] = 0.5f;
        }
        fluid_tracking_reset(env);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_PRESSURECELL);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
    }

    //test the result
    float expected, actual;

    //
    // chunk in z direction should have density > 0 at 1,1,1
    //
    expected = MAX_FLUID_VALUE;
    actual = furtherZ->d[CENTER_LOC][IX(5,1,2)];
    if(actual < FLUID_PRESSURE_CELL_ERROR_MARGIN){
        printf("Failed to gain density at (5,1,2)! actual: %f  \n", actual);
        rVal++;
    }

    expected = MAX_FLUID_VALUE;
    actual = furtherZ->d[CENTER_LOC][IX(DIM-2,1,0)];
    if(actual < FLUID_PRESSURE_CELL_ERROR_MARGIN){
        printf("Failed to gain density at (DIM-2,1,0)! actual: %f  \n", actual);
        rVal++;
    }

    return rVal;
}

/**
 * Testing transmitting density across borders
 */
int fluid_sim_pressurecell_border_transmission_tests(int argc, char **argv){
    int rVal = 0;

    // rVal += fluid_sim_pressurecell_border_transmission_test1();
    rVal += fluid_sim_pressurecell_border_transmission_test2();
    // rVal += fluid_sim_pressurecell_border_transmission_test3();

    return rVal;
}

