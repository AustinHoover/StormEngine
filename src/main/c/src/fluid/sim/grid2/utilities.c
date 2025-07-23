#include <stdio.h>
#include <stdint.h>
#include <immintrin.h>

#include "fluid/env/utilities.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/environment.h"
#include "fluid/queue/chunk.h"
#include "fluid/sim/grid2/solver_consts.h"
#include "fluid/sim/grid2/utilities.h"
#include "fluid/sim/grid2/velocity.h"





/**
 * Adds from a source array to a destination array
*/
void fluid_grid2_add_source(float * x, float * s, float dt){
	int i;
    int size=DIM*DIM*DIM;
    __m256 existing;
    __m256 delta;
    __m256 dtVec = _mm256_set1_ps(dt);
	for(i=0; i<size; i=i+8){
        existing = _mm256_loadu_ps(&x[i]);
        delta = _mm256_loadu_ps(&s[i]);
        _mm256_storeu_ps(&x[i],
            _mm256_add_ps(
                existing,
                _mm256_mul_ps(
                    delta,
                    dtVec
                )
            )
        );
    }
    // for(i=0; i<size; i++){
    //     x[i] += dt*s[i];
    // }
}


/**
 * Sets the bounds reflecting off hard borders and otherwise assuming continuity
 */
void fluid_grid2_set_bounds_legacy(
    Environment * environment,
    int vector_dir,
    float * target
){
    //set the boundary planes
    for(int x = 1; x < DIM-1; x++){
        for(int y = 1; y < DIM-1; y++){

            //x-direction boundary planes
            if(vector_dir==BOUND_SET_VECTOR_DIFFUSE_PHI_U || vector_dir==BOUND_SET_VECTOR_U){
                target[IX(0,x,y)]     = -target[IX(1,x,y)];
                target[IX(DIM-1,x,y)] = -target[IX(DIM-2,x,y)];
            } else {
                target[IX(0,x,y)]     = target[IX(1,x,y)];
                target[IX(DIM-1,x,y)] = target[IX(DIM-2,x,y)];
            }

            //y-direction boundary planes
            if(vector_dir==BOUND_SET_VECTOR_DIFFUSE_PHI_V || vector_dir==BOUND_SET_VECTOR_V){
                target[IX(x,0,y)]     = -target[IX(x,1,y)];
                target[IX(x,DIM-1,y)] = -target[IX(x,DIM-2,y)];
            } else {
                target[IX(x,0,y)]     = target[IX(x,1,y)];
                target[IX(x,DIM-1,y)] = target[IX(x,DIM-2,y)];
            }

            //z-direction boundary planes
            if(vector_dir==BOUND_SET_VECTOR_DIFFUSE_PHI_W || vector_dir==BOUND_SET_VECTOR_W){
                target[IX(x,y,0)]     = -target[IX(x,y,1)];
                target[IX(x,y,DIM-1)] = -target[IX(x,y,DIM-2)];
            } else {
                target[IX(x,y,0)]     = target[IX(x,y,1)];
                target[IX(x,y,DIM-1)] = target[IX(x,y,DIM-2)];
            }
        }
    }

    //sets the edges of the chunk
    //this should logically follow from how we're treating the boundary planes
    for(int x = 1; x < DIM-1; x++){
        target[IX(x,0,0)]         = (float)(0.5f * (target[IX(x,1,0)] + target[IX(x,0,1)]));
        target[IX(x,DIM-1,0)]     = (float)(0.5f * (target[IX(x,DIM-2,0)] + target[IX(x,DIM-1,1)]));
        target[IX(x,0,DIM-1)]     = (float)(0.5f * (target[IX(x,1,DIM-1)] + target[IX(x,0,DIM-2)]));
        target[IX(x,DIM-1,DIM-1)] = (float)(0.5f * (target[IX(x,DIM-2,DIM-1)] + target[IX(x,DIM-1,DIM-2)]));

        target[IX(0,x,0)] = (float)(0.5f * (target[IX(1,x,0)] + target[IX(0,x,1)]));
        target[IX(DIM-1,x,0)] = (float)(0.5f * (target[IX(DIM-2,x,0)] + target[IX(DIM-1,x,1)]));
        target[IX(0,x,DIM-1)] = (float)(0.5f * (target[IX(1,x,DIM-1)] + target[IX(0,x,DIM-2)]));
        target[IX(DIM-1,x,DIM-1)] = (float)(0.5f * (target[IX(DIM-2,x,DIM-1)] + target[IX(DIM-1,x,DIM-2)]));


        target[IX(0,0,x)] = (float)(0.5f * (target[IX(1,0,x)] + target[IX(0,1,x)]));
        target[IX(DIM-1,0,x)] = (float)(0.5f * (target[IX(DIM-2,0,x)] + target[IX(DIM-1,1,x)]));
        target[IX(0,DIM-1,x)] = (float)(0.5f * (target[IX(1,DIM-1,x)] + target[IX(0,DIM-2,x)]));
        target[IX(DIM-1,DIM-1,x)] = (float)(0.5f * (target[IX(DIM-2,DIM-1,x)] + target[IX(DIM-1,DIM-2,x)]));

    }
    //sets the corners of the chunk
    //this should logically follow from how we're treating the boundary planes
    target[IX(0,0,0)]             = (float)((target[IX(1,0,0)]+target[IX(0,1,0)]+target[IX(0,0,1)])/3.0);
    target[IX(DIM-1,0,0)]         = (float)((target[IX(DIM-2,0,0)]+target[IX(DIM-1,1,0)]+target[IX(DIM-1,0,1)])/3.0);
    target[IX(0,DIM-1,0)]         = (float)((target[IX(1,DIM-1,0)]+target[IX(0,DIM-2,0)]+target[IX(0,DIM-1,1)])/3.0);
    target[IX(0,0,DIM-1)]         = (float)((target[IX(0,0,DIM-2)]+target[IX(1,0,DIM-1)]+target[IX(0,1,DIM-1)])/3.0);
    target[IX(DIM-1,DIM-1,0)]     = (float)((target[IX(DIM-2,DIM-1,0)]+target[IX(DIM-1,DIM-2,0)]+target[IX(DIM-1,DIM-1,1)])/3.0);
    target[IX(0,DIM-1,DIM-1)]     = (float)((target[IX(1,DIM-1,DIM-1)]+target[IX(0,DIM-2,DIM-1)]+target[IX(0,DIM-1,DIM-2)])/3.0);
    target[IX(DIM-1,0,DIM-1)]     = (float)((target[IX(DIM-1,0,DIM-2)]+target[IX(DIM-2,0,DIM-1)]+target[IX(DIM-1,1,DIM-1)])/3.0);
    target[IX(DIM-1,DIM-1,DIM-1)] = (float)((target[IX(DIM-1,DIM-1,DIM-2)]+target[IX(DIM-1,DIM-2,DIM-1)]+target[IX(DIM-1,DIM-1,DIM-2)])/3.0);
}

