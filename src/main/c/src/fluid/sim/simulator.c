#include <stdlib.h>

#include "stb/stb_ds.h"

#include "fluid/dispatch/dispatcher.h"
#include "fluid/sim/simulator.h"
#include "fluid/sim/grid/simulation.h"
#include "fluid/sim/grid2/grid2.h"
#include "fluid/sim/pressurecell/pressurecell.h"
#include "fluid/sim/cellular/cellular.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"

/**
 * Simulates the various chunk queues in the fluid environment
 * @param environment The environment storing the simulation queues
 */
LIBRARY_API void fluid_simulate(Environment * environment){
    FluidSimQueue queue = environment->queue;

    int currentCount, i;

    //cellular sim
    {
        currentCount = stbds_arrlen(queue.cellularQueue);
        if(currentCount > 0){
            fluid_cellular_simulate(environment);
        }
    }

    //grid sim
    {
        currentCount = stbds_arrlen(queue.gridQueue);
        if(currentCount > 0){
            fluid_grid_simulate(currentCount,queue.gridQueue,environment,environment->consts.dt);
        }
    }

    //grid2 sim
    {
        currentCount = stbds_arrlen(queue.grid2Queue);
        if(currentCount > 0){
            fluid_grid2_simulate(currentCount,queue.grid2Queue,environment,environment->consts.dt);
        }
    }

    //pressurecell sim
    {
        currentCount = stbds_arrlen(queue.pressurecellQueue);
        if(currentCount > 0){
            fluid_pressurecell_simulate(currentCount,queue.pressurecellQueue,environment,environment->consts.dt);
        }
    }
}

