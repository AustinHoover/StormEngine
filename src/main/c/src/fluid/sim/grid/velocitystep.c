#include <stdio.h>
#include <immintrin.h>
#include <stdint.h>

#include "fluid/env/utilities.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/sim/grid/solver_consts.h"


#define BOUND_NO_DIR 0
#define BOUND_DIR_U 1
#define BOUND_DIR_V 2
#define BOUND_DIR_W 3

#define SET_BOUND_IGNORE 0
#define SET_BOUND_USE_NEIGHBOR 1

void add_source(int N, float * x, float * s, float dt);
void advect(uint32_t chunk_mask, int N, int b, float ** jrd, float ** jrd0, float * u, float * v, float * w, float dt);


/*
 * Adds force to all vectors
 */
void addSourceToVectors
  (
  int N,
  int chunk_mask,
  float ** jru,
  float ** jrv,
  float ** jrw,
  float ** jru0,
  float ** jrv0,
  float ** jrw0,
  float DIFFUSION_CONST,
  float VISCOSITY_CONST,
  float dt){
    add_source(N,GET_ARR_RAW(jru,CENTER_LOC),GET_ARR_RAW(jru0,CENTER_LOC),dt);
    add_source(N,GET_ARR_RAW(jrv,CENTER_LOC),GET_ARR_RAW(jrv0,CENTER_LOC),dt);
    add_source(N,GET_ARR_RAW(jrw,CENTER_LOC),GET_ARR_RAW(jrw0,CENTER_LOC),dt);
}

/**
 * Adds from a source array to a destination array
*/
void add_source(int N, float * x, float * s, float dt){
	int i;
    int size=N*N*N;
	for(i=0; i<size; i++){
        x[i] += dt*s[i];
    }
}

/*
 * Solves vector diffusion along all axis
 */