/**
 * Sets the bounds reflecting off hard borders and otherwise assuming continuity
 */
void fluid_grid2_set_bounds_reflection(
    Environment * environment,
    int vector_dir,
    float * target
){
    float * boundsArr = environment->state.grid2.fluid_grid2_neighborArr_bounds;

    //set the internal boundaries
    for(int x = 1; x < DIM-1; x++){
        for(int y = 1; y < DIM-1; y++){
            for(int z = 1; z < DIM-1; z++){
                if(boundsArr[IX(x,y,z)] > 0){
                    target[IX(x,y,z)] = 0;
                }
            }
        }
    }

    //set the boundary planes
    for(int x=1; x < DIM-1; x++){
        for(int y = 1; y < DIM-1; y++){

            //x-direction boundary planes
            if(vector_dir==BOUND_SET_VECTOR_DIFFUSE_PHI_U || vector_dir==BOUND_SET_VECTOR_U){
                if(boundsArr[IX(0,x,y)] > 0){
                    target[IX(0,x,y)]     = -target[IX(1,x,y)];
                } else {
                    target[IX(0,x,y)]     = 0;
                }
                if(boundsArr[IX(DIM-1,x,y)] > 0){
                    target[IX(DIM-1,x,y)] = -target[IX(DIM-2,x,y)];
                } else {
                    target[IX(DIM-1,x,y)] = 0;
                }
            } else {
                target[IX(0,x,y)]     = target[IX(1,x,y)];
                target[IX(DIM-1,x,y)] = target[IX(DIM-2,x,y)];
            }

            //y-direction boundary planes
            if(vector_dir==BOUND_SET_VECTOR_DIFFUSE_PHI_V || vector_dir==BOUND_SET_VECTOR_V){
                if(boundsArr[IX(x,0,y)] > 0){
                    target[IX(x,0,y)]     = -target[IX(x,1,y)];
                } else {
                    target[IX(x,0,y)]     = 0;
                }
                if(boundsArr[IX(x,DIM-1,y)] > 0){
                    target[IX(x,DIM-1,y)] = -target[IX(x,DIM-2,y)];
                } else {
                    target[IX(x,DIM-1,y)] = 0;
                }
            } else {
                target[IX(x,0,y)]     = target[IX(x,1,y)];
                target[IX(x,DIM-1,y)] = target[IX(x,DIM-2,y)];
            }

            //z-direction boundary planes
            if(vector_dir==BOUND_SET_VECTOR_DIFFUSE_PHI_W || vector_dir==BOUND_SET_VECTOR_W){
                if(boundsArr[IX(x,y,0)] > 0){
                    target[IX(x,y,0)]     = -target[IX(x,y,1)];
                } else {
                    target[IX(x,y,0)]     = 0;
                }
                if(boundsArr[IX(x,y,DIM-1)] > 0){
                    target[IX(x,y,DIM-1)] = -target[IX(x,y,DIM-2)];
                } else {
                    target[IX(x,y,DIM-1)] = 0;
                }
            } else {
                target[IX(x,y,0)]     = target[IX(x,y,1)];
                target[IX(x,y,DIM-1)] = target[IX(x,y,DIM-2)];
            }
        }
    }

    //sets the edges of the chunk
    //this should logically follow from how we're treating the boundary planes
    for(int x = 1; x < DIM-1; x++){
        target[IX(x,0,0)]         = (float)(0.5f * (target[IX(x,1,0)] + target[IX(x,0,1)]));
        target[IX(x,DIM-1,0)]     = (float)(0.5f * (target[IX(x,DIM-2,0)] + target[IX(x,DIM-1,1)]));
        target[IX(x,0,DIM-1)]     = (float)(0.5f * (target[IX(x,1,DIM-1)] + target[IX(x,0,DIM-2)]));
        target[IX(x,DIM-1,DIM-1)] = (float)(0.5f * (target[IX(x,DIM-2,DIM-1)] + target[IX(x,DIM-1,DIM-2)]));

        target[IX(0,x,0)] = (float)(0.5f * (target[IX(1,x,0)] + target[IX(0,x,1)]));
        target[IX(DIM-1,x,0)] = (float)(0.5f * (target[IX(DIM-2,x,0)] + target[IX(DIM-1,x,1)]));
        target[IX(0,x,DIM-1)] = (float)(0.5f * (target[IX(1,x,DIM-1)] + target[IX(0,x,DIM-2)]));
        target[IX(DIM-1,x,DIM-1)] = (float)(0.5f * (target[IX(DIM-2,x,DIM-1)] + target[IX(DIM-1,x,DIM-2)]));


        target[IX(0,0,x)] = (float)(0.5f * (target[IX(1,0,x)] + target[IX(0,1,x)]));
        target[IX(DIM-1,0,x)] = (float)(0.5f * (target[IX(DIM-2,0,x)] + target[IX(DIM-1,1,x)]));
        target[IX(0,DIM-1,x)] = (float)(0.5f * (target[IX(1,DIM-1,x)] + target[IX(0,DIM-2,x)]));
        target[IX(DIM-1,DIM-1,x)] = (float)(0.5f * (target[IX(DIM-2,DIM-1,x)] + target[IX(DIM-1,DIM-2,x)]));

    }
    //sets the corners of the chunk
    //this should logically follow from how we're treating the boundary planes
    target[IX(0,0,0)]             = (float)((target[IX(1,0,0)]+target[IX(0,1,0)]+target[IX(0,0,1)])/3.0);
    target[IX(DIM-1,0,0)]         = (float)((target[IX(DIM-2,0,0)]+target[IX(DIM-1,1,0)]+target[IX(DIM-1,0,1)])/3.0);
    target[IX(0,DIM-1,0)]         = (float)((target[IX(1,DIM-1,0)]+target[IX(0,DIM-2,0)]+target[IX(0,DIM-1,1)])/3.0);
    target[IX(0,0,DIM-1)]         = (float)((target[IX(0,0,DIM-2)]+target[IX(1,0,DIM-1)]+target[IX(0,1,DIM-1)])/3.0);
    target[IX(DIM-1,DIM-1,0)]     = (float)((target[IX(DIM-2,DIM-1,0)]+target[IX(DIM-1,DIM-2,0)]+target[IX(DIM-1,DIM-1,1)])/3.0);
    target[IX(0,DIM-1,DIM-1)]     = (float)((target[IX(1,DIM-1,DIM-1)]+target[IX(0,DIM-2,DIM-1)]+target[IX(0,DIM-1,DIM-2)])/3.0);
    target[IX(DIM-1,0,DIM-1)]     = (float)((target[IX(DIM-1,0,DIM-2)]+target[IX(DIM-2,0,DIM-1)]+target[IX(DIM-1,1,DIM-1)])/3.0);
    target[IX(DIM-1,DIM-1,DIM-1)] = (float)((target[IX(DIM-1,DIM-1,DIM-2)]+target[IX(DIM-1,DIM-2,DIM-1)]+target[IX(DIM-1,DIM-1,DIM-2)])/3.0);
}


