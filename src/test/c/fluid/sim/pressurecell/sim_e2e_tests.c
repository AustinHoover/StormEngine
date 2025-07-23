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
 * Testing pressure values
 */
int fluid_sim_pressurecell_sim_e2e_test1(){
    printf("fluid_sim_pressurecell_sim_e2e_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE - deltaDensity;
    currentChunk->d0[CENTER_LOC][IX(4,4,4)] = deltaDensity;
    currentChunk->u0[CENTER_LOC][IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    currentChunk->v0[CENTER_LOC][IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    currentChunk->w0[CENTER_LOC][IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;

    //actually simulate
    fluid_pressurecell_simulate(chunkCount,queue,env,env->consts.dt);

    return rVal;
}

/**
 * Testing normalizing values
 */
int fluid_sim_pressurecell_sim_e2e_test2(){
    printf("fluid_sim_pressurecell_sim_e2e_test2\n");
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
    int frameCount = 50;
    for(int frame = 0; frame < frameCount; frame++){
        fluid_tracking_reset(env);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_PRESSURECELL);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
    }

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = 0;
    actual = currentChunk->d[CENTER_LOC][IX(1,4,DIM-2)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to recapture density into (1,4,DIM-2)!  expected: %f    actual: %f  \n");
        printf("%f \n", currentChunk->v[CENTER_LOC][IX(1,4,DIM-2)]);
    }

    expected = MAX_FLUID_VALUE;
    actual = currentChunk->d[CENTER_LOC][IX(DIM-2,1,DIM-2)];
    if(fabs(expected - actual) < FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to recapture density into (DIM-2,DIM-2,DIM-2)!  expected: %f    actual: %f  \n");
    }

    //assert that all y=1 chunks are empty (it's just a puddle on the floor, shouldn't be creating fluid in the sky)
    for(int i = 0; i < chunkCount; i++){
        Chunk * toEvaluate = queue[i];
        if(toEvaluate->y < 1){
            continue;
        }
        float sum = chunk_sum_density_with_borders(toEvaluate);
        if(sum > FLUID_PRESSURE_CELL_ERROR_MARGIN){
            rVal++;
            printf("Sky chunk has density! \n");
            printf("sum: %f \n",sum);
            printf("%d,%d,%d \n",toEvaluate->x,toEvaluate->y,toEvaluate->z);
            printf("\n");
        }
    }

    return rVal;
}

/**
 * Testing pressure values
 */
int fluid_sim_pressurecell_sim_e2e_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_pressurecell_sim_e2e_test1();
    // rVal += fluid_sim_pressurecell_sim_e2e_test2();

    return rVal;
}