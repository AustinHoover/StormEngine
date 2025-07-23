#include <stdint.h>
#include <stdlib.h>
#include <immintrin.h>
#include <sys/time.h>

//native interfaces
#include "native/electrosphere_server_physics_fluid_simulator_FluidAcceleratedSimulator.h"

//fluid lib
#include "fluid/env/utilities.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/sim/grid2/flux.h"
#include "fluid/sim/grid2/grid2.h"
#include "fluid/sim/grid2/solver_consts.h"
#include "fluid/sim/grid2/velocity.h"
#include "fluid/sim/grid2/density.h"
#include "fluid/sim/grid2/utilities.h"

#ifndef SAVE_STEPS
#define SAVE_STEPS 0
#endif

static inline void fluid_grid2_saveStep(float * values, const char * name);
static inline void fluid_grid2_applyGravity(Chunk * currentChunk, Environment * environment);
static inline void fluid_grid2_clearArr(float ** d);
static inline void fluid_grid2_rewrite_bounds(Environment * environment, Chunk * chunk);

/**
 * Used for storing timings
 */
static struct timeval tv;

LIBRARY_API void fluid_grid2_simulate(
    int numChunks,
    Chunk ** passedInChunks,
    Environment * environment,
    jfloat timestep
){
    Chunk ** chunks = passedInChunks;
    double start, end, perMilli;

    //
    //This is the section of non-parallel code
    //

    //update ODE solver data
    environment->state.grid2.diffuseData.dt = timestep;
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        fluid_grid2_update_ghost_flux(environment,currentChunk);
    }




    //
    //Everything below here should be emberassingly parallel
    //
    gettimeofday(&tv,NULL);
    start = 1000000.0 * tv.tv_sec + tv.tv_usec;
    //maintenance
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //update the bounds arrays
        fluid_grid2_rewrite_bounds(environment,currentChunk);



        //add velocity
        fluid_grid2_applyGravity(currentChunk,environment);
        fluid_grid2_addSourceToVectors(
            environment,
            currentChunk->u,
            currentChunk->v,
            currentChunk->w,
            currentChunk->u0,
            currentChunk->v0,
            currentChunk->w0,
            timestep
        );

        //swap all vector fields
        fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
        fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
        fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);
    }

    gettimeofday(&tv,NULL);
    start = 1000000.0 * tv.tv_sec + tv.tv_usec;

    //diffuse velocity
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //update the bounds arrays
        fluid_grid2_rewrite_bounds(environment,currentChunk);

        //solve vector diffusion
        fluid_grid2_solveVectorDiffuse(
            environment,
            currentChunk->u,
            currentChunk->v,
            currentChunk->w,
            currentChunk->u0,
            currentChunk->v0,
            currentChunk->w0,
            timestep
        );
        
    }

    //time tracking
    gettimeofday(&tv,NULL);
    end = 1000000.0 * tv.tv_sec + tv.tv_usec;
    perMilli = (end - start) / 1000.0f;
    environment->state.timeTracking.velocityDiffuse = perMilli;
    start = end;


    //project
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //update the bounds arrays
        fluid_grid2_rewrite_bounds(environment,currentChunk);

        // setup projection
        fluid_grid2_setupProjection(
            environment,
            currentChunk,
            currentChunk->u,
            currentChunk->v,
            currentChunk->w,
            currentChunk->u0,
            currentChunk->v0,
            timestep
        );

        //
        //Perform main projection solver
        fluid_grid2_solveProjection(environment,currentChunk,currentChunk->u0,currentChunk->v0,timestep);

        //Finalize projection
        fluid_grid2_finalizeProjection(environment,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,timestep);

        //swap all vector fields
        fluid_grid2_flip_arrays(currentChunk->u,currentChunk->u0);
        fluid_grid2_flip_arrays(currentChunk->v,currentChunk->v0);
        fluid_grid2_flip_arrays(currentChunk->w,currentChunk->w0);

    }

    //time tracking
    gettimeofday(&tv,NULL);
    end = 1000000.0 * tv.tv_sec + tv.tv_usec;
    perMilli = (end - start) / 1000.0f;
    environment->state.timeTracking.velocityProject = perMilli;
    start = end;

    //advect
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //update the bounds arrays
        fluid_grid2_rewrite_bounds(environment,currentChunk);
    
        // advect
        fluid_grid2_advectVectors(environment,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,timestep);

    }

    //time tracking
    gettimeofday(&tv,NULL);
    end = 1000000.0 * tv.tv_sec + tv.tv_usec;
    perMilli = (end - start) / 1000.0f;
    environment->state.timeTracking.velocityAdvect = perMilli;
    start = end;

    //project again
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //update the bounds arrays
        fluid_grid2_rewrite_bounds(environment,currentChunk);

        //setup projection
        fluid_grid2_setupProjection(environment,currentChunk,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,timestep);
        //Perform main projection solver
        fluid_grid2_solveProjection(environment,currentChunk,currentChunk->u0,currentChunk->v0,timestep);
        //Finalize projection
        fluid_grid2_finalizeProjection(environment,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,timestep);
    }


    //time tracking
    gettimeofday(&tv,NULL);
    end = 1000000.0 * tv.tv_sec + tv.tv_usec;
    perMilli = (end - start) / 1000.0f;
    environment->state.timeTracking.velocityProject = environment->state.timeTracking.velocityProject + perMilli;
    start = end;

    ///------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    ///------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    ///------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    ///------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    ///------------------------------------------------------------------------------------------------------------------------------------------------------------------------




    //
    //Density step
    //
    double deltaDensity = 0;
    environment->state.existingDensity = 0;
    environment->state.newDensity = 0;
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //add density
        fluid_grid2_addDensity(environment,currentChunk->d,currentChunk->d0,timestep);
        environment->state.existingDensity = environment->state.existingDensity + fluid_grid2_calculateSum(currentChunk->d);
        //swap all density arrays
        fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
    }

    //time tracking
    gettimeofday(&tv,NULL);
    end = 1000000.0 * tv.tv_sec + tv.tv_usec;
    perMilli = (end - start) / 1000.0f;
    environment->state.timeTracking.densityMaintenance = perMilli;
    start = end;


    //solve density diffusion
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //update the bounds arrays
        // fluid_grid2_rewrite_bounds(environment, currentChunk); //33% more time than just diffusion step
        //diffuse density
        fluid_grid2_solveDiffuseDensity(environment,currentChunk->d,currentChunk->d0,timestep);
    }

    //time tracking
    gettimeofday(&tv,NULL);
    end = 1000000.0 * tv.tv_sec + tv.tv_usec;
    perMilli = (end - start) / 1000.0f;
    environment->state.timeTracking.densityDiffuse = perMilli;
    start = end;


    //flip arrays
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //swap all density arrays
        fluid_grid2_flip_arrays(currentChunk->d,currentChunk->d0);
    }

    //time tracking
    gettimeofday(&tv,NULL);
    end = 1000000.0 * tv.tv_sec + tv.tv_usec;
    perMilli = (end - start) / 1000.0f;
    environment->state.timeTracking.densityMaintenance = environment->state.timeTracking.densityMaintenance + perMilli;
    start = end;

    
    //advect
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        //update the bounds arrays
        fluid_grid2_rewrite_bounds(environment, currentChunk);


        //advect density
        fluid_grid2_advectDensity(environment,currentChunk->d,currentChunk->d0,currentChunk->u,currentChunk->v,currentChunk->w,timestep);
    }

    //get time at end
    gettimeofday(&tv,NULL);
    end = 1000000.0 * tv.tv_sec + tv.tv_usec;
    perMilli = (end - start) / 1000.0f;
    environment->state.timeTracking.densityAdvect = perMilli;


    //summarize time tracking
    environment->state.timeTracking.densityTotal = environment->state.timeTracking.densityAdvect + environment->state.timeTracking.densityDiffuse;
    environment->state.timeTracking.velocityTotal = 
        environment->state.timeTracking.velocityDiffuse +
        environment->state.timeTracking.velocityAdvect +
        environment->state.timeTracking.velocityProject
    ;


    //
    //mirror densities
    //
    {
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            //update the bounds arrays
            fluid_grid2_rewrite_bounds(environment, currentChunk);
            fluid_grid2_set_bounds(environment,BOUND_SET_DENSITY,currentChunk->d[CENTER_LOC]);
        }
    }


    //
    //normalize densities
    //
    {
        double transformedDensity = 0;
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            transformedDensity = transformedDensity + fluid_grid2_sum_for_normalization(environment,currentChunk);
        }
        double normalizationRatio = 0;
        if(transformedDensity != 0){
            double expectedNewSum = environment->state.existingDensity + environment->state.newDensity;
            normalizationRatio = expectedNewSum / transformedDensity;
            environment->state.normalizationRatio = normalizationRatio;
        }
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            fluid_grid2_normalizeDensity(environment,currentChunk->d,normalizationRatio);
        }
    }

    //
    //clear delta arrays
    //
    {
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            fluid_grid2_clearArr(currentChunk->d0);
            fluid_grid2_clearArr(currentChunk->u0);
            fluid_grid2_clearArr(currentChunk->v0);
            fluid_grid2_clearArr(currentChunk->w0);
        }
    }
}















