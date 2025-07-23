#include <stdlib.h>

#include "stb/stb_ds.h"

#include "fluid/dispatch/dispatcher.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"

/**
 * Dispatches chunks to different simulation queues based on the chunk's properties
 * @param numReadIn The number of chunks
 * @param chunkViewC The array of chunks
 * @param environment The environment storing the simulation queues
 * @param override Overrides the queueing system to force all chunks into a given queue (ie for testing)
 */
LIBRARY_API void fluid_dispatch(int numReadIn, Chunk ** chunkViewC, Environment * environment, int override){
    //clear queues
    int countCurrent;
    countCurrent = arrlen(environment->queue.cellularQueue);
    if(countCurrent > 0){
        arrdeln(environment->queue.cellularQueue,0,countCurrent);
    }
    countCurrent = arrlen(environment->queue.gridQueue);
    if(countCurrent > 0){
        arrdeln(environment->queue.gridQueue,0,countCurrent);
    }
    countCurrent = arrlen(environment->queue.grid2Queue);
    if(countCurrent > 0){
        arrdeln(environment->queue.grid2Queue,0,countCurrent);
    }
    countCurrent = arrlen(environment->queue.pressurecellQueue);
    if(countCurrent > 0){
        arrdeln(environment->queue.pressurecellQueue,0,countCurrent);
    }


    if(override == FLUID_DISPATCHER_OVERRIDE_CELLULAR){
        //queue new chunks
        for(int i = 0; i < numReadIn; i++){
            Chunk * currentChunk = chunkViewC[i];
            //TODO: conditionally add to queues based on some values (ie lod, spatial loc, etc)
            arrput(environment->queue.cellularQueue,currentChunk);
        }
        return;
    } else if(override == FLUID_DISPATCHER_OVERRIDE_GRID2){
        //queue new chunks
        for(int i = 0; i < numReadIn; i++){
            Chunk * currentChunk = chunkViewC[i];
            //TODO: conditionally add to queues based on some values (ie lod, spatial loc, etc)
            arrput(environment->queue.grid2Queue,currentChunk);
        }
        return;
    } else if(override == FLUID_DISPATCHER_OVERRIDE_PRESSURECELL){
        //queue new chunks
        for(int i = 0; i < numReadIn; i++){
            Chunk * currentChunk = chunkViewC[i];
            //TODO: conditionally add to queues based on some values (ie lod, spatial loc, etc)
            arrput(environment->queue.pressurecellQueue,currentChunk);
        }
        return;
    }



    //queue new chunks
    for(int i = 0; i < numReadIn; i++){
        Chunk * currentChunk = chunkViewC[i];
        //TODO: conditionally add to queues based on some values (ie lod, spatial loc, etc)
        arrput(environment->queue.pressurecellQueue,currentChunk);
    }
    
}

