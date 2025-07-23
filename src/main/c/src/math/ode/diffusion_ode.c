
#include "math/ode/diffusion_ode.h"

/**
 * Computes the residual of a given position in a diffusion ode
 */
float ode_diffusion_cg_stencil(float * phi, int x, int y, int z, OdeData * data){
    OdeDiffuseData * diffuseData = (OdeDiffuseData *)data;
    float a = diffuseData->dt*FLUID_GRID2_VISCOSITY_CONSTANT/(FLUID_GRID2_H*FLUID_GRID2_H);
    float c = 1+6*a;
    return
        6 * phi[IX(x,y,z)] -
        1 * (
            phi[IX(x+1,y,z)] + phi[IX(x-1,y,z)] + 
            phi[IX(x,y+1,z)] + phi[IX(x,y-1,z)] + 
            phi[IX(x,y,z+1)] + phi[IX(x,y,z-1)]
        )
    ;
}

