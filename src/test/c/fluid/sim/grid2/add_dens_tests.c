#include <math.h>

#include "stb/stb_ds.h"

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/grid2/density.h"
#include "fluid/sim/grid2/grid2.h"
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
 * Testing full sim routine
 */
int fluid_sim_grid2_add_dens_test1(){
    printf("fluid_sim_grid2_add_dens_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,3,3,3);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    float beforeSum = chunk_queue_sum_density(queue);

    //actually simulate
    int frameCount = 2;
    int additionFrameCutoff = 25;
    for(int frame = 0; frame < frameCount; frame++){
        if(frame < additionFrameCutoff){
            queue[0]->d0[CENTER_LOC][IX(5,5,5)] = MAX_FLUID_VALUE;
        }
        fluid_grid2_simulate(3*3*3,queue,env,FLUID_GRID2_SIM_STEP);
        printf("Existing sum: %lf\n", env->state.existingDensity);
        printf("New density: %lf\n", env->state.newDensity);
        printf("Adjustment Ratio: %f\n", env->state.normalizationRatio);
        float afterSum = chunk_queue_sum_density(queue);
        printf("AFter transform sum: %f\n",afterSum);
        printf("\n");
    }

    //test the result
    float afterSum = chunk_queue_sum_density(queue);
    float expectedSum = additionFrameCutoff * MAX_FLUID_VALUE * FLUID_GRID2_SIM_STEP;
    if(fabs(expectedSum - afterSum) > FLUID_GRID2_PROJECTION_ERROR_MARGIN){
        rVal += assertEqualsFloat(expectedSum,afterSum,"Simulation did not properly add density! expected: %f     actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing full sim routines
 */
int fluid_sim_grid2_add_dens_tests(int argc, char **argv){
    int rVal = 0;

    // rVal += fluid_sim_grid2_add_dens_test1();

    return rVal;
}