/**
 * Sets the bounds assuming continuity
 */
void fluid_grid2_set_bounds_continuity(
    Environment * environment,
    float * target
){
    float * boundsArr = environment->state.grid2.fluid_grid2_neighborArr_bounds;
    //set the boundary planes
    for(int x=1; x < DIM-1; x++){
        for(int y = 1; y < DIM-1; y++){
            target[IX(0,x,y)]     = target[IX(1,x,y)];
            target[IX(DIM-1,x,y)] = target[IX(DIM-2,x,y)];
            target[IX(x,0,y)]     = target[IX(x,1,y)];
            target[IX(x,DIM-1,y)] = target[IX(x,DIM-2,y)];
            target[IX(x,y,0)]     = target[IX(x,y,1)];
            target[IX(x,y,DIM-1)] = target[IX(x,y,DIM-2)];
        }
    }

    //sets the edges of the chunk
    //this should logically follow from how we're treating the boundary planes
    for(int x = 1; x < DIM-1; x++){
        target[IX(x,0,0)]         = (float)(0.5f * (target[IX(x,1,0)] + target[IX(x,0,1)]));
        target[IX(x,DIM-1,0)]     = (float)(0.5f * (target[IX(x,DIM-2,0)] + target[IX(x,DIM-1,1)]));
        target[IX(x,0,DIM-1)]     = (float)(0.5f * (target[IX(x,1,DIM-1)] + target[IX(x,0,DIM-2)]));
        target[IX(x,DIM-1,DIM-1)] = (float)(0.5f * (target[IX(x,DIM-2,DIM-1)] + target[IX(x,DIM-1,DIM-2)]));

        target[IX(0,x,0)] = (float)(0.5f * (target[IX(1,x,0)] + target[IX(0,x,1)]));
        target[IX(DIM-1,x,0)] = (float)(0.5f * (target[IX(DIM-2,x,0)] + target[IX(DIM-1,x,1)]));
        target[IX(0,x,DIM-1)] = (float)(0.5f * (target[IX(1,x,DIM-1)] + target[IX(0,x,DIM-2)]));
        target[IX(DIM-1,x,DIM-1)] = (float)(0.5f * (target[IX(DIM-2,x,DIM-1)] + target[IX(DIM-1,x,DIM-2)]));


        target[IX(0,0,x)] = (float)(0.5f * (target[IX(1,0,x)] + target[IX(0,1,x)]));
        target[IX(DIM-1,0,x)] = (float)(0.5f * (target[IX(DIM-2,0,x)] + target[IX(DIM-1,1,x)]));
        target[IX(0,DIM-1,x)] = (float)(0.5f * (target[IX(1,DIM-1,x)] + target[IX(0,DIM-2,x)]));
        target[IX(DIM-1,DIM-1,x)] = (float)(0.5f * (target[IX(DIM-2,DIM-1,x)] + target[IX(DIM-1,DIM-2,x)]));

    }
    //sets the corners of the chunk
    //this should logically follow from how we're treating the boundary planes
    target[IX(0,0,0)]             = (float)((target[IX(1,0,0)]+target[IX(0,1,0)]+target[IX(0,0,1)])/3.0);
    target[IX(DIM-1,0,0)]         = (float)((target[IX(DIM-2,0,0)]+target[IX(DIM-1,1,0)]+target[IX(DIM-1,0,1)])/3.0);
    target[IX(0,DIM-1,0)]         = (float)((target[IX(1,DIM-1,0)]+target[IX(0,DIM-2,0)]+target[IX(0,DIM-1,1)])/3.0);
    target[IX(0,0,DIM-1)]         = (float)((target[IX(0,0,DIM-2)]+target[IX(1,0,DIM-1)]+target[IX(0,1,DIM-1)])/3.0);
    target[IX(DIM-1,DIM-1,0)]     = (float)((target[IX(DIM-2,DIM-1,0)]+target[IX(DIM-1,DIM-2,0)]+target[IX(DIM-1,DIM-1,1)])/3.0);
    target[IX(0,DIM-1,DIM-1)]     = (float)((target[IX(1,DIM-1,DIM-1)]+target[IX(0,DIM-2,DIM-1)]+target[IX(0,DIM-1,DIM-2)])/3.0);
    target[IX(DIM-1,0,DIM-1)]     = (float)((target[IX(DIM-1,0,DIM-2)]+target[IX(DIM-2,0,DIM-1)]+target[IX(DIM-1,1,DIM-1)])/3.0);
    target[IX(DIM-1,DIM-1,DIM-1)] = (float)((target[IX(DIM-1,DIM-1,DIM-2)]+target[IX(DIM-1,DIM-2,DIM-1)]+target[IX(DIM-1,DIM-1,DIM-2)])/3.0);
}