/**
 * Saves a step of the simulation to a file
 */
static inline void fluid_grid2_saveStep(float * values, const char * name){
    if(SAVE_STEPS){
        FILE *fp;
        int N = DIM;

        // ... fill the array somehow ...

        fp = fopen(name, "w");
        // check for error here

        for(int x = 0; x < DIM; x++){
            for(int y = 0; y < DIM; y++){
                for(int z = 0; z < DIM; z++){
                    float val = values[IX(x,y,z)];
                    if(val < FLUID_GRID2_REALLY_SMALL_VALUE && val > -FLUID_GRID2_REALLY_SMALL_VALUE){
                        val = 0;
                    }
                    fprintf(fp, "%f\t", val);
                }
                fprintf(fp, "\n");
            }
            fprintf(fp, "\n");
        }

        fclose(fp);
    }
}

/**
 * Applies gravity to the chunk
 * @param currentChunk The chunk to apply on
 * @param environment The environment data of the world
 */
static inline void fluid_grid2_applyGravity(Chunk * currentChunk, Environment * environment){
    for(int x = 1; x < DIM-1; x++){
        for(int y = 1; y < DIM-1; y++){
            for(int z = 1; z < DIM-1; z++){
                GET_ARR_RAW(currentChunk->v0,CENTER_LOC)[IX(x,y,z)] = GET_ARR_RAW(currentChunk->v0,CENTER_LOC)[IX(x,y,z)] + environment->consts.gravity;
            }
        }
    }
}

