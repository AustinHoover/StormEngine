#include <stdio.h>
#include <stdlib.h>

#include "fluid/queue/chunk.h"
#include "fluid/queue/sparse.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/utilities.h"
#include "../../util/test.h"




int fluid_chunk_tests(int argc, char **argv){
    int rVal = 0;
    //allocate a sparse array
    SparseChunkArray * sparseArray = fluid_sparse_array_create();
    if(sparseArray == NULL){
        printf("Failed to allocate sparseArray!\n");
        return 1;
    }

    //create chunks to add to the sparse array
    Chunk * chunk1 = (Chunk *)malloc(sizeof(Chunk));
    chunk1->d[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk1->d0[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk1->u[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk1->v[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk1->w[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk1->u0[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk1->v0[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk1->w0[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    for(int x = 0; x < DIM; x++){
        for(int y = 0; y < DIM; y++){
            for(int z = 0; z < DIM; z++){
                chunk1->d[CENTER_LOC][x * DIM * DIM + y * DIM + z] = 1;
            }
        }
    }
    Chunk * chunk2 = (Chunk *)malloc(sizeof(Chunk));
    chunk2->d[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk2->d0[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk2->u[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk2->v[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk2->w[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk2->u0[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk2->v0[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    chunk2->w0[CENTER_LOC] = (float *)malloc(DIM * DIM * DIM * sizeof(float));
    for(int x = 0; x < DIM; x++){
        for(int y = 0; y < DIM; y++){
            for(int z = 0; z < DIM; z++){
                chunk2->d[CENTER_LOC][x * DIM * DIM + y * DIM + z] = 2;
            }
        }
    }

    //add a chunk
    fluid_sparse_array_add_chunk(sparseArray,chunk1,0,0,0);
    rVal += assertEquals(sparseArray->d[fluid_sparse_array_get_index(sparseArray,0,0,0)],0,"index 0,0,0 in the sparse array should be a border value -- %d %d\n");
    rVal += assertEquals(chunk1->d[CENTER_LOC][IX(1,1,1)],1,"chunk1 should have a value of 1 -- %d %d\n");
    rVal += assertEquals(sparseArray->d[fluid_sparse_array_get_index(sparseArray,1,1,1)],chunk1->d[CENTER_LOC][IX(1,1,1)],"index 1,1,1 in the sparse array should be 1,1,1 of the chunk stored at 0,0,0 -- %d %d\n");

    //make sure adding a second chunk doesn't override the first one
    fluid_sparse_array_add_chunk(sparseArray,chunk2,1,0,0);
    rVal += assertEquals(sparseArray->d[fluid_sparse_array_get_index(sparseArray,0,0,0)],0,"index 0,0,0 in the sparse array should be a border value -- %d %d\n");
    rVal += assertEquals(sparseArray->d[fluid_sparse_array_get_index(sparseArray,1,1,1)],chunk1->d[CENTER_LOC][IX(1,1,1)],"index 1,1,1 in the sparse array should be 0,0,0 of the chunk stored at 0,0,0 -- %d %d\n");
    rVal += assertEquals(sparseArray->d[fluid_sparse_array_get_index(sparseArray,16,16,16)],chunk1->d[CENTER_LOC][IX(15,15,15)],"index 16,16,16 in the sparse array should be 15,15,15 of the chunk stored at 0,0,0 -- %d %d\n");
    rVal += assertEquals(sparseArray->d[fluid_sparse_array_get_index(sparseArray,17,1,1)],chunk2->d[CENTER_LOC][IX(1,1,1)],"index 17,1,1 in the sparse array should be 1,1,1 of the chunk stored at 1,0,0 -- %d %d\n");

    //validate fluid_sparse_array_get_chunk_count
    rVal += assertEquals(fluid_sparse_array_get_chunk_count(sparseArray),2,"Sparse array not counting chunks correctly -- %d %d \n");


    //set some value in sparse array for future tests
    sparseArray->d[fluid_sparse_array_get_index(sparseArray,17,1,1)] = 6;
    sparseArray->d[fluid_sparse_array_get_index(sparseArray,1,1,1)] = 6;

    //check zeroing out behavior of removing chunks
    fluid_sparse_array_remove_chunk(sparseArray,chunk2,1,0,0);
    rVal += assertEquals(sparseArray->d[fluid_sparse_array_get_index(sparseArray,17,1,1)],0,"index 17,1,1 in the sparse array should be 1,1,1 of the chunk stored at 1,0,0 -- %d %d\n");

    //check no-copying behavior of removing chunks
    rVal += assertNotEquals(chunk2->d[CENTER_LOC][IX(1,1,1)],6,"chunk should NOT have received the value from the sparse array -- %d %d \n");

    //check no-copying behavior of removing chunks
    fluid_sparse_array_remove_chunk(sparseArray,chunk1,0,0,0);
    rVal += assertNotEquals(chunk1->d[CENTER_LOC][IX(1,1,1)],6,"chunk should have received the value from the sparse array -- %d %d \n");

    //clean the array and make sure it's clean
    fluid_sparse_array_clean(sparseArray);
    rVal += assertEquals(sparseArray->d[fluid_sparse_array_get_index(sparseArray,1,1,1)],0,"Array should have been cleaned -- %d %d \n");


    //free a sparse array
    fluid_sparse_array_free(sparseArray);

    return rVal;
}

/**
 * Empty test launcher
 */
int fluid_queue_sparse_tests(){
    return 0;
}