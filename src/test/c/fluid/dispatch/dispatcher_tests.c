
#include "stb/stb_ds.h"

#include "fluid/dispatch/dispatcher.h"
#include "fluid/env/environment.h"

#include "../../util/chunk_test_utils.h"
#include "../../util/test.h"

int fluid_dispatch_dispatcher_tests(){
    int rVal = 0;

    Environment * env = fluid_environment_create();

    int queueSize = 10;
    Chunk ** queue = chunk_create_queue(queueSize);
    fluid_dispatch(queueSize,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);

    int gridChunksFound = stbds_arrlen(env->queue.gridQueue) +
        stbds_arrlen(env->queue.cellularQueue)
    ;
    rVal += assertEquals(gridChunksFound,queueSize,"should have 10 queued chunks -- %d %d \n");

    return rVal;
}