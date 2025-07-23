#include <stdio.h>
#include <immintrin.h>
#include <stdint.h>
#include <jni.h>

#include "fluid/env/utilities.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/environment.h"
#include "fluid/queue/chunk.h"


/**
 * Adds density to the density array
 * @return The change in density within this chunk for this frame
*/
void addDensity(
    Environment * environment,
    int N,
    int chunk_mask,
    float ** d,
    float ** d0,
    float dt
){
    int i;
    int size=N*N*N;
    float * x = GET_ARR_RAW(d,CENTER_LOC);
    float * s = GET_ARR_RAW(d0,CENTER_LOC);
    for(i=0; i<size; i++){
        environment->state.newDensity = environment->state.newDensity + dt * s[i];
        environment->state.existingDensity = environment->state.existingDensity + x[i];
        x[i] += dt*s[i];
        if(x[i] < MIN_FLUID_VALUE){
            x[i] = MIN_FLUID_VALUE;
        } else if(x[i] > MAX_FLUID_VALUE){
            x[i] = MAX_FLUID_VALUE;
        }
    }
}

/*
 * A single iteration of the jacobi to solve density diffusion
 */
void solveDiffuseDensity(
  int N,
  int chunk_mask,
  float ** d,
  float ** d0,
  float ** jru,
  float ** jrv,
  float ** jrw,
  float DIFFUSION_CONST,
  float VISCOSITY_CONST,
  float dt
){
    float a=dt*DIFFUSION_CONST*N*N*N;
    float c=1+6*a;
    int i, j, k, l, m;
    float * x = GET_ARR_RAW(d,CENTER_LOC);
    float * x0 = GET_ARR_RAW(d0,CENTER_LOC);
    
    __m256 aScalar = _mm256_set1_ps(a);
    __m256 cScalar = _mm256_set1_ps(c);

    //transform u direction
    for(k=1; k<N-1; k++){
        for(j=1; j<N-1; j++){
            int n = 0;
            //solve as much as possible vectorized
            for(i = 1; i < N-1; i=i+8){
                __m256 vector = _mm256_loadu_ps(&x[IX(i-1,j,k)]);
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[IX(i+1,j,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[IX(i,j-1,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[IX(i,j+1,k)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[IX(i,j,k-1)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[IX(i,j,k+1)]));
                vector = _mm256_mul_ps(vector,aScalar);
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x0[IX(i,j,k)]));
                vector = _mm256_div_ps(vector,cScalar);
                _mm256_storeu_ps(&x[IX(i,j,k)],vector);
            }
            //If there is any leftover, perform manual solving
            if(i>N-1){
                for(i=i-8; i < N-1; i++){
                    x[IX(i,j,k)] = (x0[IX(i,j,k)] + a*(x[IX(i-1,j,k)]+x[IX(i+1,j,k)]+x[IX(i,j-1,k)]+x[IX(i,j+1,k)]+x[IX(i,j,k-1)]+x[IX(i,j,k+1)]))/c;
                }
            }
        }
    }
}

/**
 * Advects the density based on the vectors
*/
void advectDensity(uint32_t chunk_mask, int N, float ** d, float ** d0, float ** ur, float ** vr, float ** wr, float dt){
    int i, j, k, i0, j0, k0, i1, j1, k1;
    int m,n,o;
    float x, y, z, s0, t0, s1, t1, u1, u0, dtx,dty,dtz;
    
    dtx=dty=dtz=dt*N;

    float * center_d = GET_ARR_RAW(d,CENTER_LOC);
    float * center_d0 = GET_ARR_RAW(d0,CENTER_LOC);

    float * u = GET_ARR_RAW(ur,CENTER_LOC);
    float * v = GET_ARR_RAW(vr,CENTER_LOC);
    float * w = GET_ARR_RAW(wr,CENTER_LOC);

    for(k=1; k<N-1; k++){
        for(j=1; j<N-1; j++){
            for(i=1; i<N-1; i++){
                center_d0 = GET_ARR_RAW(d0,CENTER_LOC);
                //calculate location to pull from
                x = i-dtx*u[IX(i,j,k)];
                y = j-dty*v[IX(i,j,k)];
                z = k-dtz*w[IX(i,j,k)];

                m = n = o = 1;

                if(x < 1){ m -= 1; }
                if(x >= N-1){ m += 1; }
                if(y < 1){ n -= 1; }
                if(y >= N-1){ n += 1; }
                if(z < 1){ o -= 1; }
                if(z >= N-1){ o += 1; }

                //If the out of bounds coordinate is in bounds for a neighbor chunk, use that chunk as source instead
                // if(CK(m,n,o) != CENTER_LOC){
                //     printf("Looking in border chunk\n");
                // }
                // if(x > 16){
                //     printf("%f %d %d %d\n",m,n,o);
                // }
                // if(CK(m,n,o) != CENTER_LOC && ARR_EXISTS(chunk_mask,m,n,o)){
                //     // printf("Hit other chunk\n");
                //     d0 = GET_ARR(env,jrd0,CK(m,n,o));
                //     x = x + CHUNK_NORMALIZE_U[CK(m,n,o)] * (N-1);
                //     y = y + CHUNK_NORMALIZE_V[CK(m,n,o)] * (N-1);
                //     z = z + CHUNK_NORMALIZE_W[CK(m,n,o)] * (N-1);
                // }

                if(x < 0.001f){
                    //cases to consider:
                    //m = 0, x = -10
                    //m = 2, x = 0.01
                    x=0.001f;
                    i0=(int)0;
                    i1=1;
                    s0 = 0.999f;
                    s1 = 0.001f;
                } else if(x >= N - 1){
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

                //clamp location within chunk
                // if (x<0.5f) x=0.5f;
                // if (x>N+0.5f) x=N+0.5f;
                if (y<0.5f) y=0.5f;
                if (y>N+0.5f) y=N+0.5f;
                if (z<0.5f) z=0.5f;
                if (z>N+0.5f) z=N+0.5f;

                //get actual indices
                // i0=(int)x;
                // i1=i0+1;
                j0=(int)y;
                j1=j0+1;
                k0=(int)z;
                k1=k0+1;

                //calculate percentage of each index
                // s1 = x-i0;
                // s0 = 1-s1;
                t1 = y-j0;
                t0 = 1-t1;
                u1 = z-k0;
                u0 = 1-u1;

                if(i0 >= N){
                    i0 = N - 1;
                }
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
                if(i1 >= N){
                    i1 = N - 1;
                }
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
                center_d[IX(i,j,k)] = 
                s0*(
                    t0*u0*center_d0[IX(i0,j0,k0)]+
                    t1*u0*center_d0[IX(i0,j1,k0)]+
                    t0*u1*center_d0[IX(i0,j0,k1)]+
                    t1*u1*center_d0[IX(i0,j1,k1)]
                )+
                s1*(
                    t0*u0*center_d0[IX(i1,j0,k0)]+
                    t1*u0*center_d0[IX(i1,j1,k0)]+
                    t0*u1*center_d0[IX(i1,j0,k1)]+
                    t1*u1*center_d0[IX(i1,j1,k1)]
                );
            }
        }
    }
}


/**
 * Sums the density of the chunk
 */
double calculateSum(uint32_t chunk_mask, int N, float ** d){
    int j;
    int size=N*N*N;
    float * x = GET_ARR_RAW(d,CENTER_LOC);
    double rVal = 0;
    for(j=0; j<size; j++){
        rVal = rVal + x[j];
    }
    return rVal;
}

/**
 * Normalizes the density array with a given ratio
 */
void normalizeDensity(int N, float ** d, float ratio){
    int j;
    int size=N*N*N;
    float * x = GET_ARR_RAW(d,CENTER_LOC);
    for(j=0; j<size; j++){
        float value = x[j];
        value = value * ratio;
        x[j] = value;
    }
}
