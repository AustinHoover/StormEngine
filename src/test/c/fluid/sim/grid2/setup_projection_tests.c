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
 * Testing velocity advection
 */
int fluid_sim_grid2_setup_projection_test1(){
    printf("fluid_sim_grid2_setup_projection_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->u[CENTER_LOC][IX(3,3,3)] = 1.0f;

    //actually simulate
    fluid_grid2_setupProjection(env,currentChunk,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,FLUID_GRID2_SIM_STEP);

    //test the result
    rVal += assertEqualsFloat(currentChunk->v0[CENTER_LOC][IX(2,3,3)],-0.5f * FLUID_GRID2_H,"Divergence of the vector field at 3,3,3 should be -0.5 * h!   actual: %f    expected: %f  \n");
    rVal += assertEqualsFloat(currentChunk->v0[CENTER_LOC][IX(3,3,3)],0,"Divergence of the vector field at 3,3,3 should be 0!   actual: %f    expected: %f  \n");
    rVal += assertEqualsFloat(currentChunk->v0[CENTER_LOC][IX(4,3,3)],0.5f * FLUID_GRID2_H,"Divergence of the vector field at 4,3,3 should be 0.5 * h!   actual: %f    expected: %f  \n");
    rVal += assertEqualsFloat(currentChunk->v0[CENTER_LOC][IX(5,3,3)],0,"Divergence of the vector field at 5,3,3 should be 0!   actual: %f    expected: %f  \n");

    return rVal;
}

/**
 * Testing velocity advection
 */
int fluid_sim_grid2_setup_projection_test2(){
    printf("fluid_sim_grid2_setup_projection_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->u[CENTER_LOC][IX(3,3,3)] = -1.0f;

    //actually simulate
    fluid_grid2_setupProjection(env,currentChunk,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,FLUID_GRID2_SIM_STEP);

    //test the result
    rVal += assertEqualsFloat(currentChunk->v0[CENTER_LOC][IX(2,3,3)],0.5f * FLUID_GRID2_H,"Divergence of the vector field at 3,3,3 should be 0.5 * h!   actual: %f    expected: %f  \n");
    rVal += assertEqualsFloat(currentChunk->v0[CENTER_LOC][IX(3,3,3)],0,"Divergence of the vector field at 3,3,3 should be 0!   %f %f  \n");
    rVal += assertEqualsFloat(currentChunk->v0[CENTER_LOC][IX(4,3,3)],-0.5f * FLUID_GRID2_H,"Divergence of the vector field at 4,3,3 should be -0.5 * h!   actual: %f    expected: %f  \n");

    return rVal;
}

/**
 * Testing velocity advection
 */
int fluid_sim_grid2_setup_projection_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_grid2_setup_projection_test1();
    rVal += fluid_sim_grid2_setup_projection_test2();

    return rVal;
}