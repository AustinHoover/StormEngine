
#include "stb/stb_ds.h"

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/grid2/solver_consts.h"
#include "fluid/sim/grid2/utilities.h"
#include "fluid/sim/grid2/velocity.h"
#include "math/ode/multigrid.h"
#include "../../../util/chunk_test_utils.h"
#include "../../../util/test.h"


/**
 * Placement value for populating velocity arrays
 */
#define FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL 1.0f





/**
 * Testing velocity diffusion
 */
int fluid_sim_grid2_velocity_diffuse_test1(){
    int rVal = 0;
    printf("fluid_sim_grid2_velocity_diffuse_test1\n");
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);




    Chunk * currentChunk = queue[0];
    currentChunk->u[CENTER_LOC][IX(2,2,2)] = FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL;
    currentChunk->v[CENTER_LOC][IX(3,2,2)] = FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL;
    currentChunk->w[CENTER_LOC][IX(2,2,3)] = FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL;

    float beforeSumX = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_U);
    float beforeSumY = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_V);
    float beforeSumZ = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_W);
    rVal += assertEqualsFloat(beforeSumX,FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL,"x-velocity diffuse step changed velocity sum!  %f %f  \n");
    rVal += assertEqualsFloat(beforeSumY,FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL,"y-velocity diffuse step changed velocity sum!  %f %f  \n");
    rVal += assertEqualsFloat(beforeSumZ,FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL,"z-velocity diffuse step changed velocity sum!  %f %f  \n");

    fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
    fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
    fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);
    //diffuse density
    //solve vector diffusion
    for(int l = 0; l < FLUID_GRID2_LINEARSOLVERTIMES; l++){
        //solve vector diffusion
        fluid_grid2_solveVectorDiffuse(env,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,FLUID_GRID2_SIM_STEP);
        //update array for vectors
        fluid_grid2_set_bounds(env,BOUND_SET_VECTOR_U,currentChunk->u[CENTER_LOC]);
        fluid_grid2_set_bounds(env,BOUND_SET_VECTOR_V,currentChunk->v[CENTER_LOC]);
        fluid_grid2_set_bounds(env,BOUND_SET_VECTOR_W,currentChunk->w[CENTER_LOC]);
    }
    //swap all density arrays
    //swap vector fields
    fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
    fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
    fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);

    //sum the result
    float afterSumX = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_U);
    float afterSumY = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_V);
    float afterSumZ = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_W);

    //actually check
    rVal += assertEqualsFloat(beforeSumX,afterSumX,"x-velocity diffuse step changed velocity sum!  %f %f  \n");
    rVal += assertEqualsFloat(beforeSumY,afterSumY,"y-velocity diffuse step changed velocity sum!  %f %f  \n");
    rVal += assertEqualsFloat(beforeSumZ,afterSumZ,"z-velocity diffuse step changed velocity sum!  %f %f  \n");

    return rVal;
}


/**
 * Testing velocity diffusion
 */
int fluid_sim_grid2_velocity_diffuse_test2(){
    int rVal = 0;
    printf("fluid_sim_grid2_velocity_diffuse_test2\n");
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);




    Chunk * currentChunk = queue[0];
    currentChunk->u[CENTER_LOC][IX(2,2,2)] = FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL;
    currentChunk->v[CENTER_LOC][IX(3,2,2)] = FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL;
    currentChunk->w[CENTER_LOC][IX(2,2,3)] = FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL;

    float beforeSumX = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_U);
    float beforeSumY = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_V);
    float beforeSumZ = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_W);
    rVal += assertEqualsFloat(beforeSumX,FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL,"x-velocity diffuse step changed velocity sum!  %f %f  \n");
    rVal += assertEqualsFloat(beforeSumY,FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL,"y-velocity diffuse step changed velocity sum!  %f %f  \n");
    rVal += assertEqualsFloat(beforeSumZ,FLUID_GRID2_VELOCITY_DIFFUSE_TESTS_PLACEMENT_VAL,"z-velocity diffuse step changed velocity sum!  %f %f  \n");

    int frameCount = 50;
    for(int frame = 0; frame < frameCount; frame++){
        fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
        fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
        fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);
        //diffuse density
        //solve vector diffusion
        for(int l = 0; l < FLUID_GRID2_LINEARSOLVERTIMES; l++){
            //solve vector diffusion
            fluid_grid2_solveVectorDiffuse(env,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,FLUID_GRID2_SIM_STEP);
            //update array for vectors
            fluid_grid2_set_bounds(env,BOUND_SET_VECTOR_U,currentChunk->u[CENTER_LOC]);
            fluid_grid2_set_bounds(env,BOUND_SET_VECTOR_V,currentChunk->v[CENTER_LOC]);
            fluid_grid2_set_bounds(env,BOUND_SET_VECTOR_W,currentChunk->w[CENTER_LOC]);
        }
        //swap all density arrays
        //swap vector fields
        fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
        fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
        fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);
    }

    //sum the result
    float afterSumX = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_U);
    float afterSumY = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_V);
    float afterSumZ = chunk_queue_sum_velocity(queue,FLUID_GRID2_DIRECTION_W);

    //actually check
    rVal += assertEqualsFloat(beforeSumX,afterSumX,"x-velocity diffuse step changed velocity sum!  %f %f  \n");
    rVal += assertEqualsFloat(beforeSumY,afterSumY,"y-velocity diffuse step changed velocity sum!  %f %f  \n");
    rVal += assertEqualsFloat(beforeSumZ,afterSumZ,"z-velocity diffuse step changed velocity sum!  %f %f  \n");

    return rVal;
}




/**
 * Testing velocity diffusion
 */
int fluid_sim_grid2_velocity_diffuse_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_grid2_velocity_diffuse_test1();

    return rVal;
}