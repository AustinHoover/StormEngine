#include <math.h>

#include "stb/stb_ds.h"

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/queue/metadatacalc.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/dispatch/dispatcher.h"
#include "fluid/sim/simulator.h"
#include "fluid/sim/pressurecell/pressure.h"
#include "fluid/sim/pressurecell/pressurecell.h"
#include "fluid/sim/pressurecell/solver_consts.h"
#include "fluid/tracking/tracking.h"
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
 * Testing normalizing values
 */
int fluid_sim_pressurecell_add_gravity_test1(){
    printf("fluid_sim_pressurecell_add_gravity_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    for(int x = 0; x < DIM; x++){
        for(int y = 0; y < DIM; y++){
            currentChunk->d[CENTER_LOC][IX(x,1,y)] = MAX_FLUID_VALUE;
        }
    }

    //actually simulate
    fluid_tracking_reset(env);
    fluid_solve_bounds(chunkCount,queue,env);
    fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_PRESSURECELL);
    fluid_simulate(env);
    fluid_solve_bounds(chunkCount,queue,env);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    float gravForce = FLUID_PRESSURECELL_SIM_STEP * FLUID_PRESSURECELL_GRAVITY;
    gravForce = fmax(fmin(1.0f, gravForce),-1.0f); //gravity force is clamped
    expected = -0.87;
    actual = currentChunk->v[CENTER_LOC][IX(1,1,DIM-2)];
    if(fabs(actual) < FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal++;
        printf("Gravity not applied!\n");
        printf("grav(1,1,DIM-2): %f \n", actual);
        printf("expected: %f \n", expected);
        printf("diff:  %f  \n", fabs(expected - actual));
    }

    return rVal;
}

/**
 * Testing pressure values
 */
int fluid_sim_pressurecell_add_gravity_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_pressurecell_add_gravity_test1();

    return rVal;
}