/**
 * Sets the bounds to the neighbor's value
 */
void fluid_grid2_set_bounds_ghost_cell(
    Environment * environment,
    int sourceType,
    float * target
){
    float * neighborArr;
    switch(sourceType){
        case BOUND_SET_DENSITY_PHI:
        case BOUND_SET_DENSITY: {
            neighborArr = environment->state.grid2.fluid_grid2_neighborArr_d;
        } break;
        case BOUND_SET_VECTOR_DIFFUSE_PHI_U:
        case BOUND_SET_VECTOR_U: {
            neighborArr = environment->state.grid2.fluid_grid2_neighborArr_u;
        } break;
        case BOUND_SET_VECTOR_DIFFUSE_PHI_V:
        case BOUND_SET_VECTOR_V: {
            neighborArr = environment->state.grid2.fluid_grid2_neighborArr_v;
        } break;
        case BOUND_SET_VECTOR_DIFFUSE_PHI_W:
        case BOUND_SET_VECTOR_W: {
            neighborArr = environment->state.grid2.fluid_grid2_neighborArr_w;
        } break;

        //used the cached scalar potential from last frame
        case BOUND_SET_PROJECTION_PHI: {
            neighborArr = environment->state.grid2.fluid_grid2_neighborArr_scalarCache;
        } break;
        case BOUND_SET_PROJECTION_PHI_0: {
            neighborArr = environment->state.grid2.fluid_grid2_neighborArr_divergenceCache;
        } break;
    }
    //set the boundary planes
    for(int x=1; x < DIM-1; x++){
        for(int y = 1; y < DIM-1; y++){
            target[IX(0,x,y)]     = neighborArr[IX(0,x,y)];
            target[IX(DIM-1,x,y)] = neighborArr[IX(DIM-1,x,y)];
            target[IX(x,0,y)]     = neighborArr[IX(x,0,y)];
            target[IX(x,DIM-1,y)] = neighborArr[IX(x,DIM-1,y)];
            target[IX(x,y,0)]     = neighborArr[IX(x,y,0)];
            target[IX(x,y,DIM-1)] = neighborArr[IX(x,y,DIM-1)];
        }
    }

    //sets the edges of the chunk
    //this should logically follow from how we're treating the boundary planes
    for(int x = 1; x < DIM-1; x++){
        target[IX(x,0,0)]         = neighborArr[IX(x,0,0)];
        target[IX(x,DIM-1,0)]     = neighborArr[IX(x,DIM-1,0)];
        target[IX(x,0,DIM-1)]     = neighborArr[IX(x,0,DIM-1)];
        target[IX(x,DIM-1,DIM-1)] = neighborArr[IX(x,DIM-1,DIM-1)];

        target[IX(0,x,0)]         = neighborArr[IX(0,x,0)];
        target[IX(DIM-1,x,0)]     = neighborArr[IX(DIM-1,x,0)];
        target[IX(0,x,DIM-1)]     = neighborArr[IX(0,x,DIM-1)];
        target[IX(DIM-1,x,DIM-1)] = neighborArr[IX(DIM-1,x,DIM-1)];


        target[IX(0,0,x)]         = neighborArr[IX(0,0,x)];
        target[IX(DIM-1,0,x)]     = neighborArr[IX(DIM-1,0,x)];
        target[IX(0,DIM-1,x)]     = neighborArr[IX(0,DIM-1,x)];
        target[IX(DIM-1,DIM-1,x)] = neighborArr[IX(DIM-1,DIM-1,x)];

    }
    //sets the corners of the chunk
    //this should logically follow from how we're treating the boundary planes
    target[IX(0,0,0)]             = neighborArr[IX(0,0,0)];
    target[IX(DIM-1,0,0)]         = neighborArr[IX(DIM-1,0,0)];
    target[IX(0,DIM-1,0)]         = neighborArr[IX(0,DIM-1,0)];
    target[IX(0,0,DIM-1)]         = neighborArr[IX(0,0,DIM-1)];
    target[IX(DIM-1,DIM-1,0)]     = neighborArr[IX(DIM-1,DIM-1,0)];
    target[IX(0,DIM-1,DIM-1)]     = neighborArr[IX(0,DIM-1,DIM-1)];
    target[IX(DIM-1,0,DIM-1)]     = neighborArr[IX(DIM-1,0,DIM-1)];
    target[IX(DIM-1,DIM-1,DIM-1)] = neighborArr[IX(DIM-1,DIM-1,DIM-1)];
}

