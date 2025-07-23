#include <math.h>

#include "stb/stb_ds.h"

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/pressurecell/normalization.h"
#include "fluid/sim/pressurecell/solver_consts.h"
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
int fluid_sim_pressurecell_normalization_test1(){
    printf("fluid_sim_pressurecell_normalization_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,1,1,1);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    currentChunk->vTempCache[IX(1,1,1)] = -0.8;
    currentChunk->d[CENTER_LOC][IX(1,1,1)] = 0.2;

    currentChunk->vTempCache[IX(DIM-2,DIM-2,DIM-2)] = 0.8;
    currentChunk->d[CENTER_LOC][IX(DIM-2,DIM-2,DIM-2)] = 0.2;

    //actually simulate
    fluid_pressurecell_recapture_density(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = MAX_FLUID_VALUE;
    actual = currentChunk->dTempCache[IX(1,1,1)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to recapture density into (1,1,1)!  expected: %f    actual: %f  \n");
    }

    expected = MAX_FLUID_VALUE;
    actual = currentChunk->dTempCache[IX(DIM-2,DIM-2,DIM-2)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to recapture density into (DIM-2,DIM-2,DIM-2)!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

/**
 * Testing normalization
 */
int fluid_sim_pressurecell_normalization_tests(int argc, char **argv){
    int rVal = 0;

    // rVal += fluid_sim_pressurecell_normalization_test1();

    return rVal;
}

