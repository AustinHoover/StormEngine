#include <stdint.h>
#include "fluid/env/environment.h"

#ifndef MAINFUNC
#define MAINFUNC


/*
 * Class:     electrosphere_FluidSim
 * Method:    addSourceToVectors
 * Signature: (II[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;FFF)V
 */
void addSourceToVectors
  (int, int, float **, float **, float **, float **, float **, float **, float, float, float);

/*
 * Class:     electrosphere_FluidSim
 * Method:    solveVectorDiffuse
 * Signature: (II[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;FFF)V
 */
void solveVectorDiffuse
  (int, int, float **, float **, float **, float **, float **, float **, float, float, float);

/*
 * Class:     electrosphere_FluidSim
 * Method:    setupProjection
 * Signature: (II[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;FFF)V
 */
void setupProjection
  (
  int N,
  int chunk_mask,
  float ** ur,
  float ** vr,
  float ** wr,
  float ** pr,
  float ** divr,
  float DIFFUSION_CONST,
  float VISCOSITY_CONST,
  float dt);

/*
 * Class:     electrosphere_FluidSim
 * Method:    solveProjection
 * Signature: (II[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;FFF)V
 */
void solveProjection
  (int, int, float **, float **, float **, float **, float **, float **, float, float, float);

/*
 * Class:     electrosphere_FluidSim
 * Method:    finalizeProjection
 * Signature: (II[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;FFF)V
 */
void finalizeProjection
  (int, int,  float **, float **, float **, float **, float **, float **, float, float, float);

/*
 * Class:     electrosphere_FluidSim
 * Method:    advectVectors
 * Signature: (II[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;FFF)V
 */
void advectVectors
  (int, int, float **, float **, float **, float **, float **, float **, float, float, float);

/**
 * Adds density to the density array
 * @return The change in density within this chunk for this frame
*/
void addDensity(Environment * environment, int, int, float **, float **, float);

/*
 * Class:     electrosphere_FluidSim
 * Method:    solveDiffuseDensity
 * Signature: (II[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;FFF)V
 */
void solveDiffuseDensity
  (int, int, float **, float **, float **, float **, float **, float, float, float);

/*
 * Class:     electrosphere_FluidSim
 * Method:    advectDensity
 * Signature: (II[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;[Ljava/nio/ByteBuffer;FFF)V
 */
void advectDensity(uint32_t chunk_mask, int N, float ** d, float ** d0, float ** ur, float ** vr, float ** wr, float dt);


/**
 * Sums the density of the chunk
 */
double calculateSum(uint32_t chunk_mask, int N, float ** d);

/**
 * Normalizes the density array with a given ratio
 */
void normalizeDensity(int N, float ** d, float ratio);

void setBoundsToNeighborsRaw
  (
  int N,
  int chunk_mask,
  int vector_dir,
  float ** neighborArray);

void copyNeighborsRaw
  (
  int N,
  int chunk_mask,
  int cx,
  int vector_dir,
  float ** neighborArray);

#endif