

#include "fluid/env/utilities.h"
#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/sim/grid2/flux.h"


/**
 * Estimaates the ghost flux for this chunk
 */
void fluid_grid2_estimate_ghost_flux(float ** arrays){
    int i, j;
    int neighborIndex;
    //x+ face
    neighborIndex = CK(2,1,1);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                // arrays[CENTER_LOC][IX(DIM-1,     i,     j)] = arrays[neighborIndex][IX(     1,     i,     j)];
                arrays[CENTER_LOC][IX(DIM-1,     i,     j)] = (arrays[neighborIndex][IX(     1,     i,     j)] + arrays[CENTER_LOC][IX(DIM-2,     i,     j)])/2.0f;
            }
        }
    } else {
        for(i = 1; i < DIM; i++){
            for(j = 1; j < DIM; j++){
                arrays[CENTER_LOC][IX(DIM-1,     i,     j)] = arrays[CENTER_LOC][IX(DIM-2,     i,     j)];
            }
        }
    }

    //x- face
    neighborIndex = CK(0,1,1);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                // arrays[CENTER_LOC][IX(0,     i,     j)] = arrays[neighborIndex][IX(DIM-2,     i,     j)];
                arrays[CENTER_LOC][IX(0,     i,     j)] = (arrays[neighborIndex][IX(DIM-2,     i,     j)] + arrays[CENTER_LOC][IX(1,     i,     j)])/2.0f;
            }
        }
    } else {
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(0,     i,     j)] = arrays[CENTER_LOC][IX(1,     i,     j)];
            }
        }
    }

    //y+ face
    neighborIndex = CK(1,2,1);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                // arrays[CENTER_LOC][IX(    i, DIM-1,     j)] = arrays[neighborIndex][IX(     i,     1,     j)];
                arrays[CENTER_LOC][IX(    i, DIM-1,     j)] = (arrays[neighborIndex][IX(     i,     1,     j)] + arrays[CENTER_LOC][IX(    i, DIM-2,     j)])/2.0f;
            }
        }
    } else {
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(    i, DIM-1,     j)] = arrays[CENTER_LOC][IX(    i, DIM-2,     j)];
            }
        }
    }

    //y- face
    neighborIndex = CK(1,0,1);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                // arrays[CENTER_LOC][IX(i,     0,     j)] = arrays[neighborIndex][IX(     i,     DIM-2,     j)];
                arrays[CENTER_LOC][IX(i,     0,     j)] = (arrays[neighborIndex][IX(     i,     DIM-2,     j)] + arrays[CENTER_LOC][IX(i,     1,     j)])/2.0f;
            }
        }
    } else {
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(i,     0,     j)] = arrays[CENTER_LOC][IX(i,     1,     j)];
            }
        }
    }

    //z+ face
    neighborIndex = CK(1,1,2);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                // arrays[CENTER_LOC][IX(    i,     j, DIM-1)] = arrays[neighborIndex][IX(     i,     j,     1)];
                arrays[CENTER_LOC][IX(    i,     j, DIM-1)] = (arrays[neighborIndex][IX(     i,     j,     1)] + arrays[CENTER_LOC][IX(    i,     j, DIM-2)])/2.0f;
            }
        }
    } else {
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(    i,     j, DIM-1)] = arrays[CENTER_LOC][IX(    i,     j, DIM-2)];
            }
        }
    }

    //z- face
    neighborIndex = CK(1,1,0);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                // arrays[CENTER_LOC][IX(i,     j,     0)] = arrays[neighborIndex][IX(     i,     j,     DIM-2)];
                arrays[CENTER_LOC][IX(i,     j,     0)] = (arrays[neighborIndex][IX(     i,     j,     DIM-2)] + arrays[CENTER_LOC][IX(i,     j,     1)])/2.0f;
            }
        }
    } else {
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(i,     j,     0)] = arrays[CENTER_LOC][IX(i,     j,     1)];
            }
        }
    }


    //
    // edges
    //

    //x+ y+ edge
    neighborIndex = CK(2,2,1);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            // arrays[CENTER_LOC][IX(DIM-1, DIM-1,     i)] = arrays[neighborIndex][IX(     1,     1,     i)];
            arrays[CENTER_LOC][IX(DIM-1, DIM-1,     i)] = (arrays[neighborIndex][IX(     1,     1,     i)] + arrays[CENTER_LOC][IX(DIM-2, DIM-2,     i)])/2.0f;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(DIM-1, DIM-1,     i)] = arrays[CENTER_LOC][IX(DIM-2, DIM-2,     i)];
        }
    }

    //x+ y- edge
    neighborIndex = CK(2,0,1);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            // arrays[CENTER_LOC][IX(DIM-1, 0,     i)] = arrays[neighborIndex][IX(     1,     DIM-2,     i)];
            arrays[CENTER_LOC][IX(DIM-1, 0,     i)] = (arrays[neighborIndex][IX(     1,     DIM-2,     i)] + arrays[CENTER_LOC][IX(DIM-2, 1,     i)])/2.0f;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(DIM-1, 0,     i)] = arrays[CENTER_LOC][IX(DIM-2, 1,     i)];
        }
    }

    //x- y+ edge
    neighborIndex = CK(0,2,1);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            // arrays[CENTER_LOC][IX(     0, DIM-1,     i)] = arrays[neighborIndex][IX(     DIM-2,     1,     i)];
            arrays[CENTER_LOC][IX(     0, DIM-1,     i)] = (arrays[neighborIndex][IX(     DIM-2,     1,     i)] + arrays[CENTER_LOC][IX(      1, DIM-2,     i)])/2.0f;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(      0, DIM-1,     i)] = arrays[CENTER_LOC][IX(      1, DIM-2,     i)];
        }
    }

    //x- y- edge
    neighborIndex = CK(0,0,1);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            // arrays[CENTER_LOC][IX(    0, 0,     i)] = arrays[neighborIndex][IX(   DIM-2,     DIM-2,     i)];
            arrays[CENTER_LOC][IX(    0, 0,     i)] = (arrays[neighborIndex][IX(   DIM-2,     DIM-2,     i)] + arrays[CENTER_LOC][IX(    1, 1,     i)])/2.0f;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(    0, 0,     i)] = arrays[CENTER_LOC][IX(    1, 1,     i)];
        }
    }









    //x+ z+ edge
    neighborIndex = CK(2,1,2);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            // arrays[CENTER_LOC][IX(DIM-1,    i, DIM-1)] = arrays[neighborIndex][IX(     1,    i,     1)];
            arrays[CENTER_LOC][IX(DIM-1,    i, DIM-1)] = (arrays[neighborIndex][IX(     1,    i,     1)] + arrays[CENTER_LOC][IX(DIM-2,    i, DIM-2)])/2.0f;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(DIM-1,    i, DIM-1)] = arrays[CENTER_LOC][IX(DIM-2,    i, DIM-2)];
        }
    }

    //x+ z- edge
    neighborIndex = CK(2,1,0);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            // arrays[CENTER_LOC][IX(DIM-1,    i, 0)] = arrays[neighborIndex][IX(     1,    i,     DIM-2)];
            arrays[CENTER_LOC][IX(DIM-1,    i, 0)] = (arrays[neighborIndex][IX(     1,    i,     DIM-2)] + arrays[CENTER_LOC][IX(DIM-2,    i, 1)])/2.0f;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(DIM-1,    i, 0)] = arrays[CENTER_LOC][IX(DIM-2,    i, 1)];
        }
    }

    //x- z+ edge
    neighborIndex = CK(0,1,2);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            // arrays[CENTER_LOC][IX(     0,    i, DIM-1)] = arrays[neighborIndex][IX(     DIM-2,    i,     1)];
            arrays[CENTER_LOC][IX(     0,    i, DIM-1)] = (arrays[neighborIndex][IX(     DIM-2,    i,     1)] + arrays[CENTER_LOC][IX(      1,    i, DIM-2)])/2.0f;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(      0,    i, DIM-1)] = arrays[CENTER_LOC][IX(      1,    i, DIM-2)];
        }
    }

    //x- z- edge
    neighborIndex = CK(0,1,0);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            // arrays[CENTER_LOC][IX(    0,    i, 0)] = arrays[neighborIndex][IX(   DIM-2,    i,     DIM-2)];
            arrays[CENTER_LOC][IX(    0,    i, 0)] = (arrays[neighborIndex][IX(   DIM-2,    i,     DIM-2)] + arrays[CENTER_LOC][IX(    1,    i, 1)])/2.0f;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(    0,    i, 0)] = arrays[CENTER_LOC][IX(    1,    i, 1)];
        }
    }








    //y+ z+ edge
    neighborIndex = CK(1,2,2);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            // arrays[CENTER_LOC][IX(    i, DIM-1,    DIM-1)] = arrays[neighborIndex][IX(    i,     1,     1)];
            arrays[CENTER_LOC][IX(    i, DIM-1,    DIM-1)] = (arrays[neighborIndex][IX(    i,     1,     1)] + arrays[CENTER_LOC][IX(    i, DIM-2, DIM-2)])/2.0f;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(    i, DIM-1, DIM-1)] = arrays[CENTER_LOC][IX(    i, DIM-2, DIM-2)];
        }
    }

    //y+ z- edge
    neighborIndex = CK(1,2,0);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            // arrays[CENTER_LOC][IX(    i,DIM-1, 0)] = arrays[neighborIndex][IX(    i,     1,     DIM-2)];
            arrays[CENTER_LOC][IX(    i,DIM-1, 0)] = (arrays[neighborIndex][IX(    i,     1,     DIM-2)] + arrays[CENTER_LOC][IX(    i,DIM-2, 1)])/2.0f;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(    i,DIM-1, 0)] = arrays[CENTER_LOC][IX(    i,DIM-2, 1)];
        }
    }

    //y- z+ edge
    neighborIndex = CK(1,0,2);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            // arrays[CENTER_LOC][IX(    i,     0, DIM-1)] = arrays[neighborIndex][IX(    i,     DIM-2,    1)];
            arrays[CENTER_LOC][IX(    i,     0, DIM-1)] = (arrays[neighborIndex][IX(    i,     DIM-2,    1)] + arrays[CENTER_LOC][IX(    i,      1, DIM-2)])/2.0f;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(    i,      0, DIM-1)] = arrays[CENTER_LOC][IX(    i,      1, DIM-2)];
        }
    }

    //y- z- edge
    neighborIndex = CK(1,0,0);
    if(arrays[neighborIndex] != NULL){
        for(i = 1; i < DIM-1; i++){
            // arrays[CENTER_LOC][IX(    i,    0, 0)] = arrays[neighborIndex][IX(    i,   DIM-2,     DIM-2)];
            arrays[CENTER_LOC][IX(    i,    0, 0)] = (arrays[neighborIndex][IX(    i,   DIM-2,     DIM-2)] + arrays[CENTER_LOC][IX(    i,    1, 1)])/2.0f;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(    i,    0, 0)] = arrays[CENTER_LOC][IX(    i,    1, 1)];
        }
    }


    //
    //  CORNERS
    //

    //x+ y+ z+ corner
    neighborIndex = CK(2,2,2);
    if(arrays[neighborIndex] != NULL){
        // arrays[CENTER_LOC][IX(DIM-1, DIM-1, DIM-1)] = arrays[neighborIndex][IX(     1,     1,     1)];
        arrays[CENTER_LOC][IX(DIM-1, DIM-1, DIM-1)] = (arrays[neighborIndex][IX(     1,     1,     1)] + arrays[CENTER_LOC][IX(DIM-2, DIM-2, DIM-2)])/2.0f;
    } else {
        arrays[CENTER_LOC][IX(DIM-1, DIM-1, DIM-1)] = arrays[CENTER_LOC][IX(DIM-2, DIM-2, DIM-2)];
    }

    //x+ y+ z- corner
    neighborIndex = CK(2,2,0);
    if(arrays[neighborIndex] != NULL){
        // arrays[CENTER_LOC][IX(DIM-1, DIM-1,     0)] = arrays[neighborIndex][IX(     1,     1, DIM-2)];
        arrays[CENTER_LOC][IX(DIM-1, DIM-1,     0)] = (arrays[neighborIndex][IX(     1,     1, DIM-2)] + arrays[CENTER_LOC][IX(DIM-2, DIM-2,     1)])/2.0f;
    } else {
        arrays[CENTER_LOC][IX(DIM-1, DIM-1,     0)] = arrays[CENTER_LOC][IX(DIM-2, DIM-2,     1)];
    }



    //x+ y- z+ corner
    neighborIndex = CK(2,0,2);
    if(arrays[neighborIndex] != NULL){
        // arrays[CENTER_LOC][IX(DIM-1, 0, DIM-1)] = arrays[neighborIndex][IX(     1, DIM-2,     1)];
        arrays[CENTER_LOC][IX(DIM-1, 0, DIM-1)] = (arrays[neighborIndex][IX(     1, DIM-2,     1)] + arrays[CENTER_LOC][IX(DIM-2, 1, DIM-2)])/2.0f;
    } else {
        arrays[CENTER_LOC][IX(DIM-1, 0, DIM-1)] = arrays[CENTER_LOC][IX(DIM-2, 1, DIM-2)];
    }

    //x+ y- z- corner
    neighborIndex = CK(2,0,0);
    if(arrays[neighborIndex] != NULL){
        // arrays[CENTER_LOC][IX(DIM-1, 0,     0)] = arrays[neighborIndex][IX(     1, DIM-2, DIM-2)];
        arrays[CENTER_LOC][IX(DIM-1, 0,     0)] = (arrays[neighborIndex][IX(     1, DIM-2, DIM-2)] + arrays[CENTER_LOC][IX(DIM-2, 1,     1)])/2.0f;
    } else {
        arrays[CENTER_LOC][IX(DIM-1, 0,     0)] = arrays[CENTER_LOC][IX(DIM-2, 1,     1)];
    }



    //x- y+ z+ corner
    neighborIndex = CK(0,2,2);
    if(arrays[neighborIndex] != NULL){
        // arrays[CENTER_LOC][IX(0, DIM-1, DIM-1)] = arrays[neighborIndex][IX( DIM-2,     1,     1)];
        arrays[CENTER_LOC][IX(0, DIM-1, DIM-1)] = (arrays[neighborIndex][IX( DIM-2,     1,     1)] + arrays[CENTER_LOC][IX(1, DIM-2, DIM-2)])/2.0f;
    } else {
        arrays[CENTER_LOC][IX(0, DIM-1, DIM-1)] = arrays[CENTER_LOC][IX(1, DIM-2, DIM-2)];
    }

    //x- y+ z- corner
    neighborIndex = CK(0,2,0);
    if(arrays[neighborIndex] != NULL){
        // arrays[CENTER_LOC][IX(0, DIM-1,     0)] = arrays[neighborIndex][IX( DIM-2,     1, DIM-2)];
        arrays[CENTER_LOC][IX(0, DIM-1,     0)] = (arrays[neighborIndex][IX( DIM-2,     1, DIM-2)] + arrays[CENTER_LOC][IX(1, DIM-2,     1)])/2.0f;
    } else {
        arrays[CENTER_LOC][IX(0, DIM-1,     0)] = arrays[CENTER_LOC][IX(1, DIM-2,     1)];
    }



    //x- y- z+ corner
    neighborIndex = CK(0,0,2);
    if(arrays[neighborIndex] != NULL){
        // arrays[CENTER_LOC][IX(0, 0, DIM-1)] = arrays[neighborIndex][IX( DIM-2, DIM-2,     1)];
        arrays[CENTER_LOC][IX(0, 0, DIM-1)] = (arrays[neighborIndex][IX( DIM-2, DIM-2,     1)] + arrays[CENTER_LOC][IX(1, 1, DIM-2)])/2.0f;
    } else {
        arrays[CENTER_LOC][IX(0, 0, DIM-1)] = arrays[CENTER_LOC][IX(1, 1, DIM-2)];
    }

    //x- y- z- corner
    neighborIndex = CK(0,0,0);
    if(arrays[neighborIndex] != NULL){
        // arrays[CENTER_LOC][IX(0, 0,     0)] = arrays[neighborIndex][IX( DIM-2, DIM-2, DIM-2)];
        arrays[CENTER_LOC][IX(0, 0,     0)] = (arrays[neighborIndex][IX( DIM-2, DIM-2, DIM-2)] + arrays[CENTER_LOC][IX(1, 1,     1)])/2.0f;
    } else {
        arrays[CENTER_LOC][IX(0, 0,     0)] = arrays[CENTER_LOC][IX(1, 1,     1)];
    }
}


/**
 * Updates the flux stored in the ghost cells of this chunk
 */
void fluid_grid2_update_ghost_flux(Environment * environment, Chunk * chunk){
    fluid_grid2_estimate_ghost_flux(chunk->pressureCache);
    fluid_grid2_estimate_ghost_flux(chunk->divergenceCache);
}