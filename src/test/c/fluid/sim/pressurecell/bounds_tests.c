#include <math.h>

#include "stb/stb_ds.h"

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/pressurecell/bounds.h"
#include "fluid/sim/pressurecell/density.h"
#include "fluid/sim/pressurecell/solver_consts.h"
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
#define CHUNK_DIM 4

/**
 * 
 */
int fluid_sim_pressurecell_bounds_test1(){
    printf("fluid_sim_pressurecell_bounds_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,1,1,1);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    float interpConst = 1.0f / FLUID_PRESSURECELL_SPACING;
    currentChunk->vTempCache[IX(1,1,1)] = -0.8;
    currentChunk->bounds[CENTER_LOC][IX(1,0,1)] = 1.0f;

    //actually simulate
    pressurecell_enforce_bounds(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = 0;
    actual = currentChunk->vTempCache[IX(1,1,1)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to enforce advection bounds!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * 
 */
int fluid_sim_pressurecell_bounds_test2(){
    printf("fluid_sim_pressurecell_bounds_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,1,1,1);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    float interpConst = 1.0f / FLUID_PRESSURECELL_SPACING;
    currentChunk->vTempCache[IX(1,1,1)] = -0.8;
    currentChunk->dTempCache[IX(1,1,1)] = MAX_FLUID_VALUE;
    currentChunk->bounds[CENTER_LOC][IX(1,0,1)] = 1.0f;

    //actually simulate
    pressurecell_enforce_bounds(env,currentChunk);
    fluid_pressurecell_advect_density(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = MAX_FLUID_VALUE;
    actual = chunk_sum_density(currentChunk);
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to enforce advection bounds!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * 
 */
int fluid_sim_pressurecell_bounds_test3(){
    printf("fluid_sim_pressurecell_bounds_test3\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,1,1,1);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    float interpConst = 1.0f / FLUID_PRESSURECELL_SPACING;
    currentChunk->vTempCache[IX(1,1,1)] = -0.8;
    currentChunk->dTempCache[IX(1,1,1)] = MAX_FLUID_VALUE;
    currentChunk->bounds[CENTER_LOC][IX(1,0,1)] = 1.0f;

    //actually simulate
    fluid_pressurecell_advect_density(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = MAX_FLUID_VALUE - MAX_FLUID_VALUE * -currentChunk->vTempCache[IX(1,1,1)] * env->consts.dt * interpConst;
    actual = chunk_sum_density(currentChunk);
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to enforce advection bounds!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing bounds logic
 */
int fluid_sim_pressurecell_bounds_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_pressurecell_bounds_test1();
    rVal += fluid_sim_pressurecell_bounds_test2();
    rVal += fluid_sim_pressurecell_bounds_test3();

    return rVal;
}