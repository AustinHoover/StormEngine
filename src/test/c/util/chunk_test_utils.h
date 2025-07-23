
#ifndef CHUNK_TEST_UTILS_H
#define CHUNK_TEST_UTILS_H

#include "fluid/env/environment.h"
#include "fluid/queue/chunk.h"


/**
 * A very small value
 */
#define CHUNK_TEST_UTILS_SMALL_VALUE 0.00001f

/**
 * Array id of the d array for chunk_set_val
 */
#define ARR_ID_D 0

/**
 * Array id of the d0 array for chunk_set_val
 */
#define ARR_ID_D0 1

/**
 * Array id of the u array for chunk_set_val
 */
#define ARR_ID_U 2

/**
 * Array id of the v array for chunk_set_val
 */
#define ARR_ID_V 3

/**
 * Array id of the w array for chunk_set_val
 */
#define ARR_ID_W 4

/**
 * Array id of the u0 array for chunk_set_val
 */
#define ARR_ID_U0 5

/**
 * Array id of the v0 array for chunk_set_val
 */
#define ARR_ID_V0 6

/**
 * Array id of the w0 array for chunk_set_val
 */
#define ARR_ID_W0 7



/**
 * Creates a chunk at a world position
 */
Chunk * chunk_create_pos(int x, int y, int z);

/**
 * Frees a chunk
 */
void chunk_free(Chunk * chunk);

/**
 * Sets a chunk's value
 * @param chunk The chunk
 * @param i The value to set the array to
 * @param arr THe array to set
 */
void chunk_set_val(Chunk * chunk, int i, int arr);

/**
 * Creates a chunk queue
 * @param size The size of the queue to create
 */
Chunk ** chunk_create_queue(int size);

/**
 * Fills a chunk with a value
 * @param chunk The chunk to fill
 * @param val The value to fill
 */
void chunk_fill(Chunk * chunk, float val);

/**
 * Fills a chunk with a value
 * @param chunk The chunk to fill
 * @param val The value to fill
 */
void chunk_fill_real(float * arr, float val);

/**
 * Frees a chunk queue
 */
void chunk_free_queue(Chunk ** chunks);

/**
 * Links all neighbors in a chunk queue
 */
void chunk_link_neighbors(Chunk ** chunks);

/**
 * Sums density all chunks in a queue
 */
float chunk_queue_sum_density(Chunk ** chunks);

/**
 * Sums the density of a chunk including its border values
 */
float chunk_sum_density_with_borders(Chunk * chunk);

/**
 * Sums the density of a chunk
 */
float chunk_sum_density(Chunk * chunk);

/**
 * Sums velocity in all chunks in a queue
 */
float chunk_queue_sum_velocity(Chunk ** chunks, int axis);

/**
 * Creates a grid of chunks with the specified dimensions
 * @param env The simulation environment
 * @param width The width of the grid in number of chunks
 * @param height The height of the grid in number of chunks
 * @param length The length of the grid in number of chunks
 * @return The list of chunks
 */
Chunk ** createChunkGrid(Environment * env, int width, int height, int length);

/**
 * Creates a convection cell for testing advection
 */
void advection_setup_convection_cell(Chunk ** queue, int center);

#endif