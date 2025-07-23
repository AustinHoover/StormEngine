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
 * Error margin for tests
 */
#define FLUID_GRID2_PROJECTION_ERROR_MARGIN 0.00001f

/**
 * Testing velocity advection
 */
int fluid_sim_grid2_finalize_projection_test1(){
    printf("fluid_sim_grid2_finalize_projection_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->u[CENTER_LOC][IX(3,3,3)] = 1.0f;
    fluid_grid2_setupProjection(env,currentChunk,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,FLUID_GRID2_SIM_STEP);
    fluid_grid2_solveProjection(env,currentChunk,currentChunk->u0,currentChunk->v0,FLUID_GRID2_SIM_STEP);
    fluid_grid2_set_bounds(env,BOUND_SET_PROJECTION_PHI,currentChunk->u0[CENTER_LOC]);

    //finalize
    fluid_grid2_finalizeProjection(env,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,FLUID_GRID2_SIM_STEP);

    //test the result
    float expected, actual;

    {
        float xVel_at_2_3_3 = 0;
        //2,3,3
        expected = currentChunk->u0[CENTER_LOC][IX(3,3,3)] - currentChunk->u0[CENTER_LOC][IX(1,3,3)];
        expected = expected / (2.0f * FLUID_GRID2_H);
        expected = xVel_at_2_3_3 - expected;
        actual = currentChunk->u[CENTER_LOC][IX(2,3,3)];
        if(fabs(expected - actual) > FLUID_GRID2_PROJECTION_ERROR_MARGIN){
            rVal += assertEqualsFloat(expected,actual," - Conservative velocity at 2,3,3 is above error margin!   expected: %f     actual: %f     \n");
        }
    }
    {
        float xVel_at_3_3_3 = 1.0f;
        //3,3,3
        expected = currentChunk->u0[CENTER_LOC][IX(4,3,3)] - currentChunk->u0[CENTER_LOC][IX(2,3,3)];
        expected = expected / (2.0f * FLUID_GRID2_H);
        expected = xVel_at_3_3_3 - expected;
        actual = currentChunk->u[CENTER_LOC][IX(3,3,3)];
        if(fabs(expected - actual) > FLUID_GRID2_PROJECTION_ERROR_MARGIN){
            rVal += assertEqualsFloat(expected,actual," - Conservative velocity at 3,3,3 is above error margin!   expected: %f     actual: %f     \n");
        }
    }

    return rVal;
}


/**
 * Testing velocity advection
 */
int fluid_sim_grid2_finalize_projection_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_grid2_finalize_projection_test1();

    return rVal;
}