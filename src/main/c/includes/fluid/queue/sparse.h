#ifndef FLUID_QUEUE_SPARSE_H
#define FLUID_QUEUE_SPARSE_H

//Must be included for public functions to be imported/exported on windows
#include "public.h"

#include "chunk.h"


/**
 * The size of the main array's real data
 */
#define MAIN_ARRAY_DIM 16

/**
 * The number of chunks to place next to one another
 */
#define SPARSE_ARRAY_CHUNK_DIM 7

/**
 * The number of chunks to place next to one another
 */
#define SPARSE_ARRAY_CHUNK_RADIUS 3

/**
 * The total number of chunks in the chunk tracking array
 */
#define SPARSE_ARRAY_TOTAL_CHUNKS (SPARSE_ARRAY_CHUNK_DIM * SPARSE_ARRAY_CHUNK_DIM * SPARSE_ARRAY_CHUNK_DIM)

/**
 * The extra values at the edges of the sparse array (to allow pulling data from borders)
 */
#define SPARSE_ARRAY_BORDER_SIZE 2

/**
 * The size of the real data in the sparse array (not including bounds)
 */
#define SPARSE_ARRAY_REAL_DATA_SIZE (SPARSE_ARRAY_CHUNK_DIM * MAIN_ARRAY_DIM)

/**
 * Size of the cells we care about in the sparse array
 */
#define SPARSE_ARRAY_ACTUAL_DATA_DIM (SPARSE_ARRAY_REAL_DATA_SIZE + SPARSE_ARRAY_BORDER_SIZE)

/**
 * The size of the dimension of the memory of the sparse array
 * While the SPARSE_ARRAY_ACTUAL_DATA_DIM may come out to 114, it will be more efficient CPU-instruction wise
 * if we round that up to 128+2
 * That way we can call highly vectorized functions (ie avx128 instead of avx64+avx32+avx16+2)
 */
#define SPARSE_ARRAY_RAW_DIM (128 + SPARSE_ARRAY_BORDER_SIZE)

/**
 * The size of a sparse array in number of elements
 */
#define SPARSE_ARRAY_FULL_SIZE (SPARSE_ARRAY_RAW_DIM * SPARSE_ARRAY_RAW_DIM * SPARSE_ARRAY_RAW_DIM)

/**
 * The unit of spacial distance
 */
#define SPATIAL_UNIT 1

/**
 * The size of the sparse array in spatial units
 */
#define SPARSE_ARRAY_SPATIAL_SIZE (SPARSE_ARRAY_ACTUAL_DATA_DIM * SPATIAL_UNIT)

/**
 * A set of sparse matricies for simulating fluids
 */
typedef struct {

    /**
     * The density array
     */
    float * d;

    /**
     * The density delta array
     */
    float * d0;

    /**
     * The x velocity array
     */
    float * u;

    /**
     * The y velocity array
     */
    float * v;
    
    /**
     * The z velocity array
     */
    float * w;

    /**
     * The x velocity delta array
     */
    float * u0;

    /**
     * The y velocity delta array
     */
    float * v0;
    
    /**
     * The z velocity delta array
     */
    float * w0;

    /**
     * The chunks inside the array
     */
    Chunk ** chunks;

} SparseChunkArray;


/**
 * Creates a sparse chunk array
 */
LIBRARY_API SparseChunkArray * fluid_sparse_array_create();

/**
 * Frees a sparse chunk array
 */
LIBRARY_API void fluid_sparse_array_free(SparseChunkArray * array);

/**
 * Adds a chunk to the sparse array
 */
LIBRARY_API void fluid_sparse_array_add_chunk(SparseChunkArray * array, Chunk * chunk, int x, int y, int z);

/**
 * Adds a chunk to the sparse array
 */
LIBRARY_API void fluid_sparse_array_remove_chunk(SparseChunkArray * array, Chunk * chunk, int x, int y, int z);

/**
 * Gets the index for a point in the chunk
 */
LIBRARY_API int fluid_sparse_array_get_index(SparseChunkArray * array, int x, int y, int z);

/**
 * Cleans the sparse array
 */
LIBRARY_API void fluid_sparse_array_clean(SparseChunkArray * array);

/**
 * Gets the number of chunks in the sparse array
 */
LIBRARY_API int fluid_sparse_array_get_chunk_count(SparseChunkArray * array);

int GCI(int x, int y, int z);
int GVI(int x, int y, int z);


#endif