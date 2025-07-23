#include <stdio.h>
#include <stdlib.h>

#include "fluid/queue/chunk.h"
#include "fluid/queue/sparse.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/utilities.h"
#include "fluid/queue/islandsolver.h"
#include "fluid/dispatch/dispatcher.h"
#include "fluid/sim/simulator.h"
#include "../../util/test.h"
#include "../../util/chunk_test_utils.h"



int fluid_sim_simulator_tests(int argc, char **argv){
    int rVal = 0;
    
    Environment * env = fluid_environment_create();

    int queueSize = 10;
    Chunk ** queue = chunk_create_queue(queueSize);
    fluid_dispatch(queueSize,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);

    fluid_simulate(env);

    return rVal;
}
