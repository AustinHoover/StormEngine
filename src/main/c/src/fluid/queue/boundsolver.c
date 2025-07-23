

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/utilities.h"



/**
 * Define as 1 to source values from surrounding chunks
 */
#define USE_BOUNDS 1



static inline void fluid_solve_bounds_checker(float ** arrays, float fillVal, float * adjacencyMatrix){
    int i, j;
    int neighborIndex;
    float adjacentVal;
    //x+ face
    neighborIndex = CK(2,1,1);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / ((DIM-2)*(DIM-2));
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(DIM-1,     i,     j)] = arrays[neighborIndex][IX(     1,     i,     j)] + adjacentVal;
            }
        }
    } else {
        for(i = 1; i < DIM; i++){
            for(j = 1; j < DIM; j++){
                arrays[CENTER_LOC][IX(DIM-1,     i,     j)] = fillVal;
            }
        }
    }

    //x- face
    neighborIndex = CK(0,1,1);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / ((DIM-2)*(DIM-2));
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(0,     i,     j)] = arrays[neighborIndex][IX(DIM-2,     i,     j)] + adjacentVal;
            }
        }
    } else {
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(0,     i,     j)] = fillVal;
            }
        }
    }

    //y+ face
    neighborIndex = CK(1,2,1);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / ((DIM-2)*(DIM-2));
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(    i, DIM-1,     j)] = arrays[neighborIndex][IX(     i,     1,     j)] + adjacentVal;
            }
        }
    } else {
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(    i, DIM-1,     j)] = fillVal;
            }
        }
    }

    //y- face
    neighborIndex = CK(1,0,1);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / ((DIM-2)*(DIM-2));
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(i,     0,     j)] = arrays[neighborIndex][IX(     i,     DIM-2,     j)] + adjacentVal;
            }
        }
    } else {
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(i,     0,     j)] = fillVal;
            }
        }
    }

    //z+ face
    neighborIndex = CK(1,1,2);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / ((DIM-2)*(DIM-2));
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(    i,     j, DIM-1)] = arrays[neighborIndex][IX(     i,     j,     1)] + adjacentVal;
            }
        }
    } else {
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(    i,     j, DIM-1)] = fillVal;
            }
        }
    }

    //z- face
    neighborIndex = CK(1,1,0);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / ((DIM-2)*(DIM-2));
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(i,     j,     0)] = arrays[neighborIndex][IX(     i,     j,     DIM-2)] + adjacentVal;
            }
        }
    } else {
        for(i = 1; i < DIM-1; i++){
            for(j = 1; j < DIM-1; j++){
                arrays[CENTER_LOC][IX(i,     j,     0)] = fillVal;
            }
        }
    }


    //
    // edges
    //

    //x+ y+ edge
    neighborIndex = CK(2,2,1);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / (DIM-2);
        for(i = 1; i < DIM-1; i++){
            arrays[CENTER_LOC][IX(DIM-1, DIM-1,     i)] = arrays[neighborIndex][IX(     1,     1,     i)] + adjacentVal;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(DIM-1, DIM-1,     i)] = fillVal;
        }
    }

    //x+ y- edge
    neighborIndex = CK(2,0,1);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / (DIM-2);
        for(i = 1; i < DIM-1; i++){
            arrays[CENTER_LOC][IX(DIM-1, 0,     i)] = arrays[neighborIndex][IX(     1,     DIM-2,     i)] + adjacentVal;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(DIM-1, 0,     i)] = fillVal;
        }
    }

    //x- y+ edge
    neighborIndex = CK(0,2,1);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / (DIM-2);
        for(i = 1; i < DIM-1; i++){
            arrays[CENTER_LOC][IX(     0, DIM-1,     i)] = arrays[neighborIndex][IX(     DIM-2,     1,     i)] + adjacentVal;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(      0, DIM-1,     i)] = fillVal;
        }
    }

    //x- y- edge
    neighborIndex = CK(0,0,1);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / (DIM-2);
        for(i = 1; i < DIM-1; i++){
            arrays[CENTER_LOC][IX(    0, 0,     i)] = arrays[neighborIndex][IX(   DIM-2,     DIM-2,     i)] + adjacentVal;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(    0, 0,     i)] = fillVal;
        }
    }









    //x+ z+ edge
    neighborIndex = CK(2,1,2);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / (DIM-2);
        for(i = 1; i < DIM-1; i++){
            arrays[CENTER_LOC][IX(DIM-1,    i, DIM-1)] = arrays[neighborIndex][IX(     1,    i,     1)] + adjacentVal;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(DIM-1,    i, DIM-1)] = fillVal;
        }
    }

    //x+ z- edge
    neighborIndex = CK(2,1,0);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / (DIM-2);
        for(i = 1; i < DIM-1; i++){
            arrays[CENTER_LOC][IX(DIM-1,    i, 0)] = arrays[neighborIndex][IX(     1,    i,     DIM-2)] + adjacentVal;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(DIM-1,    i, 0)] = fillVal;
        }
    }

    //x- z+ edge
    neighborIndex = CK(0,1,2);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / (DIM-2);
        for(i = 1; i < DIM-1; i++){
            arrays[CENTER_LOC][IX(     0,    i, DIM-1)] = arrays[neighborIndex][IX(     DIM-2,    i,     1)] + adjacentVal;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(      0,    i, DIM-1)] = fillVal;
        }
    }

    //x- z- edge
    neighborIndex = CK(0,1,0);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / (DIM-2);
        for(i = 1; i < DIM-1; i++){
            arrays[CENTER_LOC][IX(    0,    i, 0)] = arrays[neighborIndex][IX(   DIM-2,    i,     DIM-2)] + adjacentVal;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(    0,    i, 0)] = fillVal;
        }
    }








    //y+ z+ edge
    neighborIndex = CK(1,2,2);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / (DIM-2);
        for(i = 1; i < DIM-1; i++){
            arrays[CENTER_LOC][IX(    i, DIM-1,    DIM-1)] = arrays[neighborIndex][IX(    i,     1,     1)] + adjacentVal;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(    i, DIM-1, DIM-1)] = fillVal;
        }
    }

    //y+ z- edge
    neighborIndex = CK(1,2,0);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / (DIM-2);
        for(i = 1; i < DIM-1; i++){
            arrays[CENTER_LOC][IX(    i,DIM-1, 0)] = arrays[neighborIndex][IX(    i,     1,     DIM-2)] + adjacentVal;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(    i,DIM-1, 0)] = fillVal;
        }
    }

    //y- z+ edge
    neighborIndex = CK(1,0,2);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / (DIM-2);
        for(i = 1; i < DIM-1; i++){
            arrays[CENTER_LOC][IX(    i,     0, DIM-1)] = arrays[neighborIndex][IX(    i,     DIM-2,    1)] + adjacentVal;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(    i,      0, DIM-1)] = fillVal;
        }
    }

    //y- z- edge
    neighborIndex = CK(1,0,0);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        adjacentVal = adjacentVal / (DIM-2);
        for(i = 1; i < DIM-1; i++){
            arrays[CENTER_LOC][IX(    i,    0, 0)] = arrays[neighborIndex][IX(    i,   DIM-2,     DIM-2)] + adjacentVal;
        }
    } else {
        for(i = 1; i < DIM; i++){
            arrays[CENTER_LOC][IX(    i,    0, 0)] = fillVal;
        }
    }


    //
    //  CORNERS
    //

    //x+ y+ z+ corner
    neighborIndex = CK(2,2,2);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        arrays[CENTER_LOC][IX(DIM-1, DIM-1, DIM-1)] = arrays[neighborIndex][IX(     1,     1,     1)] + adjacentVal;
    } else {
        arrays[CENTER_LOC][IX(DIM-1, DIM-1, DIM-1)] = fillVal;
    }

    //x+ y+ z- corner
    neighborIndex = CK(2,2,0);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        arrays[CENTER_LOC][IX(DIM-1, DIM-1,     0)] = arrays[neighborIndex][IX(     1,     1, DIM-2)] + adjacentVal;
    } else {
        arrays[CENTER_LOC][IX(DIM-1, DIM-1,     0)] = fillVal;
    }



    //x+ y- z+ corner
    neighborIndex = CK(2,0,2);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        arrays[CENTER_LOC][IX(DIM-1, 0, DIM-1)] = arrays[neighborIndex][IX(     1, DIM-2,     1)] + adjacentVal;
    } else {
        arrays[CENTER_LOC][IX(DIM-1, 0, DIM-1)] = fillVal;
    }

    //x+ y- z- corner
    neighborIndex = CK(2,0,0);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        arrays[CENTER_LOC][IX(DIM-1, 0,     0)] = arrays[neighborIndex][IX(     1, DIM-2, DIM-2)] + adjacentVal;
    } else {
        arrays[CENTER_LOC][IX(DIM-1, 0,     0)] = fillVal;
    }



    //x- y+ z+ corner
    neighborIndex = CK(0,2,2);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        arrays[CENTER_LOC][IX(0, DIM-1, DIM-1)] = arrays[neighborIndex][IX( DIM-2,     1,     1)] + adjacentVal;
    } else {
        arrays[CENTER_LOC][IX(0, DIM-1, DIM-1)] = fillVal;
    }

    //x- y+ z- corner
    neighborIndex = CK(0,2,0);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        arrays[CENTER_LOC][IX(0, DIM-1,     0)] = arrays[neighborIndex][IX( DIM-2,     1, DIM-2)] + adjacentVal;
    } else {
        arrays[CENTER_LOC][IX(0, DIM-1,     0)] = fillVal;
    }



    //x- y- z+ corner
    neighborIndex = CK(0,0,2);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        arrays[CENTER_LOC][IX(0, 0, DIM-1)] = arrays[neighborIndex][IX( DIM-2, DIM-2,     1)] + adjacentVal;
    } else {
        arrays[CENTER_LOC][IX(0, 0, DIM-1)] = fillVal;
    }

    //x- y- z- corner
    neighborIndex = CK(0,0,0);
    if(USE_BOUNDS && arrays[neighborIndex] != NULL){
        adjacentVal = adjacencyMatrix == NULL ? 0 : adjacencyMatrix[neighborIndex];
        arrays[CENTER_LOC][IX(0, 0,     0)] = arrays[neighborIndex][IX( DIM-2, DIM-2, DIM-2)] + adjacentVal;
    } else {
        arrays[CENTER_LOC][IX(0, 0,     0)] = fillVal;
    }
}