/**
 * Clears an array
 */
static inline void fluid_grid2_clearArr(float ** d){
    float * x = GET_ARR_RAW(d,CENTER_LOC);
    for(int j = 0; j < DIM * DIM * DIM; j++){
        x[j] = 0;
    }
}



/**
 * Quickly masks a chunk's arrays
 */
static inline void fluid_grid2_populate_masked_arr(Environment * environment, float * sourceArr, float * workingArr){
    __m256 arrVal, maskVal, masked;
    int x;
    for(int z = 0; z < 18; z++){
        for(int y = 0; y < 18; y++){
            //lower part
            x = 0;
            arrVal = _mm256_loadu_ps(&sourceArr[IX(x,y,z)]);
            maskVal = _mm256_loadu_ps(&environment->state.grid2.fluid_grid2_border_mask[IX(x,y,z)]);
            masked = _mm256_mul_ps(arrVal,maskVal);
            _mm256_storeu_ps(&workingArr[IX(x,y,z)],masked);

            //middle part
            x = 8;
            arrVal = _mm256_loadu_ps(&sourceArr[IX(x,y,z)]);
            maskVal = _mm256_loadu_ps(&environment->state.grid2.fluid_grid2_border_mask[IX(x,y,z)]);
            masked = _mm256_mul_ps(arrVal,maskVal);
            _mm256_storeu_ps(&workingArr[IX(x,y,z)],masked);

            //upper part
            x = 10;
            arrVal = _mm256_loadu_ps(&sourceArr[IX(x,y,z)]);
            maskVal = _mm256_loadu_ps(&environment->state.grid2.fluid_grid2_border_mask[IX(x,y,z)]);
            masked = _mm256_mul_ps(arrVal,maskVal);
            _mm256_storeu_ps(&workingArr[IX(x,y,z)],masked);
        }
    }
}

/**
 * Rewrites the bounds arrays to contain the bounds of the current chunk
 */
static inline void fluid_grid2_rewrite_bounds(Environment * environment, Chunk * chunk){
    fluid_grid2_populate_masked_arr(environment,chunk->d[CENTER_LOC], environment->state.grid2.fluid_grid2_neighborArr_d);
    fluid_grid2_populate_masked_arr(environment,chunk->d0[CENTER_LOC], environment->state.grid2.fluid_grid2_neighborArr_d0);
    fluid_grid2_populate_masked_arr(environment,chunk->u[CENTER_LOC], environment->state.grid2.fluid_grid2_neighborArr_u);
    fluid_grid2_populate_masked_arr(environment,chunk->v[CENTER_LOC], environment->state.grid2.fluid_grid2_neighborArr_v);
    fluid_grid2_populate_masked_arr(environment,chunk->w[CENTER_LOC], environment->state.grid2.fluid_grid2_neighborArr_w);
    fluid_grid2_populate_masked_arr(environment,chunk->u0[CENTER_LOC], environment->state.grid2.fluid_grid2_neighborArr_u0);
    fluid_grid2_populate_masked_arr(environment,chunk->v0[CENTER_LOC], environment->state.grid2.fluid_grid2_neighborArr_v0);
    fluid_grid2_populate_masked_arr(environment,chunk->w0[CENTER_LOC], environment->state.grid2.fluid_grid2_neighborArr_w0);
    fluid_grid2_populate_masked_arr(environment,chunk->bounds[CENTER_LOC], environment->state.grid2.fluid_grid2_neighborArr_bounds);
    fluid_grid2_populate_masked_arr(environment,chunk->pressureCache[CENTER_LOC], environment->state.grid2.fluid_grid2_neighborArr_scalarCache);
    fluid_grid2_populate_masked_arr(environment,chunk->divergenceCache[CENTER_LOC], environment->state.grid2.fluid_grid2_neighborArr_divergenceCache);
}















