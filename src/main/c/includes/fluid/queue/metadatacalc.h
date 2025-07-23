#ifndef METADATACALC
#define METADATACALC

#include<jni.h>

#include "public.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"


/**
 * Updates the metadata for all chunks
 * @param env The java environment variable
 * @param numChunks The number of chunks
 * @param passedInChunks The chunks that were passed in
 * @param environment The environment data
 */
LIBRARY_API void updateMetadata(JNIEnv * env, int numChunks, Chunk ** passedInChunks, Environment * environment);



#endif