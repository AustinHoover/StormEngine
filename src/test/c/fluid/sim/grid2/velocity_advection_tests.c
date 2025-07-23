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
#include "fluid/sim/grid2/velocity.h"
#include "math/ode/multigrid.h"
#include "../../../util/chunk_test_utils.h"
#include "../../../util/test.h"

/**
 * Center of the advection cell
 */
#define FLUID_GRID2_VELOCITY_ADVECTION_CELL_CENTER 24


/**
 * Testing velocity advection
 */
int fluid_sim_grid2_velocity_advection_test1(){
    printf("fluid_sim_grid2_velocity_advection_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(2,2,2)] = MAX_FLUID_VALUE;
    advection_setup_convection_cell(queue, FLUID_GRID2_VELOCITY_ADVECTION_CELL_CENTER);
    float beforeSumX = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_U);
    float beforeSumY = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_V);
    float beforeSumZ = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_W);

    //actually simulate
    int frameCount = 50;
    for(int frame = 0; frame < frameCount; frame++){
        fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
            fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
            fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);
            fluid_grid2_advectVectors(env,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,FLUID_GRID2_SIM_STEP);
            fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
            fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
            fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);
    }

    //test the result
    float afterSumX = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_U);
    float afterSumY = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_V);
    float afterSumZ = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_W);
    if(fabs(beforeSumX - afterSumX) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSumX,afterSumX,"Velocity advection step changed x-velocity sum!  %f %f  \n");
    }
    if(fabs(beforeSumY - afterSumY) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSumX,afterSumX,"Velocity advection step changed y-density sum!  %f %f  \n");
    }
    if(fabs(beforeSumZ - afterSumZ) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSumX,afterSumX,"Velocity advection step changed z-density sum!  %f %f  \n");
    }

    return rVal;
}

/**
 * Testing velocity advection
 */
int fluid_sim_grid2_velocity_advection_test2(){
    printf("fluid_sim_grid2_velocity_advection_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    queue[0]->u[CENTER_LOC][IX(15,15,15)] = 1;
    queue[0]->v[CENTER_LOC][IX(15,15,15)] = 1;
    queue[0]->w[CENTER_LOC][IX(15,15,15)] = 1;
    float beforeSumX = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_U);
    float beforeSumY = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_V);
    float beforeSumZ = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_W);

    //actually simulate
    int frameCount = 1;
    for(int frame = 0; frame < frameCount; frame++){
        int chunkCount = arrlen(queue);
        for(int chunkIndex = 0; chunkIndex < 1; chunkIndex++){
            currentChunk = queue[chunkIndex];
            fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
            fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
            fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);
            fluid_grid2_advectVectors(env,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,FLUID_GRID2_SIM_STEP);
            fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
            fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
            fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);
        }
    }

    //test the result
    float afterSumX = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_U);
    float afterSumY = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_V);
    float afterSumZ = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_W);
    if(fabs(beforeSumX - afterSumX) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSumX,afterSumX,"Velocity advection step changed x-velocity sum!  %f %f  \n");
    }
    if(fabs(beforeSumY - afterSumY) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSumX,afterSumX,"Velocity advection step changed y-density sum!  %f %f  \n");
    }
    if(fabs(beforeSumZ - afterSumZ) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSumX,afterSumX,"Velocity advection step changed z-density sum!  %f %f  \n");
    }

    return rVal;
}


/**
 * Testing velocity advection
 */
int fluid_sim_grid2_velocity_advection_test3(){
    printf("fluid_sim_grid2_velocity_advection_test3\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(2,2,2)] = MAX_FLUID_VALUE;
    advection_setup_convection_cell(queue, FLUID_GRID2_VELOCITY_ADVECTION_CELL_CENTER);
    float beforeSumX = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_U);
    float beforeSumY = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_V);
    float beforeSumZ = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_W);

    //actually simulate
    int frameCount = 50;
    for(int frame = 0; frame < frameCount; frame++){
        fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
        fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
        fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);
        fluid_grid2_advectVectors(env,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,FLUID_GRID2_SIM_STEP);
        fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
        fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
        fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);
    }

    //test the result
    float afterSumX = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_U);
    float afterSumY = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_V);
    float afterSumZ = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_W);
    if(fabs(beforeSumX - afterSumX) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSumX,afterSumX,"Velocity advection step changed x-velocity sum!  %f %f  \n");
    }
    if(fabs(beforeSumY - afterSumY) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSumX,afterSumX,"Velocity advection step changed y-density sum!  %f %f  \n");
    }
    if(fabs(beforeSumZ - afterSumZ) > FLUID_GRID2_REALLY_SMALL_VALUE){
        rVal += assertEqualsFloat(beforeSumX,afterSumX,"Velocity advection step changed z-density sum!  %f %f  \n");
    }

    return rVal;
}




/**
 * Testing velocity advection
 */
int fluid_sim_grid2_velocity_advection_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_grid2_velocity_advection_test1();
    rVal += fluid_sim_grid2_velocity_advection_test2();
    rVal += fluid_sim_grid2_velocity_advection_test3();

    return rVal;
}