/**
 * Sets the boundary values for each chunk
 * @param numReadIn The number of chunks
 * @param chunkViewC The array of chunks
 * @param environment The environment storing the simulation queues
 */
LIBRARY_API void fluid_solve_bounds(int numReadIn, Chunk ** chunkViewC, Environment * environment){
    int i, j, k;
    int chunkIndex;
    int index;
    int neighborIndex;
    for(chunkIndex = 0; chunkIndex < numReadIn; chunkIndex++){
        Chunk * current = chunkViewC[chunkIndex];
        fluid_solve_bounds_checker(current->d,0,current->incomingDensity);
        fluid_solve_bounds_checker(current->d0,0,NULL);
        fluid_solve_bounds_checker(current->u,0,NULL);
        fluid_solve_bounds_checker(current->v,0,NULL);
        fluid_solve_bounds_checker(current->w,0,NULL);
        fluid_solve_bounds_checker(current->u0,0,NULL);
        fluid_solve_bounds_checker(current->v0,0,NULL);
        fluid_solve_bounds_checker(current->w0,0,NULL);
        fluid_solve_bounds_checker(current->bounds,BOUND_MAX_VALUE,NULL);
        fluid_solve_bounds_checker(current->divergenceCache,0,NULL);
        fluid_solve_bounds_checker(current->pressureCache,0,current->incomingPressure);
    }
}

