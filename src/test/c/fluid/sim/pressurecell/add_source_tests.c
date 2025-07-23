#include <math.h>

#include "stb/stb_ds.h"

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/pressurecell/density.h"
#include "fluid/sim/pressurecell/velocity.h"
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
 * Testing adding source values
 */
int fluid_sim_pressurecell_add_source_test1(){
    printf("fluid_sim_pressurecell_add_source_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d0[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;

    //actually simulate
    fluid_pressurecell_add_density(env,currentChunk);

    //test the result
    float expected, actual;
    expected = MAX_FLUID_VALUE;
    actual = currentChunk->d[CENTER_LOC][IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to add d0!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing adding source values
 */
int fluid_sim_pressurecell_add_source_test2(){
    printf("fluid_sim_pressurecell_add_source_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->u0[CENTER_LOC][IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    currentChunk->v0[CENTER_LOC][IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    currentChunk->w0[CENTER_LOC][IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;

    //actually simulate
    pressurecell_add_velocity(env,currentChunk);

    //test the result
    float expected, actual;
    expected = FLUID_PRESSURECELL_MAX_VELOCITY;
    actual = currentChunk->uTempCache[IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to add u0!  expected: %f    actual: %f  \n");
    }
    expected = FLUID_PRESSURECELL_MAX_VELOCITY;
    actual = currentChunk->vTempCache[IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to add v0!  expected: %f    actual: %f  \n");
    }
    expected = FLUID_PRESSURECELL_MAX_VELOCITY;
    actual = currentChunk->wTempCache[IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to add w0!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing adding source values
 */
int fluid_sim_pressurecell_add_source_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_pressurecell_add_source_test1();
    rVal += fluid_sim_pressurecell_add_source_test2();

    return rVal;
}