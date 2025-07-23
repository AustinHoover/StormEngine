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
 * Testing diffusing values
 */
int fluid_sim_pressurecell_diffuse_test1(){
    printf("fluid_sim_pressurecell_diffuse_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    float diffuseConst = FLUID_PRESSURECELL_DIFFUSION_CONSTANT / (FLUID_PRESSURECELL_SPACING * FLUID_PRESSURECELL_SPACING);

    //actually simulate
    fluid_pressurecell_diffuse_density(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = MAX_FLUID_VALUE - diffuseConst * 6 * MAX_FLUID_VALUE * env->consts.dt;
    actual = currentChunk->dTempCache[IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,4,4)!  expected: %f    actual: %f  \n");
    }


    //
    // neighbors
    //
    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = currentChunk->dTempCache[IX(3,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (3,4,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = currentChunk->dTempCache[IX(5,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN * env->consts.dt){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (5,4,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = currentChunk->dTempCache[IX(4,3,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,3,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = currentChunk->dTempCache[IX(4,5,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,5,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = currentChunk->dTempCache[IX(4,4,3)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,4,3)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = currentChunk->dTempCache[IX(4,4,5)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,4,5)!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing diffusing values
 */
int fluid_sim_pressurecell_diffuse_test2(){
    printf("fluid_sim_pressurecell_diffuse_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    float * uTemp = currentChunk->uTempCache;
    float * vTemp = currentChunk->vTempCache;
    float * wTemp = currentChunk->wTempCache;
    float * uArr = currentChunk->u0[CENTER_LOC];
    uTemp[IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    vTemp[IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    wTemp[IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    float diffuseConst = FLUID_PRESSURECELL_DIFFUSION_CONSTANT / (FLUID_PRESSURECELL_SPACING * FLUID_PRESSURECELL_SPACING);

    //actually simulate
    pressurecell_diffuse_velocity(env,currentChunk);

    //test the result
    float expected, actual;
    //
    // cell that originall had values
    //
    expected = MAX_FLUID_VALUE - diffuseConst * 6 * MAX_FLUID_VALUE * env->consts.dt;
    actual = uArr[IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,4,4)!  expected: %f    actual: %f  \n");
    }


    //
    // neighbors
    //
    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = uArr[IX(3,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (3,4,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = uArr[IX(5,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (5,4,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = uArr[IX(4,3,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,3,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = uArr[IX(4,5,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,5,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = uArr[IX(4,4,3)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,4,3)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = uArr[IX(4,4,5)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,4,5)!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing diffusing values with bounds
 */
int fluid_sim_pressurecell_diffuse_test3(){
    printf("fluid_sim_pressurecell_diffuse_test3\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    float * dArr = currentChunk->d[CENTER_LOC];
    float * dTemp = currentChunk->dTempCache;
    float * bounds = currentChunk->bounds[CENTER_LOC];
    float diffuseConst = FLUID_PRESSURECELL_DIFFUSION_CONSTANT / (FLUID_PRESSURECELL_SPACING * FLUID_PRESSURECELL_SPACING);
    dArr[IX(4,4,4)] = MAX_FLUID_VALUE;
    bounds[IX(4,5,4)] = 1.0f;

    //actually simulate
    fluid_pressurecell_diffuse_density(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = MAX_FLUID_VALUE - diffuseConst * 5 * MAX_FLUID_VALUE * env->consts.dt;
    actual = dTemp[IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,4,4)!  expected: %f    actual: %f  \n");
    }


    //
    // neighbors
    //
    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = dTemp[IX(3,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (3,4,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = dTemp[IX(5,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN * env->consts.dt){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (5,4,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = dTemp[IX(4,3,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,3,4)!  expected: %f    actual: %f  \n");
    }

    expected = 0;
    actual = dTemp[IX(4,5,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,5,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = dTemp[IX(4,4,3)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,4,3)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * MAX_FLUID_VALUE * env->consts.dt;
    actual = dTemp[IX(4,4,5)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,4,5)!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing diffusing values with bounds
 */
int fluid_sim_pressurecell_diffuse_test4(){
    printf("fluid_sim_pressurecell_diffuse_test4\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    float * dArr = currentChunk->d[CENTER_LOC];
    float * dTemp = currentChunk->dTempCache;
    float * bounds = currentChunk->bounds[CENTER_LOC];
    float diffuseConst = FLUID_PRESSURECELL_DIFFUSION_CONSTANT / (FLUID_PRESSURECELL_SPACING * FLUID_PRESSURECELL_SPACING);
    dArr[IX(4,4,4)] = MAX_FLUID_VALUE;
    bounds[IX(3,4,4)] = 1.0f;
    bounds[IX(5,4,4)] = 1.0f;
    bounds[IX(4,3,4)] = 1.0f;
    bounds[IX(4,5,4)] = 1.0f;
    bounds[IX(4,4,3)] = 1.0f;
    bounds[IX(4,4,5)] = 1.0f;

    //actually simulate
    fluid_pressurecell_diffuse_density(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = MAX_FLUID_VALUE;
    actual = dTemp[IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,4,4)!  expected: %f    actual: %f  \n");
    }


    //
    // neighbors
    //
    expected = 0;
    actual = dTemp[IX(3,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (3,4,4)!  expected: %f    actual: %f  \n");
    }

    expected = 0;
    actual = dTemp[IX(5,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN * env->consts.dt){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (5,4,4)!  expected: %f    actual: %f  \n");
    }

    expected = 0;
    actual = dTemp[IX(4,3,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,3,4)!  expected: %f    actual: %f  \n");
    }

    expected = 0;
    actual = dTemp[IX(4,5,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,5,4)!  expected: %f    actual: %f  \n");
    }

    expected = 0;
    actual = dTemp[IX(4,4,3)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,4,3)!  expected: %f    actual: %f  \n");
    }

    expected = 0;
    actual = dTemp[IX(4,4,5)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse density correctly (4,4,5)!  expected: %f    actual: %f  \n");
    }

    return rVal;
}


/**
 * Testing diffusing values
 */
int fluid_sim_pressurecell_diffuse_test5(){
    printf("fluid_sim_pressurecell_diffuse_test5\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    float * uTemp = currentChunk->uTempCache;
    float * vTemp = currentChunk->vTempCache;
    float * wTemp = currentChunk->wTempCache;
    float * uArr = currentChunk->u0[CENTER_LOC];
    float * bounds = currentChunk->bounds[CENTER_LOC];
    float diffuseConst = FLUID_PRESSURECELL_DIFFUSION_CONSTANT / (FLUID_PRESSURECELL_SPACING * FLUID_PRESSURECELL_SPACING);
    uTemp[IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    vTemp[IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    wTemp[IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    bounds[IX(5,4,4)] = 1.0f;

    //actually simulate
    pressurecell_diffuse_velocity(env,currentChunk);

    //test the result
    float expected, actual;
    //
    // cell that originall had values
    //
    expected = FLUID_PRESSURECELL_MAX_VELOCITY - diffuseConst * 5 * FLUID_PRESSURECELL_MAX_VELOCITY * env->consts.dt;
    actual = uArr[IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,4,4)!  expected: %f    actual: %f  \n");
    }


    //
    // neighbors
    //
    expected = diffuseConst * FLUID_PRESSURECELL_MAX_VELOCITY * env->consts.dt;
    actual = uArr[IX(3,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (3,4,4)!  expected: %f    actual: %f  \n");
    }

    expected = 0;
    actual = uArr[IX(5,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (5,4,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * FLUID_PRESSURECELL_MAX_VELOCITY * env->consts.dt;
    actual = uArr[IX(4,3,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,3,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * FLUID_PRESSURECELL_MAX_VELOCITY * env->consts.dt;
    actual = uArr[IX(4,5,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,5,4)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * FLUID_PRESSURECELL_MAX_VELOCITY * env->consts.dt;
    actual = uArr[IX(4,4,3)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,4,3)!  expected: %f    actual: %f  \n");
    }

    expected = diffuseConst * FLUID_PRESSURECELL_MAX_VELOCITY * env->consts.dt;
    actual = uArr[IX(4,4,5)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,4,5)!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing diffusing values
 */
int fluid_sim_pressurecell_diffuse_test6(){
    printf("fluid_sim_pressurecell_diffuse_test6\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    float * uTemp = currentChunk->uTempCache;
    float * vTemp = currentChunk->vTempCache;
    float * wTemp = currentChunk->wTempCache;
    float * uArr = currentChunk->u0[CENTER_LOC];
    float * bounds = currentChunk->bounds[CENTER_LOC];
    float diffuseConst = FLUID_PRESSURECELL_DIFFUSION_CONSTANT / (FLUID_PRESSURECELL_SPACING * FLUID_PRESSURECELL_SPACING);
    uTemp[IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    vTemp[IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    wTemp[IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    bounds[IX(3,4,4)] = 1.0f;
    bounds[IX(5,4,4)] = 1.0f;
    bounds[IX(4,3,4)] = 1.0f;
    bounds[IX(4,5,4)] = 1.0f;
    bounds[IX(4,4,3)] = 1.0f;
    bounds[IX(4,4,5)] = 1.0f;

    //actually simulate
    pressurecell_diffuse_velocity(env,currentChunk);

    //test the result
    float expected, actual;
    //
    // cell that originall had values
    //
    expected = FLUID_PRESSURECELL_MAX_VELOCITY;
    actual = uArr[IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,4,4)!  expected: %f    actual: %f  \n");
    }


    //
    // neighbors
    //
    expected = 0;
    actual = uArr[IX(3,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (3,4,4)!  expected: %f    actual: %f  \n");
    }

    expected = 0;
    actual = uArr[IX(5,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (5,4,4)!  expected: %f    actual: %f  \n");
    }

    expected = 0;
    actual = uArr[IX(4,3,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,3,4)!  expected: %f    actual: %f  \n");
    }

    expected = 0;
    actual = uArr[IX(4,5,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,5,4)!  expected: %f    actual: %f  \n");
    }

    expected = 0;
    actual = uArr[IX(4,4,3)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,4,3)!  expected: %f    actual: %f  \n");
    }

    expected = 0;
    actual = uArr[IX(4,4,5)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to diffuse velocity correctly (4,4,5)!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing adding source values
 */
int fluid_sim_pressurecell_diffuse_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_pressurecell_diffuse_test1();
    rVal += fluid_sim_pressurecell_diffuse_test2();
    rVal += fluid_sim_pressurecell_diffuse_test3();
    rVal += fluid_sim_pressurecell_diffuse_test4();
    rVal += fluid_sim_pressurecell_diffuse_test5();
    rVal += fluid_sim_pressurecell_diffuse_test6();

    return rVal;
}