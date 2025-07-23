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
#define FLUID_GRID2_PROJECTION_CELL_CENTER 24

/**
 * Error margin for tests
 */
#define FLUID_GRID2_PROJECTION_ERROR_MARGIN 0.00001f

/**
 * Testing velocity advection
 */
int fluid_sim_grid2_advect_projection_test1(){
    printf("fluid_sim_grid2_advect_projection_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    float beforeSum = chunk_queue_sum_density(queue);

    //actually simulate
    int frameCount = 1;
    for(int frame = 0; frame < frameCount; frame++){
        int chunkCount = arrlen(queue);
        for(int chunkIndex = 0; chunkIndex < 1; chunkIndex++){
            currentChunk = queue[chunkIndex];
            //advect velocity
            fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
            fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
            fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);
            fluid_grid2_advectVectors(env,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,FLUID_GRID2_SIM_STEP);
            fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
            fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
            fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);

            ////project
            fluid_grid2_setupProjection(env,currentChunk,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,FLUID_GRID2_SIM_STEP);
            fluid_grid2_set_bounds(env,BOUND_SET_PROJECTION_PHI,currentChunk->u0[CENTER_LOC]);
            fluid_grid2_set_bounds(env,BOUND_SET_PROJECTION_PHI_0,currentChunk->v0[CENTER_LOC]);
            fluid_grid2_solveProjection(env,currentChunk,currentChunk->u0,currentChunk->v0,FLUID_GRID2_SIM_STEP);
            fluid_grid2_set_bounds(env,BOUND_SET_PROJECTION_PHI,currentChunk->u0[CENTER_LOC]);
            fluid_grid2_finalizeProjection(env,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,FLUID_GRID2_SIM_STEP);

            //advect density
            fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
            fluid_grid2_advectDensity(env,currentChunk->d,currentChunk->d0,currentChunk->u,currentChunk->v,currentChunk->w,FLUID_GRID2_SIM_STEP);
        }
    }

    //test the result
    float afterSum = chunk_queue_sum_density(queue);
    if(fabs(beforeSum - afterSum) > FLUID_GRID2_PROJECTION_ERROR_MARGIN){
        rVal += assertEqualsFloat(beforeSum,afterSum,"Advection changed density!  %f %f  \n");
    }

    return rVal;
}

/**
 * Testing velocity advection
 */
int fluid_sim_grid2_advect_projection_compute_error_over_time(){
    printf("fluid_sim_grid2_advect_projection_compute_error_over_time\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);



    for(int complexity = 2; complexity < DIM-1; complexity++){
        //actually simulate
        for(int frameCount = 1; frameCount < 50; frameCount++){
            //setup chunk values
            Chunk * currentChunk = queue[0];
            chunk_fill(currentChunk,0);
            for(int x = 1; x < complexity; x++){
                for(int y = 1; y < complexity; y++){
                    for(int z = 1; z < complexity; z++){
                        currentChunk->d[CENTER_LOC][IX(x,y,z)] = MAX_FLUID_VALUE;
                        currentChunk->u[CENTER_LOC][IX(x,y,z)] = MAX_FLUID_VALUE;
                    }
                }
            }
            float beforeSum = chunk_queue_sum_density(queue);

            //solve
            for(int frame = 0; frame < frameCount; frame++){
                int chunkCount = arrlen(queue);
                for(int chunkIndex = 0; chunkIndex < 1; chunkIndex++){
                    currentChunk = queue[chunkIndex];
                    //advect velocity
                    fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
                    fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
                    fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);
                    fluid_grid2_advectVectors(env,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,FLUID_GRID2_SIM_STEP);
                    fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
                    fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
                    fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);

                    ////project
                    fluid_grid2_setupProjection(env,currentChunk,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,FLUID_GRID2_SIM_STEP);
                    fluid_grid2_set_bounds(env,BOUND_SET_PROJECTION_PHI,currentChunk->u0[CENTER_LOC]);
                    fluid_grid2_set_bounds(env,BOUND_SET_PROJECTION_PHI_0,currentChunk->v0[CENTER_LOC]);
                    fluid_grid2_solveProjection(env,currentChunk,currentChunk->u0,currentChunk->v0,FLUID_GRID2_SIM_STEP);
                    fluid_grid2_set_bounds(env,BOUND_SET_PROJECTION_PHI,currentChunk->u0[CENTER_LOC]);
                    fluid_grid2_finalizeProjection(env,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,FLUID_GRID2_SIM_STEP);

                    //advect density
                    fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
                    fluid_grid2_advectDensity(env,currentChunk->d,currentChunk->d0,currentChunk->u,currentChunk->v,currentChunk->w,FLUID_GRID2_SIM_STEP);
                }
            }
            //test the result
            float afterSum = chunk_queue_sum_density(queue);
            if(fabs(beforeSum - afterSum) > FLUID_GRID2_PROJECTION_ERROR_MARGIN){
                printf("%f,",fabs(beforeSum - afterSum));
                // rVal += assertEqualsFloat(beforeSum,afterSum,"Advection changed density!  %f %f  \n");
            }
        }
        printf("\n");
    }

    rVal++;
    return rVal;
}

/**
 * Testing velocity advection
 */
int fluid_sim_grid2_advect_projection_tests(int argc, char **argv){
    int rVal = 0;

    // rVal += fluid_sim_grid2_advect_projection_test1();
    // rVal += fluid_sim_grid2_advect_projection_compute_error_over_time();

    return rVal;
}