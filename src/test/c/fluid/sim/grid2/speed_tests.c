#include <math.h>
#include <sys/time.h>

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



#define TARGET_SIM_RADIUS 3.0
#define TARGET_SIM_DIAMETER (TARGET_SIM_RADIUS * 2) + 1

/**
 * Ie probably shouldn't be simulating the sky
 */
#define PERCENT_ACTIVE_IN_RADIUS 0.5
#define CHUNKS_PER_PLAYER ((TARGET_SIM_DIAMETER * TARGET_SIM_DIAMETER * TARGET_SIM_DIAMETER) * PERCENT_ACTIVE_IN_RADIUS)

/**
 * Say 8 players are logged in
 */
#define PLAYERS_PER_SERVER 8.0

//worst case scenario is they're all on different boats trying to reach one another

#define TARGET_CHUNKS (PLAYERS_PER_SERVER * CHUNKS_PER_PLAYER)

/**
 * Number of threads on server to dedicate to fluid sim
 */
#define SIM_THREADS 4.0


/**
 * Allow 8 milli seconds per thread to simulate
 */
#define TOTAL_ALLOWED_TIME_IN_MILLI_SECONDS 8.0

/**
 * Allowed time per chunk
 */
#define TIME_PER_CHUNK (TOTAL_ALLOWED_TIME_IN_MILLI_SECONDS / (TARGET_CHUNKS / SIM_THREADS))

/**
 * Error margin for tests
 */
#define FLUID_GRID2_PROJECTION_ERROR_MARGIN 0.00001f

/**
 * Target number of fluid frames/second
 */
#define TARGET_FPS 1

/**
 * Used for storing timings
 */
struct timeval tv;



/**
 * Testing full sim routine
 */
int fluid_sim_grid2_speed_test1(){
    printf("fluid_sim_grid2_speed_test1\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,TARGET_SIM_DIAMETER,TARGET_SIM_DIAMETER,TARGET_SIM_DIAMETER);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    float beforeSum = chunk_queue_sum_density(queue);

    //actually simulate
    int frameCount = 1;
    double frameTimeAccumulator = 0;
    for(int frame = 0; frame < frameCount; frame++){
        //get time at start
        gettimeofday(&tv,NULL);
        double start = 1000000.0 * tv.tv_sec + tv.tv_usec;

        //sim
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);

        //get time at end
        gettimeofday(&tv,NULL);
        double end = 1000000.0 * tv.tv_sec + tv.tv_usec;
        frameTimeAccumulator = frameTimeAccumulator + (end - start);
    }

    //test the result
    double avgPerChunk = frameTimeAccumulator / (float)frameCount / (float)chunkCount;
    double perMilli = avgPerChunk / 1000.0f;
    if(avgPerChunk > TIME_PER_CHUNK || 1){
        printf("Chunk simulation failing desired speed! (frames: %d,  chunk count: %d) \n", frameCount, chunkCount);
        printf("Accumulator Value:   %lf  \n",frameTimeAccumulator);
        printf("Frame time per chunk (micro): %lf  \n",avgPerChunk);
        printf("Frame time per chunk (milli): %lf  \n",perMilli);
        printf("Target time (milli): %f  \n",TIME_PER_CHUNK);
        printf("Velocity time (milli): %f  \n",env->state.timeTracking.velocityTotal);
        printf(" - Advect (milli): %f  \n",env->state.timeTracking.velocityAdvect);
        printf(" - Diffuse (milli): %f  \n",env->state.timeTracking.velocityDiffuse);
        printf(" - Project (milli): %f  \n",env->state.timeTracking.velocityProject);
        printf("Density time (milli): %f  \n",env->state.timeTracking.densityTotal);
        printf(" - Advect (milli): %f  \n",env->state.timeTracking.densityAdvect);
        printf(" - Diffuse (milli): %f  \n",env->state.timeTracking.densityDiffuse);
        printf(" - Maintenance (milli): %f  \n",env->state.timeTracking.densityMaintenance);
        printf("\n");
        rVal++;
    }

    return rVal;
}

/**
 * Testing full sim routine
 */
