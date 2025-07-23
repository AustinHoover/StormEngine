#include <math.h>

#include "stb/stb_ds.h"

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/pressurecell/pressure.h"
#include "fluid/sim/pressurecell/solver_consts.h"
#include "math/ode/multigrid.h"
#include "../../../util/chunk_test_utils.h"
#include "../../../util/test.h"

/**
 * Error margin for tests
 */
#define FLUID_PRESSURE_CELL_ERROR_MARGIN 0.00001f

/**
 * Number of chunks
 */
#define CHUNK_DIM 4

/**
 * Testing pressure values
 */
int fluid_sim_pressurecell_divergence_test1(){
    printf("fluid_sim_pressurecell_divergence_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    currentChunk->divergenceCache[CENTER_LOC][IX(4,4,4)] = 0;

    currentChunk->uTempCache[IX(3,4,4)] = 1;
    currentChunk->uTempCache[IX(5,4,4)] = -1;
    currentChunk->vTempCache[IX(4,3,4)] = 1;
    currentChunk->vTempCache[IX(4,5,4)] = -1;
    currentChunk->wTempCache[IX(4,4,3)] = 1;
    currentChunk->wTempCache[IX(4,4,5)] = -1;

    //actually simulate
    pressurecell_approximate_divergence(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = 3;
    actual = currentChunk->divergenceCache[CENTER_LOC][IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to calculate divergence correctly (4,4,4)!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing pressure values
 */
int fluid_sim_pressurecell_divergence_test2(){
    printf("fluid_sim_pressurecell_divergence_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];

    currentChunk->uTempCache[IX(0,1,1)] = 0;
    currentChunk->uTempCache[IX(2,1,1)] = 1;
    currentChunk->vTempCache[IX(1,0,1)] = 0;
    currentChunk->vTempCache[IX(1,2,1)] = -1;
    currentChunk->wTempCache[IX(1,1,0)] = 0;
    currentChunk->wTempCache[IX(1,1,2)] = -1;

    //actually simulate
    pressurecell_approximate_divergence(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = 0.5;
    actual = currentChunk->divergenceCache[CENTER_LOC][IX(1,1,1)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to calculate divergence correctly (1,1,1)!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing pressure values
 */
int fluid_sim_pressurecell_divergence_test3(){
    printf("fluid_sim_pressurecell_divergence_test3\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];

    currentChunk->uTempCache[IX(1,1,1)] = 1;
    currentChunk->vTempCache[IX(1,1,1)] = 1;
    currentChunk->wTempCache[IX(1,1,1)] = 1;

    currentChunk->uTempCache[IX(2,1,1)] = 1;
    currentChunk->vTempCache[IX(1,2,1)] = 1;
    currentChunk->wTempCache[IX(1,1,2)] = -1;

    //actually simulate
    pressurecell_approximate_divergence(env,currentChunk);

    //test the result
    float expected, actual;
    int cx, cy, cz;
    float u, v, w;
    float sum;

    //
    // cell that originall had values
    //
    cx = 1;
    cy = 1;
    cz = 1;
    expected = currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy,cz)];
    if(expected != -0.5){ //we expect 0.5 velocity to leave this cell in one iteration
        rVal++;
        printf("Divergence calc failed\n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("%f  \n",expected);
        printf("div at[%d,%d,%d]  %f  \n", cx,cy,cz,    currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy,cz)]);
        printf("div at[%d,%d,%d]  %f  \n", cx-1,cy,cz,  currentChunk->divergenceCache[CENTER_LOC][IX(cx-1,cy,cz)]);
        printf("div at[%d,%d,%d]  %f  \n", cx+1,cy,cz,  currentChunk->divergenceCache[CENTER_LOC][IX(cx+1,cy,cz)]);
        printf("div at[%d,%d,%d]  %f  \n", cx,cy-1,cz,  currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy-1,cz)]);
        printf("div at[%d,%d,%d]  %f  \n", cx,cy+1,cz,  currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy+1,cz)]);
        printf("div at[%d,%d,%d]  %f  \n", cx,cy,cz-1,  currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy,cz-1)]);
        printf("div at[%d,%d,%d]  %f  \n", cx,cy,cz+1,  currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy,cz+1)]);
        printf("\n");
    }

    cx = 2;
    cy = 1;
    cz = 1;
    expected = currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy,cz)];
    if(expected != 0.5){//we expect 0.5 velocity to enter this cell in one iteration
        rVal++;
        printf("Divergence calc failed\n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("%f  \n",expected);
        printf("div at[%d,%d,%d]  %f  \n", cx,cy,cz,    currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy,cz)]);
        printf("div at[%d,%d,%d]  %f  \n", cx-1,cy,cz,  currentChunk->divergenceCache[CENTER_LOC][IX(cx-1,cy,cz)]);
        printf("div at[%d,%d,%d]  %f  \n", cx+1,cy,cz,  currentChunk->divergenceCache[CENTER_LOC][IX(cx+1,cy,cz)]);
        printf("div at[%d,%d,%d]  %f  \n", cx,cy-1,cz,  currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy-1,cz)]);
        printf("div at[%d,%d,%d]  %f  \n", cx,cy+1,cz,  currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy+1,cz)]);
        printf("div at[%d,%d,%d]  %f  \n", cx,cy,cz-1,  currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy,cz-1)]);
        printf("div at[%d,%d,%d]  %f  \n", cx,cy,cz+1,  currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy,cz+1)]);
        printf("\n");
    }

    return rVal;
}

/**
 * Testing pressure values
 */
int fluid_sim_pressurecell_divergence_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_pressurecell_divergence_test1();
    rVal += fluid_sim_pressurecell_divergence_test2();
    rVal += fluid_sim_pressurecell_divergence_test3();

    return rVal;
}