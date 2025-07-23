#ifndef CHUNKMASK_H
#define CHUNKMASK_H

#include <stdint.h>

/**
 * The number of entries in the neighbor array
 */
#define NEIGHBOR_ARRAY_COUNT 27

#define CENTER_LOC 13

#define CHUNK_222 1
#define CHUNK_122 2
#define CHUNK_022 4
#define CHUNK_212 8
#define CHUNK_112 16
#define CHUNK_012 32
#define CHUNK_202 64
#define CHUNK_102 128
#define CHUNK_002 256

#define CHUNK_221 512
#define CHUNK_121 1024
#define CHUNK_021 2048
#define CHUNK_211 4096
#define CHUNK_111 8192
#define CHUNK_011 16384
#define CHUNK_201 32768
#define CHUNK_101 65536
#define CHUNK_001 131072

#define CHUNK_220 262144
#define CHUNK_120 524288
#define CHUNK_020 1048576
#define CHUNK_210 2097152
#define CHUNK_110 4194304
#define CHUNK_010 8388608
#define CHUNK_200 16777216
#define CHUNK_100 33554432
#define CHUNK_000 67108864

extern const uint32_t CHUNK_INDEX_ARR[];


//control offsetting the advect sampler location if a valid neighbor chunk is hit
extern const char CHUNK_NORMALIZE_U[];

extern const char CHUNK_NORMALIZE_V[];

extern const char CHUNK_NORMALIZE_W[];

/**
 * Calculates the bitmask for available chunks for the provided chunk's neighbor array
*/
LIBRARY_API uint32_t calculateChunkMask(JNIEnv * env, jobjectArray jrx);

#endif