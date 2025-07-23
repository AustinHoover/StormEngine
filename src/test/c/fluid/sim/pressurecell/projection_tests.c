#include <math.h>

#include "stb/stb_ds.h"

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/pressurecell/pressure.h"
#include "fluid/sim/pressurecell/velocity.h"
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
 * Testing projection values
 */
int fluid_sim_pressurecell_projection_test1(){
    printf("fluid_sim_pressurecell_projection_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    currentChunk->divergenceCache[CENTER_LOC][IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = FLUID_PRESSURECELL_MAX_VELOCITY;
    float gridSpacing = FLUID_PRESSURECELL_SPACING * FLUID_PRESSURECELL_SPACING;
    pressurecell_approximate_pressure(env,currentChunk);

    //actually simulate
    pressurecell_project_velocity(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    expected = FLUID_PRESSURECELL_MAX_VELOCITY - (currentChunk->pressureTempCache[IX(5,4,4)] - currentChunk->pressureTempCache[IX(3,4,4)]) / (gridSpacing * 2.0f);
    actual = currentChunk->u[CENTER_LOC][IX(4,4,4)];
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal++;
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,4,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(3,4,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(5,4,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,3,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,5,4)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,4,3)]);
        printf("%f  \n",  currentChunk->pressureTempCache[IX(4,4,5)]);
    }

    return rVal;
}

/**
 * Testing projection mass conservation
 */
