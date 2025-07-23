#include <math.h>

#include "stb/stb_ds.h"

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/pressurecell/pressure.h"
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
 * Testing pressure values
 */
int fluid_sim_pressurecell_pressure_test1(){
    printf("fluid_sim_pressurecell_pressure_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    currentChunk->divergenceCache[CENTER_LOC][IX(4,4,4)] = 1;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = 1;

    //actually simulate
    pressurecell_approximate_pressure(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = currentChunk->divergenceCache[CENTER_LOC][IX(4,4,4)];
    actual = currentChunk->pressureTempCache[IX(4,4,4)] * 6 - (
        currentChunk->pressureTempCache[IX(3,4,4)] +
        currentChunk->pressureTempCache[IX(5,4,4)] +
        currentChunk->pressureTempCache[IX(4,3,4)] +
        currentChunk->pressureTempCache[IX(4,5,4)] +
        currentChunk->pressureTempCache[IX(4,4,3)] +
        currentChunk->pressureTempCache[IX(4,4,5)]
    );
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to advect density correctly (4,4,4)!  expected: %f    actual: %f  \n");
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,4,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(3,4,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(5,4,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,3,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,5,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,4,3)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,4,5)]);
    }


    expected = currentChunk->divergenceCache[CENTER_LOC][IX(4,3,4)];
    actual = currentChunk->pressureTempCache[IX(4,3,4)] * 6 - (
        currentChunk->pressureTempCache[IX(3,3,4)] +
        currentChunk->pressureTempCache[IX(5,3,4)] +
        currentChunk->pressureTempCache[IX(4,2,4)] +
        currentChunk->pressureTempCache[IX(4,4,4)] +
        currentChunk->pressureTempCache[IX(4,3,3)] +
        currentChunk->pressureTempCache[IX(4,3,5)]
    );
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to advect density correctly (4,3,4)!  expected: %f    actual: %f  \n");
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,3,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(3,3,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(5,3,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,2,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,4,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,3,3)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,3,5)]);
    }

    return rVal;
}

/**
 * Testing pressure values
 */