void solveVectorDiffuse (
  int N,
  int chunk_mask,
  float ** jru,
  float ** jrv,
  float ** jrw,
  float ** jru0,
  float ** jrv0,
  float ** jrw0,
  float DIFFUSION_CONST,
  float VISCOSITY_CONST,
  float dt
){
    float a=dt*VISCOSITY_CONST*N*N*N;
    float c=1+6*a;
    int i, j, k, l, m;
    float * u = GET_ARR_RAW(jru,CENTER_LOC);
    float * v = GET_ARR_RAW(jrv,CENTER_LOC);
    float * w = GET_ARR_RAW(jrw,CENTER_LOC);
    float * u0 = GET_ARR_RAW(jru0,CENTER_LOC);
    float * v0 = GET_ARR_RAW(jrv0,CENTER_LOC);
    float * w0 = GET_ARR_RAW(jrw0,CENTER_LOC);
    
    __m256 aScalar = _mm256_set1_ps(a);
    __m256 cScalar = _mm256_set1_ps(c);

    //transform u direction
    for(k=1; k<N-1; k++){
        for(j=1; j<N-1; j++){
            int n = 0;
            //solve as much as possible vectorized
            for(i = 1; i < N-1; i=i+8){
                __m256 vector = _mm256_loadu_ps(&u[IX(i-1,j,k)]);
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&u[IX(i+1,j,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&u[IX(i,j-1,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&u[IX(i,j+1,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&u[IX(i,j,k-1)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&u[IX(i,j,k+1)]));
                vector = _mm256_mul_ps(vector,aScalar);
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&u0[IX(i,j,k)]));
                vector = _mm256_div_ps(vector,cScalar);
                _mm256_storeu_ps(&u[IX(i,j,k)],vector);
            }
            //If there is any leftover, perform manual solving
            if(i>N-1){
                for(i=i-8; i < N-1; i++){
                    u[IX(i,j,k)] = (u0[IX(i,j,k)] + a*(u[IX(i-1,j,k)]+u[IX(i+1,j,k)]+u[IX(i,j-1,k)]+u[IX(i,j+1,k)]+u[IX(i,j,k-1)]+u[IX(i,j,k+1)]))/c;
                }
            }
        }
    }

    //transform v direction
    for(k=1; k<N-1; k++){
        for(j=1; j<N-1; j++){
            int n = 0;
            //solve as much as possible vectorized
            for(i = 1; i < N-1; i=i+8){
                __m256 vector = _mm256_loadu_ps(&v[IX(i-1,j,k)]);
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&v[IX(i+1,j,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&v[IX(i,j-1,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&v[IX(i,j+1,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&v[IX(i,j,k-1)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&v[IX(i,j,k+1)]));
                vector = _mm256_mul_ps(vector,aScalar);
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&v0[IX(i,j,k)]));
                vector = _mm256_div_ps(vector,cScalar);
                _mm256_storeu_ps(&v[IX(i,j,k)],vector);
            }
            //If there is any leftover, perform manual solving
            if(i>N-1){
                for(i=i-8; i < N-1; i++){
                    v[IX(i,j,k)] = (v0[IX(i,j,k)] + a*(v[IX(i-1,j,k)]+v[IX(i+1,j,k)]+v[IX(i,j-1,k)]+v[IX(i,j+1,k)]+v[IX(i,j,k-1)]+v[IX(i,j,k+1)]))/c;
                }
            }
        }
    }

    //transform w direction
    for(k=1; k<N-1; k++){
        for(j=1; j<N-1; j++){
            int n = 0;
            //solve as much as possible vectorized
            for(i = 1; i < N-1; i=i+8){
                __m256 vector = _mm256_loadu_ps(&w[IX(i-1,j,k)]);
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&w[IX(i+1,j,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&w[IX(i,j-1,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&w[IX(i,j+1,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&w[IX(i,j,k-1)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&w[IX(i,j,k+1)]));
                vector = _mm256_mul_ps(vector,aScalar);
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&w0[IX(i,j,k)]));
                vector = _mm256_div_ps(vector,cScalar);
                _mm256_storeu_ps(&w[IX(i,j,k)],vector);
            }
            //If there is any leftover, perform manual solving
            if(i>N-1){
                for(i=i-8; i < N-1; i++){
                    w[IX(i,j,k)] = (w0[IX(i,j,k)] + a*(w[IX(i-1,j,k)]+w[IX(i+1,j,k)]+w[IX(i,j-1,k)]+w[IX(i,j+1,k)]+w[IX(i,j,k-1)]+w[IX(i,j,k+1)]))/c;
                }
            }
        }
    }
}

/**
 * Sets up a projection system of equations
 * It stores the first derivative of the field in pr, and zeroes out divr.
 * This allows you to calculate the second derivative into divr using the derivative stored in pr.
 * @param N The dimension of the grid
 * @param ur The x velocity grid
 * @param vr The y velocity grid
 * @param wr The z velocity grid
 * @param pr The grid that will contain the first derivative
 * @param divr The grid that will be zeroed out in preparation of the solver
 * @param DIFFUSION_CONST The diffusion constant
 * @param VISCOSITY_CONST The viscosity constant
 * @param dt The timestep for the simulation
 */
void setupProjection(
  int N,
  int chunk_mask,
  float ** ur,
  float ** vr,
  float ** wr,
  float ** pr,
  float ** divr,
  float DIFFUSION_CONST,
  float VISCOSITY_CONST,
  float dt
){
    int i, j, k;

    __m256 nVector = _mm256_set1_ps(N);
    __m256 constScalar = _mm256_set1_ps(-1.0/3.0);
    __m256 zeroVec = _mm256_set1_ps(0);
    __m256 vector, vector2, vector3;

    float * u = GET_ARR_RAW(ur,CENTER_LOC);
    float * v = GET_ARR_RAW(vr,CENTER_LOC);
    float * w = GET_ARR_RAW(wr,CENTER_LOC);

    float * p = GET_ARR_RAW(pr,CENTER_LOC);
    float * div = GET_ARR_RAW(divr,CENTER_LOC);

    float scalar = 1.0/3.0;
    float h = 1.0/N;

    for(k=1; k<N-1; k++){
        for(j=1; j<N-1; j++){
            i = 1;
            //
            //lower
            //
            //first part
            vector = _mm256_loadu_ps(&u[IX(i+1,j,k)]);
            vector = _mm256_sub_ps(vector,_mm256_loadu_ps(&u[IX(i-1,j,k)]));
            vector = _mm256_div_ps(vector,nVector);
            //second part
            vector2 = _mm256_loadu_ps(&v[IX(i,j+1,k)]);
            vector2 = _mm256_sub_ps(vector2,_mm256_loadu_ps(&v[IX(i,j-1,k)]));
            vector2 = _mm256_div_ps(vector2,nVector);
            //third part
            vector3 = _mm256_loadu_ps(&w[IX(i,j,k+1)]);
            vector3 = _mm256_sub_ps(vector3,_mm256_loadu_ps(&w[IX(i,j,k-1)]));
            vector3 = _mm256_div_ps(vector3,nVector);
            //multiply and finalize
            vector = _mm256_add_ps(vector,_mm256_add_ps(vector2,vector3));
            vector = _mm256_mul_ps(vector,constScalar);
            //store
            _mm256_storeu_ps(&div[IX(i,j,k)],vector);
            _mm256_storeu_ps(&p[IX(i,j,k)],zeroVec);
            i = 9;
            //
            //upper
            //
            //first part
            vector = _mm256_loadu_ps(&u[IX(i+1,j,k)]);
            vector = _mm256_sub_ps(vector,_mm256_loadu_ps(&u[IX(i-1,j,k)]));
            vector = _mm256_div_ps(vector,nVector);
            //second part
            vector2 = _mm256_loadu_ps(&v[IX(i,j+1,k)]);
            vector2 = _mm256_sub_ps(vector2,_mm256_loadu_ps(&v[IX(i,j-1,k)]));
            vector2 = _mm256_div_ps(vector2,nVector);
            //third part
            vector3 = _mm256_loadu_ps(&w[IX(i,j,k+1)]);
            vector3 = _mm256_sub_ps(vector3,_mm256_loadu_ps(&w[IX(i,j,k-1)]));
            vector3 = _mm256_div_ps(vector3,nVector);
            //multiply and finalize
            vector = _mm256_add_ps(vector,_mm256_add_ps(vector2,vector3));
            vector = _mm256_mul_ps(vector,constScalar);
            //store
            _mm256_storeu_ps(&div[IX(i,j,k)],vector);
            _mm256_storeu_ps(&p[IX(i,j,k)],zeroVec);
            
        }
    }
}

/**
 * Solves a projection system of equations.
 * This performs a single iteration across a the p grid to approximate the gradient field.
 * @param jru0 The gradient field
 * @param jrv0 The first derivative field
 */
void solveProjection(
  int N,
  int chunk_mask,
  float ** jru,
  float ** jrv,
  float ** jrw,
  float ** jru0,
  float ** jrv0,
  float ** jrw0,
  float DIFFUSION_CONST,
  float VISCOSITY_CONST,
  float dt
){
    int a = 1;
    int c = 6;
    int i, j, k, l, m;
    __m256 aScalar = _mm256_set1_ps(a);
    __m256 cScalar = _mm256_set1_ps(c);

    float * p = GET_ARR_RAW(jru0,CENTER_LOC);
    float * div = GET_ARR_RAW(jrv0,CENTER_LOC);
    // update for each cell
    for(k=1; k<N-1; k++){
        for(j=1; j<N-1; j++){
            int n = 0;
            //solve as much as possible vectorized
            for(i = 1; i < N-1; i=i+8){
                __m256 vector = _mm256_loadu_ps(&p[IX(i-1,j,k)]);
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&p[IX(i+1,j,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&p[IX(i,j-1,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&p[IX(i,j+1,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&p[IX(i,j,k-1)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&p[IX(i,j,k+1)]));
                vector = _mm256_mul_ps(vector,aScalar);
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&div[IX(i,j,k)]));
                vector = _mm256_div_ps(vector,cScalar);
                _mm256_storeu_ps(&p[IX(i,j,k)],vector);
            }
            //If there is any leftover, perform manual solving
            if(i>N-1){
                for(i=i-8; i < N-1; i++){
                    p[IX(i,j,k)] = (div[IX(i,j,k)] + a*(p[IX(i-1,j,k)]+p[IX(i+1,j,k)]+p[IX(i,j-1,k)]+p[IX(i,j+1,k)]+p[IX(i,j,k-1)]+p[IX(i,j,k+1)]))/c;
                }
            }
        }
    }
}

/**
 * Finalizes a projection.
 * This subtracts the difference delta along the approximated gradient field.
 * Thus we are left with an approximately mass-conserved field. 
 */
void finalizeProjection(
  int N,
  int chunk_mask,
  float ** jru,
  float ** jrv,
  float ** jrw,
  float ** jru0,
  float ** jrv0,
  float ** jrw0,
  float DIFFUSION_CONST,
  float VISCOSITY_CONST,
  float dt
){
    int i, j, k;
    __m256 constScalar = _mm256_set1_ps(0.5f*N);
    __m256 vector, vector2, vector3;

    float * u = GET_ARR_RAW(jru,CENTER_LOC);
    float * v = GET_ARR_RAW(jrv,CENTER_LOC);
    float * w = GET_ARR_RAW(jrw,CENTER_LOC);

    float * p = GET_ARR_RAW(jru0,CENTER_LOC);
    float * div = GET_ARR_RAW(jrv0,CENTER_LOC);

    for ( k=1 ; k<N-1 ; k++ ) {
        for ( j=1 ; j<N-1 ; j++ ) {
            //
            //v
            //
            //lower
            vector =  _mm256_loadu_ps(&p[IX(1+1,j,k)]);
            vector2 = _mm256_loadu_ps(&p[IX(1-1,j,k)]);
            vector =  _mm256_sub_ps(vector,vector2);
            vector = _mm256_mul_ps(vector,constScalar);
            vector = _mm256_sub_ps(_mm256_loadu_ps(&u[IX(1,j,k)]),vector);
            _mm256_storeu_ps(&u[IX(1,j,k)],vector);
            //upper
            vector =  _mm256_loadu_ps(&p[IX(9+1,j,k)]);
            vector2 = _mm256_loadu_ps(&p[IX(9-1,j,k)]);
            vector =  _mm256_sub_ps(vector,vector2);
            vector = _mm256_mul_ps(vector,constScalar);
            vector = _mm256_sub_ps(_mm256_loadu_ps(&u[IX(9,j,k)]),vector);
            _mm256_storeu_ps(&u[IX(9,j,k)],vector);
            //
            //v
            //
            //lower
            vector =  _mm256_loadu_ps(&p[IX(1,j+1,k)]);
            vector2 = _mm256_loadu_ps(&p[IX(1,j-1,k)]);
            vector =  _mm256_sub_ps(vector,vector2);
            vector = _mm256_mul_ps(vector,constScalar);
            vector = _mm256_sub_ps(_mm256_loadu_ps(&v[IX(1,j,k)]),vector);
            _mm256_storeu_ps(&v[IX(1,j,k)],vector);
            //upper
            vector =  _mm256_loadu_ps(&p[IX(9,j+1,k)]);
            vector2 = _mm256_loadu_ps(&p[IX(9,j-1,k)]);
            vector =  _mm256_sub_ps(vector,vector2);
            vector = _mm256_mul_ps(vector,constScalar);
            vector = _mm256_sub_ps(_mm256_loadu_ps(&v[IX(9,j,k)]),vector);
            _mm256_storeu_ps(&v[IX(9,j,k)],vector);
            //
            //w
            //
            //lower
            vector =  _mm256_loadu_ps(&p[IX(1,j,k+1)]);
            vector2 = _mm256_loadu_ps(&p[IX(1,j,k-1)]);
            vector =  _mm256_sub_ps(vector,vector2);
            vector = _mm256_mul_ps(vector,constScalar);
            vector = _mm256_sub_ps(_mm256_loadu_ps(&w[IX(1,j,k)]),vector);
            _mm256_storeu_ps(&w[IX(1,j,k)],vector);
            //upper
            vector =  _mm256_loadu_ps(&p[IX(9,j,k+1)]);
            vector2 = _mm256_loadu_ps(&p[IX(9,j,k-1)]);
            vector =  _mm256_sub_ps(vector,vector2);
            vector = _mm256_mul_ps(vector,constScalar);
            vector = _mm256_sub_ps(_mm256_loadu_ps(&w[IX(9,j,k)]),vector);
            _mm256_storeu_ps(&w[IX(9,j,k)],vector);
        }
    }
}

/*
 * Advects u, v, and w
 */
void advectVectors(
  int N,
  int chunk_mask,
  float ** jru,
  float ** jrv,
  float ** jrw,
  float ** jru0,
  float ** jrv0,
  float ** jrw0,
  float DIFFUSION_CONST,
  float VISCOSITY_CONST,
  float dt
){
    advect(chunk_mask,N,1,jru,jru0,GET_ARR_RAW(jru0,CENTER_LOC),GET_ARR_RAW(jrv0,CENTER_LOC),GET_ARR_RAW(jrw0,CENTER_LOC),dt);
    advect(chunk_mask,N,2,jrv,jrv0,GET_ARR_RAW(jru0,CENTER_LOC),GET_ARR_RAW(jrv0,CENTER_LOC),GET_ARR_RAW(jrw0,CENTER_LOC),dt);
    advect(chunk_mask,N,3,jrw,jrw0,GET_ARR_RAW(jru0,CENTER_LOC),GET_ARR_RAW(jrv0,CENTER_LOC),GET_ARR_RAW(jrw0,CENTER_LOC),dt);
}

/**
 * Actually performs the advection
*/
void advect(uint32_t chunk_mask, int N, int b, float ** jrd, float ** jrd0, float * u, float * v, float * w, float dt){
    int i, j, k, i0, j0, k0, i1, j1, k1;
    int m,n,o;
    float x, y, z, s0, t0, s1, t1, u1, u0, dtx,dty,dtz;
    
    dtx=dty=dtz=dt*N;

    float * d = GET_ARR_RAW(jrd,CENTER_LOC);

    float * d0 = GET_ARR_RAW(jrd0,CENTER_LOC);

    for(k=1; k<N-1; k++){
        for(j=1; j<N-1; j++){
            for(i=1; i<N-1; i++){
                d0 = GET_ARR_RAW(jrd0,CENTER_LOC);
                //calculate location to pull from
                x = i-dtx*u[IX(i,j,k)];
                y = j-dty*v[IX(i,j,k)];
                z = k-dtz*w[IX(i,j,k)];

                m = n = o = 1;

                if(x < 0){ m += 1; }
                else if(x >= N){ m -= 1; }
                if(y < 0){ n += 1; }
                else if(y >= N){ n -= 1; }
                if(z < 0){ o += 1; }
                else if(z >= N){ o -= 1; }

                //If the out of bounds coordinate is in bounds for a neighbor chunk, use that chunk as source instead
                if(CK(m,n,o) != CENTER_LOC && ARR_EXISTS(chunk_mask,m,n,o)){

                    // if(i == 1 && j == 1 && k == 1){
                    //     printf("\narr indices: %d %d %d\n\n",m,n,o);
                    // }

                    //cases:
                    //if x = 17.01, m = 2
                    // 17 in current array is 1 in neighbor
                    // 18 in current array is 2 in neighbor
                    // 19 in current array is 3 in neighbor
                    //want to sample neighbor array at 1 & 2
                    //x becomes 1.01, sampling new array (keep in mind that 0 in the new array should contain the current array values)
                    //modification: subtract 16

                    //cases:
                    //if x = 16.99, m = 2
                    // 16 in current array is 0 in neighbor
                    // 17 in current array is 1 in neighbor
                    // 18 in current array is 2 in neighbor
                    // 19 in current array is 3 in neighbor
                    //want to sample current array still
                    //x becomes 1.01, sampling new array (keep in mind that 0 in the new array should contain the current array values)
                    //modification: no modification

                    //if x = 0.01, m = 0
                    // 0 in current array is 16 in neighbor
                    //-1 in current array is 15 in neighbor
                    //-2 in current array is 14 in neighbor
                    //want to sample current array still
                    //x becomes 15.01, sampling new array (keep in mind that 17 in the new array should contain the current array)
                    //modification: no modification

                    //if x = -0.01, m = 0
                    // 0 in current array is 16 in neighbor
                    //-1 in current array is 15 in neighbor
                    //-2 in current array is 14 in neighbor
                    //want to sample -1 & 0, so i0 becomes 15
                    //x becomes 15.99, sampling new array (keep in mind that 17 in the new array should contain the current array)
                    //modification: add 16

                    //if x = -2, m = 0
                    // 0 in current array is 16 in neighbor
                    //-1 in current array is 15 in neighbor
                    //-2 in current array is 14 in neighbor
                    //x becomes 14, sampling new array (keep in mind that 17 in the new array should contain the current array)
                    //modification: add 16


                    // printf("Hit other chunk\n");
                    d0 = GET_ARR_RAW(jrd0,CK(m,n,o));
                    x = x + CHUNK_NORMALIZE_U[CK(m,n,o)] * (N-2);
                    // printf("%d => %f\n",m,x);
                    y = y + CHUNK_NORMALIZE_V[CK(m,n,o)] * (N-2);
                    z = z + CHUNK_NORMALIZE_W[CK(m,n,o)] * (N-2);
                }

                //clamp location within chunk
                //get indices, and calculate percentage to pull from each index
                if(x < 0.001f){
                    //cases to consider:
                    //m = 0, x = -10
                    //m = 2, x = 0.01
                    x=0.001f;
                    i0=(int)0;
                    i1=1;
                    s0 = 0.999f;
                    s1 = 0.001f;
                } else if(x > N - 1){
                    //cases to consider:
                    //m = 0, x = 17.01
                    //m = 2, x = 20
                    x = N-1;
                    i0=(int)N-2;
                    i1=N-1;
                    s0 = 0.001f;
                    s1 = 0.999f;
                } else {
                    i0=(int)x;
                    i1=i0+1;
                    s1 = x-i0;
                    s0 = 1-s1;
                }

                if(y < 0.001f){
                    //cases to consider:
                    //m = 0, x = -10
                    //m = 2, x = 0.01
                    y=0.001f;
                    j0=(int)0;
                    j1=1;
                    t0 = 0.999f;
                    t1 = 0.001f;
                } else if(y > N - 1){
                    //cases to consider:
                    //m = 0, x = 17.01
                    //m = 2, x = 20
                    y = N-1;
                    j0=(int)N-2;
                    j1=N-1;
                    t0 = 0.001f;
                    t1 = 0.999f;
                } else {
                    j0=(int)y;
                    j1=j0+1;
                    t1 = y-j0;
                    t0 = 1-t1;
                }


                if(z < 0.001f){
                    //cases to consider:
                    //m = 0, x = -10
                    //m = 2, x = 0.01
                    z=0.001f;
                    k0=(int)0;
                    k1=1;
                    u0 = 0.999f;
                    u1 = 0.001f;
                } else if(z > N - 1){
                    //cases to consider:
                    //m = 0, x = 17.01
                    //m = 2, x = 20
                    z = N-1;
                    k0=(int)N-2;
                    k1=N-1;
                    u0 = 0.001f;
                    u1 = 0.999f;
                } else {
                    k0=(int)z;
                    k1=k0+1;
                    u1 = z-k0;
                    u0 = 1-u1;
                }

                // if (x<0.001f) x=0.001f;
                // if (x>N+0.5f) x=N+0.5f;
                // if (y<0.001f) y=0.001f;
                // if (y>N+0.5f) y=N+0.5f;
                // if (z<0.001f) z=0.001f;
                // if (z>N+0.5f) z=N+0.5f;

                //get actual indices
                // i0=(int)x;
                // i1=i0+1;
                // j0=(int)y;
                // j1=j0+1;
                // k0=(int)z;
                // k1=k0+1;

                //calculate percentage of each index
                // s1 = x-i0;
                // s0 = 1-s1;
                // t1 = y-j0;
                // t0 = 1-t1;
                // u1 = z-k0;
                // u0 = 1-u1;

                // if(i0 >= N){
                //     i0 = N - 1;
                // }
                // if(i0 < 0){
                //     i0 = 0;
                // }
                if(j0 >= N){
                    j0 = N - 1;
                }
                // if(j0 < 0){
                //     j0 = 0;
                // }
                if(k0 >= N){
                    k0 = N - 1;
                }
                // if(k0 < 0){
                //     k0 = 0;
                // }
                // if(i1 >= N){
                //     i1 = N - 1;
                // }
                // if(i1 < 0){
                //     i1 = 0;
                // }
                if(j1 >= N){
                    j1 = N - 1;
                }
                // if(j1 < 0){
                //     j1 = 0;
                // }
                if(k1 >= N){
                    k1 = N - 1;
                }
                // if(k1 < 0){
                //     k1 = 0;
                // }
                d[IX(i,j,k)] = 
                s0*(
                    t0*u0*d0[IX(i0,j0,k0)]+
                    t1*u0*d0[IX(i0,j1,k0)]+
                    t0*u1*d0[IX(i0,j0,k1)]+
                    t1*u1*d0[IX(i0,j1,k1)]
                )+
                s1*(
                    t0*u0*d0[IX(i1,j0,k0)]+
                    t1*u0*d0[IX(i1,j1,k0)]+
                    t0*u1*d0[IX(i1,j0,k1)]+
                    t1*u1*d0[IX(i1,j1,k1)]
                );
            }
        }
    }
}

/**
 * Sets the bounds of this cube to those of its neighbor
*/
void setBoundsToNeighborsRaw(
  int N,
  int chunk_mask,
  int vector_dir,
  float ** neighborArray
){
    float * target = GET_ARR_RAW(neighborArray,CENTER_LOC);
    float * source;
    //set the faces bounds
    for(int x=1; x < DIM-1; x++){
        for(int y = 1; y < DIM-1; y++){
            //((x)+(DIM)*(y) + (DIM)*(DIM)*(z))
            target[IX(0,x,y)] =         vector_dir==BOUND_DIR_U ? -target[IX(1,x,y)] : target[IX(1,x,y)];
            target[IX(DIM-1,x,y)] =     vector_dir==BOUND_DIR_U ? -target[IX(DIM-2,x,y)] : target[IX(DIM-2,x,y)];
            target[IX(x,0,y)] =         vector_dir==BOUND_DIR_V ? -target[IX(x,1,y)] : target[IX(x,1,y)];
            target[IX(x,DIM-1,y)] =     vector_dir==BOUND_DIR_V ? -target[IX(x,DIM-2,y)] : target[IX(x,DIM-2,y)];
            target[IX(x,y,0)] =         vector_dir==BOUND_DIR_W ? -target[IX(x,y,1)] : target[IX(x,y,1)];
            target[IX(x,y,DIM-1)] =     vector_dir==BOUND_DIR_W ? -target[IX(x,y,DIM-2)] : target[IX(x,y,DIM-2)];
        }
    }
    //sets the edges of the chunk
    for(int x = 1; x < DIM-1; x++){
        target[IX(x,0,0)]         = (float)(0.5f * (target[IX(x,1,0)] + target[IX(x,0,1)]));
        target[IX(x,DIM-1,0)]     = (float)(0.5f * (target[IX(x,DIM-2,0)] + target[IX(x,DIM-1,1)]));
        target[IX(x,0,DIM-1)]     = (float)(0.5f * (target[IX(x,1,DIM-1)] + target[IX(x,0,DIM-2)]));
        target[IX(x,DIM-1,DIM-1)] = (float)(0.5f * (target[IX(x,DIM-2,DIM-1)] + target[IX(x,DIM-1,DIM-2)]));

        target[IX(0,x,0)] = (float)(0.5f * (target[IX(1,x,0)] + target[IX(0,x,1)]));
        target[IX(DIM-1,x,0)] = (float)(0.5f * (target[IX(DIM-2,x,0)] + target[IX(DIM-1,x,1)]));
        target[IX(0,x,DIM-1)] = (float)(0.5f * (target[IX(1,x,DIM-1)] + target[IX(0,x,DIM-2)]));
        target[IX(DIM-1,x,DIM-1)] = (float)(0.5f * (target[IX(DIM-2,x,DIM-1)] + target[IX(DIM-1,x,DIM-2)]));


        target[IX(0,0,x)] = (float)(0.5f * (target[IX(1,0,x)] + target[IX(0,1,x)]));
        target[IX(DIM-1,0,x)] = (float)(0.5f * (target[IX(DIM-2,0,x)] + target[IX(DIM-1,1,x)]));
        target[IX(0,DIM-1,x)] = (float)(0.5f * (target[IX(1,DIM-1,x)] + target[IX(0,DIM-2,x)]));
        target[IX(DIM-1,DIM-1,x)] = (float)(0.5f * (target[IX(DIM-2,DIM-1,x)] + target[IX(DIM-1,DIM-2,x)]));

    }
    //sets the corners of the chunk
    target[IX(0,0,0)]             = (float)((target[IX(1,0,0)]+target[IX(0,1,0)]+target[IX(0,0,1)])/3.0);
    target[IX(DIM-1,0,0)]         = (float)((target[IX(DIM-2,0,0)]+target[IX(DIM-1,1,0)]+target[IX(DIM-1,0,1)])/3.0);
    target[IX(0,DIM-1,0)]         = (float)((target[IX(1,DIM-1,0)]+target[IX(0,DIM-2,0)]+target[IX(0,DIM-1,1)])/3.0);
    target[IX(0,0,DIM-1)]         = (float)((target[IX(0,0,DIM-2)]+target[IX(1,0,DIM-1)]+target[IX(0,1,DIM-1)])/3.0);
    target[IX(DIM-1,DIM-1,0)]     = (float)((target[IX(DIM-2,DIM-1,0)]+target[IX(DIM-1,DIM-2,0)]+target[IX(DIM-1,DIM-1,1)])/3.0);
    target[IX(0,DIM-1,DIM-1)]     = (float)((target[IX(1,DIM-1,DIM-1)]+target[IX(0,DIM-2,DIM-1)]+target[IX(0,DIM-1,DIM-2)])/3.0);
    target[IX(DIM-1,0,DIM-1)]     = (float)((target[IX(DIM-1,0,DIM-2)]+target[IX(DIM-2,0,DIM-1)]+target[IX(DIM-1,1,DIM-1)])/3.0);
    target[IX(DIM-1,DIM-1,DIM-1)] = (float)((target[IX(DIM-1,DIM-1,DIM-2)]+target[IX(DIM-1,DIM-2,DIM-1)]+target[IX(DIM-1,DIM-1,DIM-2)])/3.0);
}


/**
 * This exclusively copies neighbors to make sure zeroing out stuff doesn't break sim
*/
void copyNeighborsRaw(
  int N,
  int chunk_mask,
  int cx,
  int vector_dir,
  float ** neighborArray
){
    float * target = GET_ARR_RAW(neighborArray,CENTER_LOC);
    float * source;


    //
    //
    //   PLANES
    //
    //
    // __m512 transferVector;// = _mm512_set1_ps(0.5*N);

    //__m256 vector = _mm256_loadu_ps(&p[IX(i-1,j,k)]);
    //vector = _mm256_add_ps(vector,_mm256_loadu_ps(&p[IX(i+1,j,k)]));
    //vector = _mm256_add_ps(vector,_mm256_loadu_ps(&p[IX(i,j-1,k)]));
    //_mm256_storeu_ps(&p[IX(i,j,k)],vector);
    //__m256
    //_mm256_loadu_ps
    //_mm256_storeu_ps
    if(ARR_EXISTS(chunk_mask,0,1,1)){
        source = GET_ARR_RAW(neighborArray,CK(0,1,1));
        for(int x=1; x < DIM-1; x++){
            // transferVector = _mm512_loadu_ps(&source[IX(DIM-2,x,1)]);
            // _mm512_storeu_ps(&target[IX(0,x,1)],_mm512_loadu_ps(&source[IX(DIM-2,x,1)]));
            for(int y = 1; y < DIM-1; y++){
                target[IX(0,x,y)] = source[IX(DIM-2,x,y)];
            }
        }
    }

    if(ARR_EXISTS(chunk_mask,2,1,1)){
        source = GET_ARR_RAW(neighborArray,CK(2,1,1));
        for(int x=1; x < DIM-1; x++){
            // _mm512_storeu_ps(&target[IX(DIM-1,x,1)],_mm512_loadu_ps(&source[IX(1,x,1)]));
            for(int y = 1; y < DIM-1; y++){
                target[IX(DIM-1,x,y)] = source[IX(1,x,y)];
            }
        }
    }

    if(ARR_EXISTS(chunk_mask,1,0,1)){
        source = GET_ARR_RAW(neighborArray,CK(1,0,1));
        for(int x=1; x < DIM-1; x++){
            for(int y = 1; y < DIM-1; y++){
                target[IX(x,0,y)] = source[IX(x,DIM-2,y)];
            }
        }
    }

    if(ARR_EXISTS(chunk_mask,1,2,1)){
        source = GET_ARR_RAW(neighborArray,CK(1,2,1));
        for(int x=1; x < DIM-1; x++){
            for(int y = 1; y < DIM-1; y++){
                target[IX(x,DIM-1,y)] = source[IX(x,1,y)];
            }
        }
    }

    if(ARR_EXISTS(chunk_mask,1,1,0)){
        source = GET_ARR_RAW(neighborArray,CK(1,1,0));
        for(int x=1; x < DIM-1; x++){
            for(int y = 1; y < DIM-1; y++){
                target[IX(x,y,0)] = source[IX(x,y,DIM-2)];
            }
        }
    }

    if(ARR_EXISTS(chunk_mask,1,1,2)){
        source = GET_ARR_RAW(neighborArray,CK(1,1,2));
        for(int x=1; x < DIM-1; x++){
            for(int y = 1; y < DIM-1; y++){
                target[IX(x,y,DIM-1)] = source[IX(x,y,1)];
            }
        }
    }


    //
    //
    //    EDGES
    //
    //
    if(ARR_EXISTS(chunk_mask,0,0,1)){
        source = GET_ARR_RAW(neighborArray,CK(0,0,1));
        for(int x=1; x < DIM-1; x++){
                target[IX(0,0,x)] = source[IX(DIM-2,DIM-2,x)];
        }
    }

    if(ARR_EXISTS(chunk_mask,2,0,1)){
        source = GET_ARR_RAW(neighborArray,CK(2,0,1));
        for(int x=1; x < DIM-1; x++){
                target[IX(DIM-1,0,x)] = source[IX(1,DIM-2,x)];
        }
    }

    if(ARR_EXISTS(chunk_mask,0,2,1)){
        source = GET_ARR_RAW(neighborArray,CK(0,2,1));
        for(int x=1; x < DIM-1; x++){
                target[IX(0,DIM-1,x)] = source[IX(DIM-2,1,x)];
        }
    }

    if(ARR_EXISTS(chunk_mask,2,2,1)){
        source = GET_ARR_RAW(neighborArray,CK(2,2,1));
        for(int x=1; x < DIM-1; x++){
                target[IX(DIM-1,DIM-1,x)] = source[IX(1,1,x)];
        }
    }

    //
    //

    if(ARR_EXISTS(chunk_mask,0,1,0)){
        source = GET_ARR_RAW(neighborArray,CK(0,1,0));
        for(int x=1; x < DIM-1; x++){
                target[IX(0,x,0)] = source[IX(DIM-2,x,DIM-2)];
        }
    }

    if(ARR_EXISTS(chunk_mask,2,1,0)){
        source = GET_ARR_RAW(neighborArray,CK(2,1,0));
        for(int x=1; x < DIM-1; x++){
                target[IX(DIM-1,x,0)] = source[IX(1,x,DIM-2)];
        }
    }

    if(ARR_EXISTS(chunk_mask,0,1,2)){
        source = GET_ARR_RAW(neighborArray,CK(0,1,2));
        for(int x=1; x < DIM-1; x++){
                target[IX(0,x,DIM-1)] = source[IX(DIM-2,x,1)];
        }
    }

    if(ARR_EXISTS(chunk_mask,2,1,2)){
        source = GET_ARR_RAW(neighborArray,CK(2,1,2));
        for(int x=1; x < DIM-1; x++){
                target[IX(DIM-1,x,DIM-1)] = source[IX(1,x,1)];
        }
    }

    //
    //

    if(ARR_EXISTS(chunk_mask,1,0,0)){
        source = GET_ARR_RAW(neighborArray,CK(1,0,0));
        for(int x=1; x < DIM-1; x++){
                target[IX(x,0,0)] = source[IX(x,DIM-2,DIM-2)];
        }
    }

    if(ARR_EXISTS(chunk_mask,1,2,0)){
        source = GET_ARR_RAW(neighborArray,CK(1,2,0));
        for(int x=1; x < DIM-1; x++){
                target[IX(x,DIM-1,0)] = source[IX(x,1,DIM-2)];
        }
    }

    if(ARR_EXISTS(chunk_mask,1,0,2)){
        source = GET_ARR_RAW(neighborArray,CK(1,0,2));
        for(int x=1; x < DIM-1; x++){
                target[IX(x,0,DIM-1)] = source[IX(x,DIM-2,1)];
        }
    }

    if(ARR_EXISTS(chunk_mask,1,2,2)){
        source = GET_ARR_RAW(neighborArray,CK(1,2,2));
        for(int x=1; x < DIM-1; x++){
                target[IX(x,DIM-1,DIM-1)] = source[IX(x,1,1)];
        }
    }


    //
    //
    //     CORNERS
    //
    //

    if(ARR_EXISTS(chunk_mask,0,0,0)){
        source = GET_ARR_RAW(neighborArray,CK(0,0,0));
        target[IX(0,0,0)] = source[IX(DIM-2,DIM-2,DIM-2)];
    }

    if(ARR_EXISTS(chunk_mask,2,0,0)){
        source = GET_ARR_RAW(neighborArray,CK(2,0,0));
        target[IX(DIM-1,0,0)] = source[IX(1,DIM-2,DIM-2)];
    }

    if(ARR_EXISTS(chunk_mask,0,2,0)){
        source = GET_ARR_RAW(neighborArray,CK(0,2,0));
        target[IX(0,DIM-1,0)] = source[IX(DIM-2,1,DIM-2)];
    }

    if(ARR_EXISTS(chunk_mask,2,2,0)){
        source = GET_ARR_RAW(neighborArray,CK(2,2,0));
        target[IX(DIM-1,DIM-1,0)] = source[IX(1,1,DIM-2)];
    }

    //
    //

    if(ARR_EXISTS(chunk_mask,0,0,2)){
        source = GET_ARR_RAW(neighborArray,CK(0,0,2));
        target[IX(0,0,DIM-1)] = source[IX(DIM-2,DIM-2,1)];
    }

    if(ARR_EXISTS(chunk_mask,2,0,2)){
        source = GET_ARR_RAW(neighborArray,CK(2,0,2));
        target[IX(DIM-1,0,DIM-1)] = source[IX(1,DIM-2,1)];
    }

    if(ARR_EXISTS(chunk_mask,0,2,2)){
        source = GET_ARR_RAW(neighborArray,CK(0,2,2));
        target[IX(0,DIM-1,DIM-1)] = source[IX(DIM-2,1,1)];
    }

    if(ARR_EXISTS(chunk_mask,2,2,2)){
        source = GET_ARR_RAW(neighborArray,CK(2,2,2));
        target[IX(DIM-1,DIM-1,DIM-1)] = source[IX(1,1,1)];
    }



}
