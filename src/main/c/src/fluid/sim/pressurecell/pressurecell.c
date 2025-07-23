#include <stdint.h>
#include <stdlib.h>
#include <immintrin.h>
#include <sys/time.h>

#include "fluid/sim/pressurecell/pressurecell.h"

//fluid lib
#include "fluid/env/utilities.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/sim/pressurecell/bounds.h"
#include "fluid/sim/pressurecell/density.h"
#include "fluid/sim/pressurecell/pressure.h"
#include "fluid/sim/pressurecell/solver_consts.h"
#include "fluid/sim/pressurecell/normalization.h"
#include "fluid/sim/pressurecell/tracking.h"
#include "fluid/sim/pressurecell/velocity.h"




static inline void fluid_pressurecell_clearArr(float * d);


/**
 * Used for storing timings
 */
static struct timeval tv;

LIBRARY_API void fluid_pressurecell_simulate(
    int numChunks,
    Chunk ** passedInChunks,
    Environment * environment,
    jfloat timestep
){
    Chunk ** chunks = passedInChunks;

    //
    //This is the section of non-parallel code
    //

    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        fluid_pressurecell_clearArr(currentChunk->pressureTempCache);
        fluid_pressurecell_clearArr(currentChunk->dTempCache);
        fluid_pressurecell_clearArr(currentChunk->uTempCache);
        fluid_pressurecell_clearArr(currentChunk->vTempCache);
        fluid_pressurecell_clearArr(currentChunk->wTempCache);
        fluid_pressurecell_calculate_expected_intake(environment,currentChunk);
        // fluid_pressurecell_update_bounds(environment,currentChunk);
        // pressurecell_update_interest(environment,currentChunk);
    }





    //
    //This is the section of parallel code
    //

    //
    // Velocity phase
    //
    
    //approximate pressure first using the values from last frame
    //this allows us to guarantee that we're using the divergence from neighbors (eventually)
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        pressurecell_approximate_pressure(environment,currentChunk);
    }
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //u->u
        pressurecell_project_velocity(environment,currentChunk);
    }

    // printf("grav\n");
    // fflush(stdout);
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //u0->u0
        pressurecell_add_gravity(environment,currentChunk);
    }

    // printf("+vel\n");
    // fflush(stdout);
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //u+u0->uTemp
        pressurecell_add_velocity(environment,currentChunk);
    }

    // printf("diff vel\n");
    // fflush(stdout);
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //uTemp->u0
        pressurecell_diffuse_velocity(environment,currentChunk);
    }

    // printf("adv vel\n");
    // fflush(stdout);
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //u0->uTemp
        pressurecell_advect_velocity(environment,currentChunk);
    }

    // printf("approx div\n");
    // fflush(stdout);
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //uTemp->div
        pressurecell_approximate_divergence(environment,currentChunk);
    }




    //
    // Density phase
    //
    // printf("+dens\n");
    // fflush(stdout);
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        fluid_pressurecell_add_density(environment,currentChunk);
    }

    // printf("diff dens\n");
    // fflush(stdout);
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        fluid_pressurecell_diffuse_density(environment,currentChunk);
    }

    // printf("adv dens\n");
    // fflush(stdout);
    for(int i = 0; i < numChunks; i++){
        //uTemp->d
        Chunk * currentChunk = chunks[i];
        fluid_pressurecell_advect_density(environment,currentChunk);
        if(FLUID_PRESSURECELL_ENABLE_RECAPTURE){
            //d->dTemp->d
            fluid_pressurecell_recapture_density(environment,currentChunk);
        }
    }


    //
    // Normalization Phase
    //
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //d->d
        fluid_pressurecell_normalize_chunk(environment,currentChunk);
    }


    //
    // Update tracking
    //
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        pressurecell_update_tracking(environment,currentChunk);
    }


    //
    // Setup for next iteration
    //
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //uTemp->u
        pressurecell_copy_for_next_frame(environment,currentChunk);
        fluid_pressurecell_clearArr(currentChunk->d0[CENTER_LOC]);
        fluid_pressurecell_clearArr(currentChunk->u0[CENTER_LOC]);
        fluid_pressurecell_clearArr(currentChunk->v0[CENTER_LOC]);
        fluid_pressurecell_clearArr(currentChunk->w0[CENTER_LOC]);
    }

}






/**
 * Clears an array
 */
static inline void fluid_pressurecell_clearArr(float * d){
    for(int j = 0; j < DIM * DIM * DIM; j++){
        d[j] = 0;
    }
}



