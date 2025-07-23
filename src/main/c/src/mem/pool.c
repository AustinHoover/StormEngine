#include<stdlib.h>
#include <stdio.h>

//Libraries
#include "../../lib/stb/stb_ds.h"

#include "../../includes/mem/pool.h"


/**
 * Creates a memory pool
 * @param blockSize The size of a block within the pool
 * @return The pool
 */
POOL * pool_create(int blockSize){
    POOL * p = (POOL*)calloc(1,sizeof(POOL) );
    p->table = NULL;
    p->blockSize = blockSize;
    p->posCurr = 0;
    p->posMax = 0;
    return p;
}

/**
 * Gets a block from the pool
 * @param pool The pool to pull from
 * @return The block of memory
 */
void * pool_get(POOL * pool){
    void * rVal;
    pool->posCurr++;
    if(pool->posCurr >= pool->posMax){
        //allocate a new block
        void * newBlock = (void *)calloc(1,pool->blockSize);
        if(newBlock == NULL){
            pool->posCurr--;
            return NULL;
        }
        stbds_arrput(pool->table,newBlock);
        pool->posMax++;
    }
    rVal = pool->table[pool->posCurr - 1];
    return rVal;
}

/**
 * Destroys the pool
 * @param p The pool to destroy
 */
void pool_destroy(POOL * p){
    for(int i = 0; i < p->posMax; i++){
        free(p->table[i]);
    }
    free(p->table);
    free(p);
}

/**
 * Returns the block at index
 * @param pool The pool to return memory to
 * @param block The block to return
 */
void pool_return(POOL * pool, void * block){
    int returnedIndex = 0;
    for(int i = 0; i < pool->posCurr; i++){
        if(block == pool->table[i]){
            returnedIndex = i;
            break;
        }
    }
    void * returnedBlock = pool->table[returnedIndex];
    pool->table[returnedIndex] = pool->table[pool->posCurr - 1];
    pool->table[pool->posCurr] = returnedBlock;
    pool->posCurr--;
}