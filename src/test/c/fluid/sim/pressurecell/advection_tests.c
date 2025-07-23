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
 * Testing advecting values
 */
int fluid_sim_pressurecell_advection_test1(){
    printf("fluid_sim_pressurecell_advection_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->dTempCache[IX(4,4,4)] = MAX_FLUID_VALUE;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    currentChunk->u[CENTER_LOC][IX(5,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;

    //actually simulate
    fluid_pressurecell_advect_density(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = MAX_FLUID_VALUE - FLUID_PRESSURECELL_MAX_VELOCITY * env->consts.dt * MAX_FLUID_VALUE;
    actual = currentChunk->d[CENTER_LOC][IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to advect density correctly (4,4,4)!  expected: %f    actual: %f  \n");
    }


    //
    // neighbors
    //
    expected = FLUID_PRESSURECELL_MAX_VELOCITY * env->consts.dt * MAX_FLUID_VALUE;
    actual = currentChunk->d[CENTER_LOC][IX(5,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to advect density correctly (5,4,4)!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing advecting values
 */
int fluid_sim_pressurecell_advection_test2(){
    printf("fluid_sim_pressurecell_advection_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->uTempCache[IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;

    //actually simulate
    pressurecell_advect_velocity(env,currentChunk);

    //test the result
    float expected, actual;
    //
    // cell that originall had values
    //
    expected = FLUID_PRESSURECELL_MAX_VELOCITY - FLUID_PRESSURECELL_MAX_VELOCITY * env->consts.dt;
    actual = currentChunk->uTempCache[IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,4,4)!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing advecting values
 */
int fluid_sim_pressurecell_advection_tests(int argc, char **argv){
    int rVal = 0;

    // rVal += fluid_sim_pressurecell_advection_test1();
    // rVal += fluid_sim_pressurecell_advection_test2();

    return rVal;
}