int fluid_sim_pressurecell_pressure_test2(){
    printf("fluid_sim_pressurecell_pressure_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    currentChunk->divergenceCache[CENTER_LOC][IX(4,4,4)] = 1;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = 1;

    //actually simulate
    pressurecell_approximate_pressure(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    int cx, cy, cz;
    cx = 1;
    cy = 1;
    cz = 1;
    expected = currentChunk->divergenceCache[CENTER_LOC][IX(cx,cy,cz)];
    actual = currentChunk->pressureTempCache[IX(cx,cy,cz)] * 6 - (
        currentChunk->pressureTempCache[IX(cx-1,cy,cz)] +
        currentChunk->pressureTempCache[IX(cx+1,cy,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy-1,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy+1,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy,cz-1)] +
        currentChunk->pressureTempCache[IX(cx,cy,cz+1)]
    );
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to approximate pressure correctly!  expected: %f    actual: %f  \n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(cx,cy,cz)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
    }

    return rVal;
}

/**
 * Testing pressure values
 */
int fluid_sim_pressurecell_pressure_test3(){
    printf("fluid_sim_pressurecell_pressure_test3\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    currentChunk->u[CENTER_LOC][IX(1,1,1)] = 1;
    currentChunk->v[CENTER_LOC][IX(1,1,1)] = 1;
    currentChunk->w[CENTER_LOC][IX(1,1,1)] = 1;

    currentChunk->u[CENTER_LOC][IX(2,1,1)] = 1;
    currentChunk->v[CENTER_LOC][IX(1,2,1)] = 1;
    currentChunk->w[CENTER_LOC][IX(1,1,2)] = -1;

    //divergence at 1,1,1 should be 1.5
    pressurecell_approximate_divergence(env,currentChunk);
    //divergence at 2,1,1 should be -0.5



    //actually simulate
    pressurecell_approximate_pressure(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    int cx, cy, cz;
    cx = 1;
    cy = 1;
    cz = 1;
    //essentially this is stating that we expect neighbors of this cell to give out 0.21f more velocity than they currently will this frame
    //ergo, we should subtract that from this cell in order to prevent velocity compressibility
    expected = -0.23;
    actual = currentChunk->pressureTempCache[IX(cx,cy,cz)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to approximate pressure correctly!  expected: %f    actual: %f  \n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("              %f               \n", currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("           +Y |                \n");
        printf("              |    %f          \n", currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
        printf("              |  /   +Z        \n");
        printf("    -X        | /              \n");
        printf("    B---------A--------------%f\n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("            / |            +X  \n");
        printf("           /  |                \n");
        printf("       -Z /   |                \n");
        printf("         C    | -Y             \n");
        printf("             %f                \n", currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("                               \n");
        printf("A: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz)]  );
        printf("B: %f\n",  currentChunk->pressureTempCache[IX(cx-1,cy,cz)] );
        printf("C: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz-1)] );
        printf("Residual:  %f\n", currentChunk->projectionResidual);
        printf("\n\n\n");
    }



    cx = 2;
    cy = 1;
    cz = 1;
    //essentially this is stating that we expect neighbors of this cell to give out -0.5 less velocity than they currently will this frame
    //ergo, we should subtract that from this cell in order to prevent velocity compressibility
    expected = -0.5f;
    actual = currentChunk->pressureTempCache[IX(cx,cy,cz)] * 6 - (
        currentChunk->pressureTempCache[IX(cx-1,cy,cz)] +
        currentChunk->pressureTempCache[IX(cx+1,cy,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy-1,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy+1,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy,cz-1)] +
        currentChunk->pressureTempCache[IX(cx,cy,cz+1)]
    );
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to approximate pressure correctly!  expected: %f    actual: %f  \n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("              %f               \n", currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("           +Y |                \n");
        printf("              |    %f          \n", currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
        printf("              |  /   +Z        \n");
        printf("    -X        | /              \n");
        printf("    B---------A--------------%f\n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("            / |            +X  \n");
        printf("           /  |                \n");
        printf("       -Z /   |                \n");
        printf("         C    | -Y             \n");
        printf("             %f                \n", currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("                               \n");
        printf("A: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz)]  );
        printf("B: %f\n",  currentChunk->pressureTempCache[IX(cx-1,cy,cz)] );
        printf("C: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz-1)] );
        printf("Residual:  %f\n", currentChunk->projectionResidual);
        printf("\n\n\n");
    }

    cx = 3;
    cy = 1;
    cz = 1;
    //essentially this is stating that we expect neighbors of this cell to give out -0.462 more velocity than they currently will this frame
    //ergo, we should subtract that from this cell in order to prevent velocity compressibility
    expected = -0.5;
    actual = currentChunk->pressureTempCache[IX(cx,cy,cz)] * 6 - (
        currentChunk->pressureTempCache[IX(cx-1,cy,cz)] +
        currentChunk->pressureTempCache[IX(cx+1,cy,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy-1,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy+1,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy,cz-1)] +
        currentChunk->pressureTempCache[IX(cx,cy,cz+1)]
    );
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to approximate pressure correctly!  expected: %f    actual: %f  \n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("              %f               \n", currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("           +Y |                \n");
        printf("              |    %f          \n", currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
        printf("              |  /   +Z        \n");
        printf("    -X        | /              \n");
        printf("    B---------A--------------%f\n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("            / |            +X  \n");
        printf("           /  |                \n");
        printf("       -Z /   |                \n");
        printf("         C    | -Y             \n");
        printf("             %f                \n", currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("                               \n");
        printf("A: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz)]  );
        printf("B: %f\n",  currentChunk->pressureTempCache[IX(cx-1,cy,cz)] );
        printf("C: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz-1)] );
        printf("Residual:  %f\n", currentChunk->projectionResidual);
        printf("\n\n\n");
    }

    cx = 4;
    cy = 1;
    cz = 1;
    //essentially this is stating that we expect neighbors of this cell to give out 0.03 more velocity than they currently will this frame
    //ergo, we should subtract that from this cell in order to prevent velocity compressibility
    expected = 0.0;
    actual = currentChunk->pressureTempCache[IX(cx,cy,cz)] * 6 - (
        currentChunk->pressureTempCache[IX(cx-1,cy,cz)] +
        currentChunk->pressureTempCache[IX(cx+1,cy,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy-1,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy+1,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy,cz-1)] +
        currentChunk->pressureTempCache[IX(cx,cy,cz+1)]
    );
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to approximate pressure correctly!  expected: %f    actual: %f  \n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("              %f               \n", currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("           +Y |                \n");
        printf("              |    %f          \n", currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
        printf("              |  /   +Z        \n");
        printf("    -X        | /              \n");
        printf("    B---------A--------------%f\n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("            / |            +X  \n");
        printf("           /  |                \n");
        printf("       -Z /   |                \n");
        printf("         C    | -Y             \n");
        printf("             %f                \n", currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("                               \n");
        printf("A: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz)]  );
        printf("B: %f\n",  currentChunk->pressureTempCache[IX(cx-1,cy,cz)] );
        printf("C: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz-1)] );
        printf("Residual:  %f\n", currentChunk->projectionResidual);
        printf("\n\n\n");
    }

    return rVal;
}

/**
 * Testing pressure values
 */
int fluid_sim_pressurecell_pressure_test4(){
    printf("fluid_sim_pressurecell_pressure_test4\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    currentChunk->u[CENTER_LOC][IX(1,1,1)] = 1;
    currentChunk->v[CENTER_LOC][IX(1,1,1)] = 1;
    currentChunk->u[CENTER_LOC][IX(2,1,1)] = 1;



    //actually simulate
    int frameCount = 5;
    for(int frame = 0; frame < frameCount; frame++){
        //simulate velocity transfering
        currentChunk->u[CENTER_LOC][IX(1,1,1)] = currentChunk->u[CENTER_LOC][IX(1,1,1)] - 0.1f;
        currentChunk->u[CENTER_LOC][IX(2,1,1)] = currentChunk->u[CENTER_LOC][IX(2,1,1)] - 0.1f;
        currentChunk->u[CENTER_LOC][IX(3,1,1)] = currentChunk->u[CENTER_LOC][IX(3,1,1)] + 0.1f;
        pressurecell_approximate_divergence(env,currentChunk);
        pressurecell_approximate_pressure(env,currentChunk);
    }

    //test the result
    float expected, actual;

    //   State of the world (velocity)
    //
    //   ^
    //   |
    //   |
    //   |
    //   |
    //   0.5f  --  0.5f  ---  0.5f  --- >

    //
    // cell that originall had values
    //
    int cx, cy, cz;
    cx = 1;
    cy = 1;
    cz = 1;
    //essentially this is stating that we expect neighbors of this cell to give out 0.02 more velocity than they should this frame
    //ergo, we should subtract that from this cell in order to prevent velocity compressibility
    expected = 0.02;
    actual = currentChunk->pressureTempCache[IX(cx,cy,cz)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to approximate pressure correctly!  expected: %f    actual: %f  \n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("              %f               \n", currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("           +Y |                \n");
        printf("              |    %f          \n", currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
        printf("              |  /   +Z        \n");
        printf("    -X        | /              \n");
        printf("    B---------A--------------%f\n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("            / |            +X  \n");
        printf("           /  |                \n");
        printf("       -Z /   |                \n");
        printf("         C    | -Y             \n");
        printf("             %f                \n", currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("                               \n");
        printf("A: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz)]  );
        printf("B: %f\n",  currentChunk->pressureTempCache[IX(cx-1,cy,cz)] );
        printf("C: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz-1)] );
        printf("Residual:  %f\n", currentChunk->projectionResidual);
        printf("\n\n\n");
    }



    cx = 2;
    cy = 1;
    cz = 1;
    //essentially this is stating that we expect neighbors of this cell to give out 0.02 more velocity than they should this frame
    //ergo, we should subtract that from this cell in order to prevent velocity compressibility
    expected = 0.0;
    actual = currentChunk->pressureTempCache[IX(cx,cy,cz)] * 6 - (
        currentChunk->pressureTempCache[IX(cx-1,cy,cz)] +
        currentChunk->pressureTempCache[IX(cx+1,cy,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy-1,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy+1,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy,cz-1)] +
        currentChunk->pressureTempCache[IX(cx,cy,cz+1)]
    );
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to approximate pressure correctly!  expected: %f    actual: %f  \n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("              %f               \n", currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("           +Y |                \n");
        printf("              |    %f          \n", currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
        printf("              |  /   +Z        \n");
        printf("    -X        | /              \n");
        printf("    B---------A--------------%f\n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("            / |            +X  \n");
        printf("           /  |                \n");
        printf("       -Z /   |                \n");
        printf("         C    | -Y             \n");
        printf("             %f                \n", currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("                               \n");
        printf("A: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz)]  );
        printf("B: %f\n",  currentChunk->pressureTempCache[IX(cx-1,cy,cz)] );
        printf("C: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz-1)] );
        printf("Residual:  %f\n", currentChunk->projectionResidual);
        printf("\n\n\n");
    }

    cx = 3;
    cy = 1;
    cz = 1;
    //essentially this is stating that we expect neighbors of this cell to give out -0.221 less velocity than they should this frame
    //ergo, we should subtract that from this cell in order to prevent velocity compressibility
    expected = -0.25;
    actual = currentChunk->pressureTempCache[IX(cx,cy,cz)] * 6 - (
        currentChunk->pressureTempCache[IX(cx-1,cy,cz)] +
        currentChunk->pressureTempCache[IX(cx+1,cy,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy-1,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy+1,cz)] +
        currentChunk->pressureTempCache[IX(cx,cy,cz-1)] +
        currentChunk->pressureTempCache[IX(cx,cy,cz+1)]
    );
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to approximate pressure correctly!  expected: %f    actual: %f  \n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("              %f               \n", currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("           +Y |                \n");
        printf("              |    %f          \n", currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
        printf("              |  /   +Z        \n");
        printf("    -X        | /              \n");
        printf("    B---------A--------------%f\n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("            / |            +X  \n");
        printf("           /  |                \n");
        printf("       -Z /   |                \n");
        printf("         C    | -Y             \n");
        printf("             %f                \n", currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("                               \n");
        printf("A: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz)]  );
        printf("B: %f\n",  currentChunk->pressureTempCache[IX(cx-1,cy,cz)] );
        printf("C: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz-1)] );
        printf("Residual:  %f\n", currentChunk->projectionResidual);
        printf("\n\n\n");
    }

    return rVal;
}


/**
 * Testing pressure values
 */
int fluid_sim_pressurecell_pressure_test5(){
    printf("fluid_sim_pressurecell_pressure_test5\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    int x, y, z;
    //if everything is pulling away from 1,1,1 with maximum speed, calculate the pressure at 1,1,1
    for(x = 0; x < DIM; x++){
        for(y = 0; y < DIM; y++){
            for(z = 0; z < DIM; z++){
                currentChunk->u[CENTER_LOC][IX(x,y,z)] = 1;
                currentChunk->v[CENTER_LOC][IX(x,y,z)] = 1;
                currentChunk->u[CENTER_LOC][IX(x,y,z)] = 1;
                if(
                    x == 0 || x == DIM-1 ||
                    y == 0 || y == DIM-1 ||
                    z == 0 || z == DIM-1
                ){
                    currentChunk->u[CENTER_LOC][IX(x,y,z)] = 0;
                    currentChunk->v[CENTER_LOC][IX(x,y,z)] = 0;
                    currentChunk->u[CENTER_LOC][IX(x,y,z)] = 0;
                }
            }
        }
    }




    //actually simulate
    pressurecell_approximate_divergence(env,currentChunk);
    pressurecell_approximate_pressure(env,currentChunk);

    //test the result
    float expected, actual;

    //   State of the world (velocity)
    //
    //   ^
    //   |
    //   |
    //   |
    //   |
    //   0.5f  --  0.5f  ---  0.5f  --- >

    //
    // cell that originall had values
    //
    int cx, cy, cz;
    cx = 1;
    cy = 1;
    cz = 1;
    //essentially this is stating that we expect neighbors of this cell to give out 0.02 more velocity than they should this frame
    //ergo, we should subtract that from this cell in order to prevent velocity compressibility
    expected = 0.0;
    actual = currentChunk->pressureTempCache[IX(cx,cy,cz)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Failed to approximate pressure correctly!  expected: %f    actual: %f  \n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("              %f               \n", currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("           +Y |                \n");
        printf("              |    %f          \n", currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
        printf("              |  /   +Z        \n");
        printf("    -X        | /              \n");
        printf("    B---------A--------------%f\n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("            / |            +X  \n");
        printf("           /  |                \n");
        printf("       -Z /   |                \n");
        printf("         C    | -Y             \n");
        printf("             %f                \n", currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("                               \n");
        printf("A: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz)]  );
        printf("B: %f\n",  currentChunk->pressureTempCache[IX(cx-1,cy,cz)] );
        printf("C: %f\n",  currentChunk->pressureTempCache[IX(cx,cy,cz-1)] );
        printf("Residual:  %f\n", currentChunk->projectionResidual);
        printf("\n\n\n");
    }

    return rVal;
}

/**
 * Testing pressure values
 */
int fluid_sim_pressurecell_pressure_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_pressurecell_pressure_test1();
    // rVal += fluid_sim_pressurecell_pressure_test2();
    // rVal += fluid_sim_pressurecell_pressure_test3();
    // rVal += fluid_sim_pressurecell_pressure_test4();
    // rVal += fluid_sim_pressurecell_pressure_test5();

    return rVal;
}