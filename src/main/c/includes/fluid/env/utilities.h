#include <stdint.h>

#include "fluid/queue/chunk.h"

#ifndef UTILITIES_H
#define UTILITIES_H

#define SWAP(x0,x) {float *tmp=x0;x0=x;x=tmp;}
#define IX(i,j,k) ((i)+(DIM)*(j)+(DIM*DIM)*(k))
#define CK(m,n,o) ((m)+(n)*(3)+(o)*(3)*(3))
#define GET_ARR_RAW(src,i) src[i]
#define ARR_EXISTS(chunk_mask,m,n,o) (chunk_mask & CHUNK_INDEX_ARR[CK(m,n,o)]) > 0

#endif