/**
 * Sets the bounds to 0
 */
void fluid_grid2_set_bounds_zero(
    Environment * environment,
    float * target
){
    float * boundsArr = environment->state.grid2.fluid_grid2_neighborArr_d;
    //set the boundary planes
    for(int x=1; x < DIM-1; x++){
        for(int y = 1; y < DIM-1; y++){
            target[IX(0,x,y)]     = 0;
            target[IX(DIM-1,x,y)] = 0;
            target[IX(x,0,y)]     = 0;
            target[IX(x,DIM-1,y)] = 0;
            target[IX(x,y,0)]     = 0;
            target[IX(x,y,DIM-1)] = 0;
        }
    }

    //sets the edges of the chunk
    //this should logically follow from how we're treating the boundary planes
    for(int x = 1; x < DIM-1; x++){
        target[IX(x,0,0)]         = 0;
        target[IX(x,DIM-1,0)]     = 0;
        target[IX(x,0,DIM-1)]     = 0;
        target[IX(x,DIM-1,DIM-1)] = 0;

        target[IX(0,x,0)]         = 0;
        target[IX(DIM-1,x,0)]     = 0;
        target[IX(0,x,DIM-1)]     = 0;
        target[IX(DIM-1,x,DIM-1)] = 0;


        target[IX(0,0,x)]         = 0;
        target[IX(DIM-1,0,x)]     = 0;
        target[IX(0,DIM-1,x)]     = 0;
        target[IX(DIM-1,DIM-1,x)] = 0;

    }
    //sets the corners of the chunk
    //this should logically follow from how we're treating the boundary planes
    target[IX(0,0,0)]             = 0;
    target[IX(DIM-1,0,0)]         = 0;
    target[IX(0,DIM-1,0)]         = 0;
    target[IX(0,0,DIM-1)]         = 0;
    target[IX(DIM-1,DIM-1,0)]     = 0;
    target[IX(0,DIM-1,DIM-1)]     = 0;
    target[IX(DIM-1,0,DIM-1)]     = 0;
    target[IX(DIM-1,DIM-1,DIM-1)] = 0;
}

