
#include "fluid/sim/pressurecell/bounds.h"



/**
 * Sets the bounds reflecting off hard borders and otherwise assuming continuity
 */
void fluid_pressurecell_set_bounds_legacy(
    Environment * environment,
    int vector_dir,
    float * target
){
    //set the boundary planes
    for(int x = 1; x < DIM-1; x++){
        for(int y = 1; y < DIM-1; y++){

            //x-direction boundary planes
            if(vector_dir == FLUID_PRESSURECELL_DIRECTION_U){
                target[IX(0,x,y)]     = -target[IX(1,x,y)];
                target[IX(DIM-1,x,y)] = -target[IX(DIM-2,x,y)];
            } else {
                target[IX(0,x,y)]     = target[IX(1,x,y)];
                target[IX(DIM-1,x,y)] = target[IX(DIM-2,x,y)];
            }

            //y-direction boundary planes
            if(vector_dir == FLUID_PRESSURECELL_DIRECTION_V){
                target[IX(x,0,y)]     = -target[IX(x,1,y)];
                target[IX(x,DIM-1,y)] = -target[IX(x,DIM-2,y)];
            } else {
                target[IX(x,0,y)]     = target[IX(x,1,y)];
                target[IX(x,DIM-1,y)] = target[IX(x,DIM-2,y)];
            }

            //z-direction boundary planes
            if(vector_dir == FLUID_PRESSURECELL_DIRECTION_W){
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
void fluid_pressurecell_set_bounds_chunked(
    Environment * environment,
    int vector_dir,
    float * target,
    float * boundDir
){
    //set the boundary planes
    for(int x = 1; x < DIM-1; x++){
        for(int y = 1; y < DIM-1; y++){

            //x-direction boundary planes
            if(vector_dir == FLUID_PRESSURECELL_DIRECTION_U){
                if(boundDir[IX(0,x,y)] < BOUND_CUTOFF_VALUE){
                    target[IX(0,x,y)]     = -target[IX(1,x,y)];
                }
                if(boundDir[IX(DIM-1,x,y)] < BOUND_CUTOFF_VALUE){
                    target[IX(DIM-1,x,y)] = -target[IX(DIM-2,x,y)];
                }
            } else {
                target[IX(0,x,y)]     = target[IX(1,x,y)];
                target[IX(DIM-1,x,y)] = target[IX(DIM-2,x,y)];
            }

            //y-direction boundary planes
            if(vector_dir == FLUID_PRESSURECELL_DIRECTION_V){
                if(boundDir[IX(x,0,y)] < BOUND_CUTOFF_VALUE){
                    target[IX(x,0,y)]     = -target[IX(x,1,y)];
                }
                if(boundDir[IX(x,DIM-1,y)] < BOUND_CUTOFF_VALUE){
                    target[IX(x,DIM-1,y)] = -target[IX(x,DIM-2,y)];
                }
            } else {
                target[IX(x,0,y)]     = target[IX(x,1,y)];
                target[IX(x,DIM-1,y)] = target[IX(x,DIM-2,y)];
            }

            //z-direction boundary planes
            if(vector_dir == FLUID_PRESSURECELL_DIRECTION_W){
                if(boundDir[IX(x,y,0)] < BOUND_CUTOFF_VALUE){
                    target[IX(x,y,0)]     = -target[IX(x,y,1)];
                }
                if(boundDir[IX(x,y,DIM-1)] < BOUND_CUTOFF_VALUE){
                    target[IX(x,y,DIM-1)] = -target[IX(x,y,DIM-2)];
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
 * Sets the bounds to 0
 */
void fluid_pressurecell_set_bounds_zero(
    Environment * environment,
    int vector_dir,
    float * target
){
    //set the boundary planes
    for(int x = 1; x < DIM-1; x++){
        for(int y = 1; y < DIM-1; y++){
            //x-direction boundary planes
            target[IX(0,x,y)]     = 0;
            target[IX(DIM-1,x,y)] = 0;
            //y-direction boundary planes
            target[IX(x,0,y)]     = 0;
            target[IX(x,DIM-1,y)] = 0;
            //z-direction boundary planes
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

        target[IX(0,x,0)] = 0;
        target[IX(DIM-1,x,0)] = 0;
        target[IX(0,x,DIM-1)] = 0;
        target[IX(DIM-1,x,DIM-1)] = 0;


        target[IX(0,0,x)] = 0;
        target[IX(DIM-1,0,x)] = 0;
        target[IX(0,DIM-1,x)] = 0;
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
 * Updates the bounds of the chunk based on its neighbors
*/
LIBRARY_API void fluid_pressurecell_update_bounds(Environment * environment, Chunk * chunk){
    // fluid_pressurecell_set_bounds_legacy(environment,FLUID_PRESSURECELL_BOUND_NO_DIR,chunk->d[CENTER_LOC]);
    // fluid_pressurecell_set_bounds_legacy(environment,FLUID_PRESSURECELL_BOUND_NO_DIR,chunk->d0[CENTER_LOC]);

    fluid_pressurecell_set_bounds_legacy(environment,FLUID_PRESSURECELL_DIRECTION_U,chunk->u[CENTER_LOC]);
    fluid_pressurecell_set_bounds_legacy(environment,FLUID_PRESSURECELL_DIRECTION_V,chunk->v[CENTER_LOC]);
    fluid_pressurecell_set_bounds_legacy(environment,FLUID_PRESSURECELL_DIRECTION_W,chunk->w[CENTER_LOC]);

    // fluid_pressurecell_set_bounds_chunked(environment,FLUID_PRESSURECELL_DIRECTION_U,chunk->u[CENTER_LOC],chunk->bounds[CENTER_LOC]);
    // fluid_pressurecell_set_bounds_chunked(environment,FLUID_PRESSURECELL_DIRECTION_V,chunk->v[CENTER_LOC],chunk->bounds[CENTER_LOC]);
    // fluid_pressurecell_set_bounds_chunked(environment,FLUID_PRESSURECELL_DIRECTION_W,chunk->w[CENTER_LOC],chunk->bounds[CENTER_LOC]);
    
    fluid_pressurecell_set_bounds_zero(environment,FLUID_PRESSURECELL_DIRECTION_U,chunk->u0[CENTER_LOC]);
    fluid_pressurecell_set_bounds_zero(environment,FLUID_PRESSURECELL_DIRECTION_V,chunk->v0[CENTER_LOC]);
    fluid_pressurecell_set_bounds_zero(environment,FLUID_PRESSURECELL_DIRECTION_W,chunk->w0[CENTER_LOC]);

    // fluid_pressurecell_set_bounds_legacy(environment,FLUID_PRESSURECELL_DIRECTION_U,chunk->u0[CENTER_LOC]);
    // fluid_pressurecell_set_bounds_legacy(environment,FLUID_PRESSURECELL_DIRECTION_V,chunk->v0[CENTER_LOC]);
    // fluid_pressurecell_set_bounds_legacy(environment,FLUID_PRESSURECELL_DIRECTION_W,chunk->w0[CENTER_LOC]);
}


/**
 * Enforces velocity not running into bounds
*/
LIBRARY_API void pressurecell_enforce_bounds(Environment * environment, Chunk * chunk){
    int x, y, z;
    float * u = chunk->uTempCache;
    float * v = chunk->vTempCache;
    float * w = chunk->wTempCache;
    float * b = chunk->bounds[CENTER_LOC];
    for(x = 1; x < DIM-1; x++){
        for(y = 1; y < DIM-1; y++){
            for(z = 1; z < DIM-1; z++){
                if(u[IX(x,y,z)] > 0 && b[IX(x+1,y,z)] > 0){
                    u[IX(x,y,z)] = 0;
                }
                if(u[IX(x,y,z)] < 0 && b[IX(x-1,y,z)] > 0){
                    u[IX(x,y,z)] = 0;
                }
                if(v[IX(x,y,z)] > 0 && b[IX(x,y+1,z)] > 0){
                    v[IX(x,y,z)] = 0;
                }
                if(v[IX(x,y,z)] < 0 && b[IX(x,y-1,z)] > 0){
                    v[IX(x,y,z)] = 0;
                }
                if(w[IX(x,y,z)] > 0 && b[IX(x,y,z+1)] > 0){
                    w[IX(x,y,z)] = 0;
                }
                if(w[IX(x,y,z)] < 0 && b[IX(x,y,z-1)] > 0){
                    w[IX(x,y,z)] = 0;
                }
            }
        }
    }
}

/**
 * Updates the interest tree for this chunk
 */
// LIBRARY_API void pressurecell_update_interest(Environment * environment, Chunk * chunk){
//     int level, x, y, z;
//     int dim;
//     float * densityArr = chunk->d[CENTER_LOC];
//     float * densitSrcArr = chunk->d0[CENTER_LOC];
//     // for(level = 0; level < CHUNK_MAX_INTEREST_LEVEL; level++){
//     //     dim = pow(2,level);
//     //     for(x = 0; x < dim; x++){
//     //         for(y = 0; y < dim; y++){
//     //             for(z = 0; z < dim; z++){
//     //                 if(
//     //                     densityArr[IX(x,y,z)] > 0 ||
//     //                     densityArr[IX(x+1,y,z)] > 0 ||
//     //                     densityArr[IX(x-1,y,z)] > 0 ||
//     //                     densityArr[IX(x,y+1,z)] > 0 ||
//     //                     densityArr[IX(x,y-1,z)] > 0 ||
//     //                     densityArr[IX(x,y,z+1)] > 0 ||
//     //                     densityArr[IX(x,y,z-1)] > 0
//     //                     ){

//     //                 }
//     //             }
//     //         }
//     //     }
//     // }
//     for(x = 1; x < DIM-1; x++){
//         for(y = 1; y < DIM-1; y++){
//             for(z = 1; z < DIM-1; z++){
//                 if(
//                     densityArr[IX(x,y,z)] > MIN_FLUID_VALUE ||
//                     densityArr[IX(x+1,y,z)] > MIN_FLUID_VALUE ||
//                     densityArr[IX(x-1,y,z)] > MIN_FLUID_VALUE ||
//                     densityArr[IX(x,y+1,z)] > MIN_FLUID_VALUE ||
//                     densityArr[IX(x,y-1,z)] > MIN_FLUID_VALUE ||
//                     densityArr[IX(x,y,z+1)] > MIN_FLUID_VALUE ||
//                     densityArr[IX(x,y,z-1)] > MIN_FLUID_VALUE ||
//                     densitSrcArr[IX(x,y,z)] > MIN_FLUID_VALUE
//                 ){
//                     INTEREST(interestTree,0,x,y,z) = 1;
//                     INTEREST(interestTree,1,x,y,z) = 1;
//                     INTEREST(interestTree,2,x,y,z) = 1;
//                     INTEREST(interestTree,3,x,y,z) = 1;
//                     INTEREST(interestTree,4,x,y,z) = 1;
//                 }
//             }
//         }
//     }
// }
