#ifndef POOL_H
#define POOL_H

/**
 * A pool of memory blocks
 */
typedef struct pool {
    /**
     * The current number of utilized blocks within the pool
     */
    int posCurr;

    /**
     * The maximum number of blocks currently stored in the pool
     */
    int posMax;

    /**
     * The size of a block within the pool
     */
    int blockSize;

    /**
     * The table of blocks itself
     */
    void ** table;


} POOL;

/**
 * Creates a memory pool
 * @param blockSize The size of a block within the pool
 * @return The pool
 */
POOL * pool_create(int blockSize);

/**
 * Gets a block from the pool
 * @param pool The pool to pull from
 * @return The block of memory
 */
void * pool_get(POOL * pool);

/**
 * Destroys the pool
 * @param p The pool to destroy
 */
void pool_destroy(POOL * p);

/**
 * Returns the block at index
 * @param pool The pool to return memory to
 * @param block The block to return
 */
void pool_return(POOL * pool, void * block);

#endif