/**
 * Sets the bounds of this cube to those of its neighbor
*/
LIBRARY_API void fluid_grid2_set_bounds(
    Environment * environment,
    int vector_dir,
    float * target
){
    switch(vector_dir){

        case BOUND_SET_PROJECTION_PHI:
        case BOUND_SET_PROJECTION_PHI_0:
        case BOUND_SET_VECTOR_DIFFUSE_PHI_U:
        case BOUND_SET_VECTOR_DIFFUSE_PHI_V:
        case BOUND_SET_VECTOR_DIFFUSE_PHI_W:
        case BOUND_SET_DENSITY_PHI:
        case BOUND_SET_VECTOR_U:
        case BOUND_SET_VECTOR_V:
        case BOUND_SET_VECTOR_W:
        case BOUND_SET_DENSITY:
            fluid_grid2_set_bounds_legacy(environment,vector_dir,target);
        break;

        // case BOUND_SET_PROJECTION_PHI:
        // case BOUND_SET_PROJECTION_PHI_0:
        // case BOUND_SET_VECTOR_DIFFUSE_PHI_U:
        // case BOUND_SET_VECTOR_DIFFUSE_PHI_V:
        // case BOUND_SET_VECTOR_DIFFUSE_PHI_W:
        // case BOUND_SET_DENSITY_PHI:
        // case BOUND_SET_VECTOR_U:
        // case BOUND_SET_VECTOR_V:
        // case BOUND_SET_VECTOR_W:
        // case BOUND_SET_DENSITY:
        //     fluid_grid2_set_bounds_reflection(environment,vector_dir,target);
        // break;

        // case BOUND_SET_PROJECTION_PHI:
        // case BOUND_SET_PROJECTION_PHI_0:
        // case BOUND_SET_VECTOR_DIFFUSE_PHI_U:
        // case BOUND_SET_VECTOR_DIFFUSE_PHI_V:
        // case BOUND_SET_VECTOR_DIFFUSE_PHI_W:
        // case BOUND_SET_DENSITY_PHI:
        // case BOUND_SET_VECTOR_U:
        // case BOUND_SET_VECTOR_V:
        // case BOUND_SET_VECTOR_W:
        // case BOUND_SET_DENSITY:
        //     fluid_grid2_set_bounds_continuity(environment,target);
        // break;

        // case BOUND_SET_PROJECTION_PHI:
        // case BOUND_SET_PROJECTION_PHI_0:
        // case BOUND_SET_VECTOR_DIFFUSE_PHI_U:
        // case BOUND_SET_VECTOR_DIFFUSE_PHI_V:
        // case BOUND_SET_VECTOR_DIFFUSE_PHI_W:
        // case BOUND_SET_DENSITY_PHI:
        // case BOUND_SET_VECTOR_U:
        // case BOUND_SET_VECTOR_V:
        // case BOUND_SET_VECTOR_W:
        // case BOUND_SET_DENSITY:
        //     fluid_grid2_set_bounds_ghost_cell(environment,vector_dir,target);
        // break;
        
        // case BOUND_SET_PROJECTION_PHI:
        // case BOUND_SET_PROJECTION_PHI_0:
        // case BOUND_SET_VECTOR_DIFFUSE_PHI_U:
        // case BOUND_SET_VECTOR_DIFFUSE_PHI_V:
        // case BOUND_SET_VECTOR_DIFFUSE_PHI_W:
        // case BOUND_SET_DENSITY_PHI:
        // case BOUND_SET_VECTOR_U:
        // case BOUND_SET_VECTOR_V:
        // case BOUND_SET_VECTOR_W:
        // case BOUND_SET_DENSITY: 
        //     fluid_grid2_set_bounds_zero(environment,target);
        // break;
    }
}


/**
 * Sums the density of the chunk
 */
double fluid_grid2_calculateSum(float ** d){
    int j;
    int size=DIM*DIM*DIM;
    float * x = GET_ARR_RAW(d,CENTER_LOC);
    double rVal = 0;
    for(int i = 1; i < DIM - 1; i++){
        for(int j = 1; j < DIM - 1; j++){
            for(int k = 1; k < DIM - 1; k++){
                rVal = rVal + x[IX(i,j,k)];
            }
        }
    }
    return rVal;
}

/**
 * Flips two array matricies
 */
LIBRARY_API void fluid_grid2_flip_arrays(float ** array1, float ** array2){
    float * tmpArr;
    for(int j = 0; j < 27; j++){
        tmpArr = array1[j];
        array1[j] = array2[j];
        array2[j] = tmpArr;
    }
}

