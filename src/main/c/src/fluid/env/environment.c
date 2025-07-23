#include <stdlib.h>

#include "public.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/pressurecell/solver_consts.h"




void fluid_environment_allocate_arrays(Environment * environment);

/**
 * Creates an environment
 */
LIBRARY_API Environment * fluid_environment_create(){
    Environment * rVal = (Environment *)calloc(1,sizeof(Environment));
    rVal->queue.cellularQueue = NULL;
    rVal->queue.gridQueue = NULL;
    rVal->queue.grid2Queue = NULL;
    rVal->consts.dt = FLUID_PRESSURECELL_SIM_STEP;
    rVal->consts.gravity = FLUID_PRESSURECELL_GRAVITY;

    //allocate arrays
    fluid_environment_allocate_arrays(rVal);

    return rVal;
}

/**
 * Frees an environment
 */
LIBRARY_API void fluid_environment_free(Environment * environment){
    free(environment);
}



/**
 * Allocates the arrays necessary for environment simulation
 */
void fluid_environment_allocate_arrays(Environment * environment){
    environment->state.grid2.fluid_grid2_border_mask = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    environment->state.grid2.fluid_grid2_border_mask_inverted = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    for(int x = 0; x < DIM; x++){
        for(int y = 0; y < DIM; y++){
            for(int z = 0; z < DIM; z++){
                if(x == 0 || x == DIM-1 || y == 0 || y == DIM-1 || z == 0 || z == DIM-1){
                    environment->state.grid2.fluid_grid2_border_mask[IX(x,y,z)] = 1;
                    environment->state.grid2.fluid_grid2_border_mask_inverted[IX(x,y,z)] = 0;
                } else {
                    environment->state.grid2.fluid_grid2_border_mask[IX(x,y,z)] = 0;
                    environment->state.grid2.fluid_grid2_border_mask_inverted[IX(x,y,z)] = 1;
                }
            }
        }
    }
    environment->state.grid2.fluid_grid2_neighborArr_d = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    environment->state.grid2.fluid_grid2_neighborArr_d0 = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    environment->state.grid2.fluid_grid2_neighborArr_u = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    environment->state.grid2.fluid_grid2_neighborArr_v = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    environment->state.grid2.fluid_grid2_neighborArr_w = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    environment->state.grid2.fluid_grid2_neighborArr_u0 = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    environment->state.grid2.fluid_grid2_neighborArr_v0 = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    environment->state.grid2.fluid_grid2_neighborArr_w0 = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    environment->state.grid2.fluid_grid2_neighborArr_bounds = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    environment->state.grid2.fluid_grid2_neighborArr_divergenceCache = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    environment->state.grid2.fluid_grid2_neighborArr_scalarCache = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
}