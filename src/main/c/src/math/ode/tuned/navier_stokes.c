#include <math.h>

#include "math/ode/tuned/navier_stokes.h"
#include "math/ode/gauss_seidel.h"




/**
 * Calculates the residual for the approximation
 */
LIBRARY_API float solver_navier_stokes_get_residual(float * phi, float * phi0, int x, int y, int z, int GRIDDIM){
    float c = 6.0f;
    float laplacian = 
    - (
        phi[solver_gauss_seidel_get_index(x+1,y,z,GRIDDIM)] +
        phi[solver_gauss_seidel_get_index(x-1,y,z,GRIDDIM)] +
        phi[solver_gauss_seidel_get_index(x,y+1,z,GRIDDIM)] +
        phi[solver_gauss_seidel_get_index(x,y-1,z,GRIDDIM)] +
        phi[solver_gauss_seidel_get_index(x,y,z+1,GRIDDIM)] +
        phi[solver_gauss_seidel_get_index(x,y,z-1,GRIDDIM)]
    ) +
    phi[solver_gauss_seidel_get_index(x,y,z,GRIDDIM)] * c;
    float div = phi0[solver_gauss_seidel_get_index(x,y,z,GRIDDIM)];
    return div - laplacian;
}

/**
 * Calculates the residual for the approximation
 */
LIBRARY_API void solver_navier_stokes_approximate(float * phi, float * phi0, int x, int y, int z, int GRIDDIM){
    float a = 1.0f;
    float c = 6.0f;
    phi[solver_gauss_seidel_get_index(x,y,z,GRIDDIM)] =
    (
        phi0[solver_gauss_seidel_get_index(x,y,z,GRIDDIM)] +
        a * (
            phi[solver_gauss_seidel_get_index(x-1,y,z,GRIDDIM)] +
            phi[solver_gauss_seidel_get_index(x+1,y,z,GRIDDIM)] +
            phi[solver_gauss_seidel_get_index(x,y-1,z,GRIDDIM)] +
            phi[solver_gauss_seidel_get_index(x,y+1,z,GRIDDIM)] +
            phi[solver_gauss_seidel_get_index(x,y,z-1,GRIDDIM)] +
            phi[solver_gauss_seidel_get_index(x,y,z+1,GRIDDIM)]
        )
    ) / c;
}


/**
 * Iterates the navier stokes solver
 * @return The cummulative normalized residual
 */
LIBRARY_API float solver_navier_stokes_iterate(float * phi, float * phi0, int GRIDDIM){
    int x, y, z;
    float residual = 0;
    float rLocal = 0;
    for(z = 1; z < GRIDDIM-1; z++){
        for(y = 1; y < GRIDDIM-1; y++){
            for(x = 1; x < GRIDDIM-1; x++){
                solver_navier_stokes_approximate(phi, phi0, x, y, z, GRIDDIM);
            }
        }
    }
    for(z = 1; z < GRIDDIM-1; z++){
        for(y = 1; y < GRIDDIM-1; y++){
            for(x = 1; x < GRIDDIM-1; x++){
                rLocal = solver_navier_stokes_get_residual(phi,phi0,x,y,z,GRIDDIM);
                residual = residual + rLocal * rLocal;
            }
        }
    }
    return (float)sqrt(residual);
}