int fluid_sim_grid2_speed_test2(){
    printf("fluid_sim_grid2_speed_test2\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,TARGET_SIM_DIAMETER,TARGET_SIM_DIAMETER,TARGET_SIM_DIAMETER);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    currentChunk->u[CENTER_LOC][IX(4,4,4)] = MAX_FLUID_VALUE;
    float beforeSum = chunk_queue_sum_density(queue);

    //actually simulate
    int frameCount = TARGET_FPS;
    double frameTimeAccumulator = 0;
    for(int frame = 0; frame < frameCount; frame++){
        //get time at start
        gettimeofday(&tv,NULL);
        double start = 1000000.0 * tv.tv_sec + tv.tv_usec;

        //sim
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);

        //get time at end
        gettimeofday(&tv,NULL);
        double end = 1000000.0 * tv.tv_sec + tv.tv_usec;
        frameTimeAccumulator = frameTimeAccumulator + (end - start);
    }

    //test the result
    double avgPerChunk = frameTimeAccumulator / (float)frameCount / (float)chunkCount;
    double perMilli = avgPerChunk / 1000.0f;
    if(avgPerChunk > TIME_PER_CHUNK){
        printf("Chunk simulation failing desired speed! (frames: %d,  chunk count: %d) \n", frameCount, chunkCount);
        printf("Accumulator Value:   %lf  \n",frameTimeAccumulator);
        printf("Frame time per chunk (micro): %lf  \n",avgPerChunk);
        printf("Frame time per chunk (milli): %lf  \n",perMilli);
        printf("Target time (milli): %f  \n",TIME_PER_CHUNK);
        printf("Velocity time (milli): %f  \n",env->state.timeTracking.velocityTotal);
        printf(" - Advect (milli): %f  \n",env->state.timeTracking.velocityAdvect);
        printf(" - Diffuse (milli): %f  \n",env->state.timeTracking.velocityDiffuse);
        printf(" - Project (milli): %f  \n",env->state.timeTracking.velocityProject);
        printf("Density time (milli): %f  \n",env->state.timeTracking.densityTotal);
        printf(" - Advect (milli): %f  \n",env->state.timeTracking.densityAdvect);
        printf(" - Diffuse (milli): %f  \n",env->state.timeTracking.densityDiffuse);
        printf(" - Maintenance (milli): %f  \n",env->state.timeTracking.densityMaintenance);
        printf("\n");
        rVal++;
    }

    return rVal;
}

/**
 * Testing full sim routine
 */
int fluid_sim_grid2_speed_test3(){
    printf("fluid_sim_grid2_speed_test3\n");
    int rVal = 0;
    Environment * env = fluid_environment_create();
    Chunk ** queue = NULL;
    queue = createChunkGrid(env,TARGET_SIM_DIAMETER,TARGET_SIM_DIAMETER,TARGET_SIM_DIAMETER);
    int chunkCount = arrlen(queue);



    //setup chunk values
    Chunk * currentChunk = queue[0];
    chunk_fill_real(queue[13]->d[CENTER_LOC],MAX_FLUID_VALUE);
    chunk_fill_real(queue[13]->u[CENTER_LOC],MAX_FLUID_VALUE);
    float beforeSum = chunk_queue_sum_density(queue);

    //actually simulate
    int frameCount = TARGET_FPS;
    double frameTimeAccumulator = 0;
    for(int frame = 0; frame < frameCount; frame++){
        //get time at start
        gettimeofday(&tv,NULL);
        double start = 1000000.0 * tv.tv_sec + tv.tv_usec;

        //sim
        fluid_grid2_simulate(chunkCount,queue,env,FLUID_GRID2_SIM_STEP);

        //get time at end
        gettimeofday(&tv,NULL);
        double end = 1000000.0 * tv.tv_sec + tv.tv_usec;
        frameTimeAccumulator = frameTimeAccumulator + (end - start);
    }

    //test the result
    double avgPerChunk = frameTimeAccumulator / (float)frameCount / (float)chunkCount;
    double perMilli = avgPerChunk / 1000.0f;
    if(avgPerChunk > TIME_PER_CHUNK){
        printf("Chunk simulation failing desired speed! (frames: %d,  chunk count: %d) \n", frameCount, chunkCount);
        printf("Accumulator Value:   %lf  \n",frameTimeAccumulator);
        printf("Frame time per chunk (micro): %lf  \n",avgPerChunk);
        printf("Frame time per chunk (milli): %lf  \n",perMilli);
        printf("Target time (milli): %f  \n",TIME_PER_CHUNK);
        printf("Velocity time (milli): %f  \n",env->state.timeTracking.velocityTotal);
        printf(" - Advect (milli): %f  \n",env->state.timeTracking.velocityAdvect);
        printf(" - Diffuse (milli): %f  \n",env->state.timeTracking.velocityDiffuse);
        printf(" - Project (milli): %f  \n",env->state.timeTracking.velocityProject);
        printf("Density time (milli): %f  \n",env->state.timeTracking.densityTotal);
        printf(" - Advect (milli): %f  \n",env->state.timeTracking.densityAdvect);
        printf(" - Diffuse (milli): %f  \n",env->state.timeTracking.densityDiffuse);
        printf(" - Maintenance (milli): %f  \n",env->state.timeTracking.densityMaintenance);
        printf("\n");
        rVal++;
    }

    return rVal;
}

/**
 * Testing full sim routines
 */
int fluid_sim_grid2_speed_tests(){
    int rVal = 0;

    // rVal += fluid_sim_grid2_speed_test1();
    // rVal += fluid_sim_grid2_speed_test2();
    // rVal += fluid_sim_grid2_speed_test3();

    return rVal;
}