/**
 * Sets the bounds of an array to a provided value
 * @param arrays The array to set
 * @param fillVal The value to fill with
 */
LIBRARY_API void fluid_solve_bounds_set_bounds(float * arrays, float fillVal){
    int i, j;
    //x+ face
    for(i = 1; i < DIM; i++){
        for(j = 1; j < DIM; j++){
            arrays[IX(DIM-1,     i,     j)] = fillVal;
        }
    }

    //x- face
    for(i = 1; i < DIM-1; i++){
        for(j = 1; j < DIM-1; j++){
            arrays[IX(0,     i,     j)] = fillVal;
        }
    }

    //y+ face
    for(i = 1; i < DIM-1; i++){
        for(j = 1; j < DIM-1; j++){
            arrays[IX(    i, DIM-1,     j)] = fillVal;
        }
    }

    //y- face
    for(i = 1; i < DIM-1; i++){
        for(j = 1; j < DIM-1; j++){
            arrays[IX(i,     0,     j)] = fillVal;
        }
    }

    //z+ face
    for(i = 1; i < DIM-1; i++){
        for(j = 1; j < DIM-1; j++){
            arrays[IX(    i,     j, DIM-1)] = fillVal;
        }
    }

    //z- face
    for(i = 1; i < DIM-1; i++){
        for(j = 1; j < DIM-1; j++){
            arrays[IX(i,     j,     0)] = fillVal;
        }
    }


    //
    // edges
    //

    //x+ y+ edge
    for(i = 1; i < DIM; i++){
        arrays[IX(DIM-1, DIM-1,     i)] = fillVal;
    }

    //x+ y- edge
    for(i = 1; i < DIM; i++){
        arrays[IX(DIM-1, 0,     i)] = fillVal;
    }

    //x- y+ edge
    for(i = 1; i < DIM; i++){
        arrays[IX(      0, DIM-1,     i)] = fillVal;
    }

    //x- y- edge
    for(i = 1; i < DIM; i++){
        arrays[IX(    0, 0,     i)] = fillVal;
    }









    //x+ z+ edge
    for(i = 1; i < DIM; i++){
        arrays[IX(DIM-1,    i, DIM-1)] = fillVal;
    }

    //x+ z- edge
    for(i = 1; i < DIM; i++){
        arrays[IX(DIM-1,    i, 0)] = fillVal;
    }

    //x- z+ edge
    for(i = 1; i < DIM; i++){
        arrays[IX(      0,    i, DIM-1)] = fillVal;
    }

    //x- z- edge
    for(i = 1; i < DIM; i++){
        arrays[IX(    0,    i, 0)] = fillVal;
    }








    //y+ z+ edge
    for(i = 1; i < DIM; i++){
        arrays[IX(    i, DIM-1, DIM-1)] = fillVal;
    }

    //y+ z- edge
    for(i = 1; i < DIM; i++){
        arrays[IX(    i,DIM-1, 0)] = fillVal;
    }

    //y- z+ edge
    for(i = 1; i < DIM; i++){
        arrays[IX(    i,      0, DIM-1)] = fillVal;
    }

    //y- z- edge
    for(i = 1; i < DIM; i++){
        arrays[IX(    i,    0, 0)] = fillVal;
    }


    //
    //  CORNERS
    //

    //x+ y+ z+ corner
    arrays[IX(DIM-1, DIM-1, DIM-1)] = fillVal;

    //x+ y+ z- corner
    arrays[IX(DIM-1, DIM-1,     0)] = fillVal;



    //x+ y- z+ corner
    arrays[IX(DIM-1, 0, DIM-1)] = fillVal;

    //x+ y- z- corner
    arrays[IX(DIM-1, 0,     0)] = fillVal;



    //x- y+ z+ corner
    arrays[IX(0, DIM-1, DIM-1)] = fillVal;

    //x- y+ z- corner
    arrays[IX(0, DIM-1,     0)] = fillVal;



    //x- y- z+ corner
    arrays[IX(0, 0, DIM-1)] = fillVal;

    //x- y- z- corner
    arrays[IX(0, 0,     0)] = fillVal;
}