int fluid_sim_pressurecell_projection_test2(){
    printf("fluid_sim_pressurecell_projection_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,CHUNK_DIM,CHUNK_DIM,CHUNK_DIM);
    int chunkCount = arrlen(queue);



    //setup chunk values
    float deltaDensity = 0.01f;
    Chunk * currentChunk = queue[0];
    currentChunk->u[CENTER_LOC][IX(2,1,1)] = 1;
    currentChunk->v[CENTER_LOC][IX(1,2,1)] = 1;
    currentChunk->w[CENTER_LOC][IX(1,1,2)] = 1;
    float gridSpacing = FLUID_PRESSURECELL_SPACING * FLUID_PRESSURECELL_SPACING;
    pressurecell_approximate_divergence(env,currentChunk);
    pressurecell_approximate_pressure(env,currentChunk);

    //actually simulate
    pressurecell_project_velocity(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    int cx, cy, cz;
    float u, v, w;
    float sum;
    cx = 2;
    cy = 1;
    cz = 1;
    u = currentChunk->u[CENTER_LOC][IX(cx,cy,cz)];
    v = currentChunk->v[CENTER_LOC][IX(cx,cy,cz)];
    w = currentChunk->w[CENTER_LOC][IX(cx,cy,cz)];
    sum = u + v + w;
    if(u < 0 || v < 0 || w < 0 || sum <= 0.1f){
        rVal++;
        printf("Projection failed\n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("%f %f %f \n", u, v, w);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy,cz,  currentChunk->pressureTempCache[IX(cx,cy,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx-1,cy,cz,  currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx+1,cy,cz,  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy-1,cz,  currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy+1,cz,  currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy,cz-1,  currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy,cz+1,  currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
        printf("pdiv x  %f  \n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)] - currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("pdiv y  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy+1,cz)] - currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("pdiv z  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy,cz+1)] - currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("\n");
    }

    //
    // 
    //
    cx = 1;
    cy = 2;
    cz = 1;
    u = currentChunk->u[CENTER_LOC][IX(cx,cy,cz)];
    v = currentChunk->v[CENTER_LOC][IX(cx,cy,cz)];
    w = currentChunk->w[CENTER_LOC][IX(cx,cy,cz)];
    sum = u + v + w;
    if(u < 0 || v < 0 || w < 0 || sum <= 0.1f){
        rVal++;
        printf("Projection failed\n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("%f %f %f \n", u, v, w);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy,cz,  currentChunk->pressureTempCache[IX(cx,cy,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx-1,cy,cz,  currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx+1,cy,cz,  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy-1,cz,  currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy+1,cz,  currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy,cz-1,  currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy,cz+1,  currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
        printf("pdiv x  %f  \n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)] - currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("pdiv y  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy+1,cz)] - currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("pdiv z  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy,cz+1)] - currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("\n");
    }

    //
    // 
    //
    cx = 1;
    cy = 1;
    cz = 2;
    u = currentChunk->u[CENTER_LOC][IX(cx,cy,cz)];
    v = currentChunk->v[CENTER_LOC][IX(cx,cy,cz)];
    w = currentChunk->w[CENTER_LOC][IX(cx,cy,cz)];
    sum = u + v + w;
    if(u < 0 || v < 0 || w < 0 || sum <= 0.1f){
        rVal++;
        printf("Projection failed\n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("%f %f %f \n", u, v, w);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy,cz,  currentChunk->pressureTempCache[IX(cx,cy,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx-1,cy,cz,  currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx+1,cy,cz,  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy-1,cz,  currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy+1,cz,  currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy,cz-1,  currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy,cz+1,  currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
        printf("pdiv x  %f  \n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)] - currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("pdiv y  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy+1,cz)] - currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("pdiv z  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy,cz+1)] - currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("\n");
    }

    //
    // 
    //
    cx = 1;
    cy = 1;
    cz = 1;
    u = currentChunk->u[CENTER_LOC][IX(cx,cy,cz)];
    v = currentChunk->v[CENTER_LOC][IX(cx,cy,cz)];
    w = currentChunk->w[CENTER_LOC][IX(cx,cy,cz)];
    sum = u + v + w;
    if(u < 0 || v < 0 || w < 0 || sum <= 0.1f){
        rVal++;
        printf("Projection failed\n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("%f %f %f \n", u, v, w);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy,cz,  currentChunk->pressureTempCache[IX(cx,cy,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx-1,cy,cz,  currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx+1,cy,cz,  currentChunk->pressureTempCache[IX(cx+1,cy,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy-1,cz,  currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy+1,cz,  currentChunk->pressureTempCache[IX(cx,cy+1,cz)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy,cz-1,  currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("pressure at[%d,%d,%d]  %f  \n", cx,cy,cz+1,  currentChunk->pressureTempCache[IX(cx,cy,cz+1)]);
        printf("pdiv x  %f  \n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)] - currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("pdiv y  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy+1,cz)] - currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("pdiv z  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy,cz+1)] - currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("\n");
    }

    return rVal;
}


/**
 * Testing projection mass conservation
 */
int fluid_sim_pressurecell_projection_test3(){
    printf("fluid_sim_pressurecell_projection_test3\n");
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
    currentChunk->w[CENTER_LOC][IX(1,1,2)] = 1;
    float gridSpacing = FLUID_PRESSURECELL_SPACING * FLUID_PRESSURECELL_SPACING;
    pressurecell_approximate_divergence(env,currentChunk);
    pressurecell_approximate_pressure(env,currentChunk);

    //actually simulate
    double maxMagnitude = pressurecell_project_velocity(env,currentChunk);

    //test the result
    float expected, actual;

    //
    // cell that originall had values
    //
    int cx, cy, cz;
    float u, v, w;
    float sum;



    cx = 1;
    cy = 1;
    cz = 1;
    u = currentChunk->u[CENTER_LOC][IX(cx,cy,cz)];
    v = currentChunk->v[CENTER_LOC][IX(cx,cy,cz)];
    w = currentChunk->w[CENTER_LOC][IX(cx,cy,cz)];
    expected = 1.0f;
    actual = sqrt(u*u + v*v + w*w);
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal++;
        printf("Projection failed\n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("new force: <%f,%f,%f> \n", u, v, w);
        printf("expected: %f\n", expected);
        printf("actual: %f\n", actual);
        printf("max magnitude: %lf\n",maxMagnitude);
        printf("\n");
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
        printf("pdiv x  %f  \n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)] - currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("pdiv y  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy+1,cz)] - currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("pdiv z  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy,cz+1)] - currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("\n\n\n");
    }




    cx = 2;
    cy = 1;
    cz = 1;
    u = currentChunk->u[CENTER_LOC][IX(cx,cy,cz)];
    v = currentChunk->v[CENTER_LOC][IX(cx,cy,cz)];
    w = currentChunk->w[CENTER_LOC][IX(cx,cy,cz)];
    //essentially, the pressure difference between the previous point and the next point around this one
    //are pulling on this velocity such that it slows down
    expected = 0.596777f;
    actual = sqrt(u*u + v*v + w*w);
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal++;
        printf("Projection failed\n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("new force: <%f,%f,%f> \n", u, v, w);
        printf("expected: %f\n", expected);
        printf("actual: %f\n", actual);
        printf("max magnitude: %lf\n",maxMagnitude);
        printf("\n");
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
        printf("pdiv x  %f  \n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)] - currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("pdiv y  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy+1,cz)] - currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("pdiv z  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy,cz+1)] - currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("\n\n\n");
    }

    //
    // 
    //
    cx = 1;
    cy = 2;
    cz = 1;
    u = currentChunk->u[CENTER_LOC][IX(cx,cy,cz)];
    v = currentChunk->v[CENTER_LOC][IX(cx,cy,cz)];
    w = currentChunk->w[CENTER_LOC][IX(cx,cy,cz)];
    //essentially, the pressure difference between the previous point and the next point around this one
    //are pulling on this velocity such that it slows down
    expected = 0.596777f;
    actual = sqrt(u*u + v*v + w*w);
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal++;
        printf("Projection failed\n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("new force: <%f,%f,%f> \n", u, v, w);
        printf("expected: %f\n", expected);
        printf("actual: %f\n", actual);
        printf("max magnitude: %lf\n",maxMagnitude);
        printf("\n");
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
        printf("pdiv x  %f  \n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)] - currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("pdiv y  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy+1,cz)] - currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("pdiv z  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy,cz+1)] - currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("\n\n\n");
    }

    //
    // 
    //
    cx = 1;
    cy = 1;
    cz = 2;
    u = currentChunk->u[CENTER_LOC][IX(cx,cy,cz)];
    v = currentChunk->v[CENTER_LOC][IX(cx,cy,cz)];
    w = currentChunk->w[CENTER_LOC][IX(cx,cy,cz)];
    //essentially, the pressure difference between the previous point and the next point around this one
    //are pulling on this velocity such that it slows down
    expected = 0.596777f;
    actual = sqrt(u*u + v*v + w*w);
    if(fabs(expected - actual) > FLUID_PRESSURE_CELL_ERROR_MARGIN){
        rVal++;
        printf("Projection failed\n");
        printf("at point (%d,%d,%d) \n", cx, cy, cz);
        printf("new force: <%f,%f,%f> \n", u, v, w);
        printf("expected: %f\n", expected);
        printf("actual: %f\n", actual);
        printf("max magnitude: %lf\n",maxMagnitude);
        printf("\n");
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
        printf("pdiv x  %f  \n",  currentChunk->pressureTempCache[IX(cx+1,cy,cz)] - currentChunk->pressureTempCache[IX(cx-1,cy,cz)]);
        printf("pdiv y  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy+1,cz)] - currentChunk->pressureTempCache[IX(cx,cy-1,cz)]);
        printf("pdiv z  %f  \n",  currentChunk->pressureTempCache[IX(cx,cy,cz+1)] - currentChunk->pressureTempCache[IX(cx,cy,cz-1)]);
        printf("\n\n\n");
    }

    return rVal;
}

/**
 * Testing projection values
 */
int fluid_sim_pressurecell_projection_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_pressurecell_projection_test1();
    // rVal += fluid_sim_pressurecell_projection_test2();
    // rVal += fluid_sim_pressurecell_projection_test3();

    return rVal;
}