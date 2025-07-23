#include <stdint.h>
#include <jni.h>

#include "fluid/env/utilities.h"
#include "fluid/queue/chunkmask.h"

const uint32_t CHUNK_INDEX_ARR[] = {
    CHUNK_000, CHUNK_100, CHUNK_200,
    CHUNK_010, CHUNK_110, CHUNK_210,
    CHUNK_020, CHUNK_120, CHUNK_220,

    CHUNK_001, CHUNK_101, CHUNK_201,
    CHUNK_011, CHUNK_111, CHUNK_211,
    CHUNK_021, CHUNK_121, CHUNK_221,

    CHUNK_002, CHUNK_102, CHUNK_202,
    CHUNK_012, CHUNK_112, CHUNK_212,
    CHUNK_022, CHUNK_122, CHUNK_222,
};


//control offsetting the advect sampler location if a valid neighbor chunk is hit
const char CHUNK_NORMALIZE_U[] = {
    1, 0, -1,
    1, 0, -1,
    1, 0, -1,

    1, 0, -1,
    1, 0, -1,
    1, 0, -1,

    1, 0, -1,
    1, 0, -1,
    1, 0, -1,
};

const char CHUNK_NORMALIZE_V[] = {
     1,  1,  1,
     0,  0,  0,
    -1, -1, -1,

    1,  1,  1,
     0,  0,  0,
    -1, -1, -1,

    1,  1,  1,
     0,  0,  0,
    -1, -1, -1,
};

const char CHUNK_NORMALIZE_W[] = {
    1, 1, 1,
    1, 1, 1,
    1, 1, 1,

    0, 0, 0,
    0, 0, 0,
    0, 0, 0,

    -1, -1, -1,
    -1, -1, -1,
    -1, -1, -1,
};




/**
 * Calculates a mask that represents all nearby chunks that are actually accessible and exist
*/
uint32_t matrix_transform(JNIEnv * env, jobjectArray jrx){

    //The returned value, an availability mask that contains the availability of each neighbor chunk
    uint32_t rVal = 0;

    //Add to maks for initial chunks
    for(int i = 0; i < CENTER_LOC; i++){
        if((*env)->GetObjectArrayElement(env,jrx,i)!=NULL){
            rVal = rVal + 1;
        }
        rVal = rVal << 1;
    }
    //add 1 for center chunk because we already have that
    rVal = rVal + 1;
    rVal = rVal << 1;
    //continue on for remaining chunks
    for(int i = CENTER_LOC+1; i < 27; i++){
        if((*env)->GetObjectArrayElement(env,jrx,i)!=NULL){
            rVal = rVal + 1;
        }
        if(i < 26){
            rVal = rVal << 1;
        }
    }

    return rVal;
}



/**
 * Calculates the bitmask for available chunks for the provided chunk's neighbor array
*/
LIBRARY_API uint32_t calculateChunkMask(JNIEnv * env, jobjectArray jrx){
    return matrix_transform(env,jrx);
}

