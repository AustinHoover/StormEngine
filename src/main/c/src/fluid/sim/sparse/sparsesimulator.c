#include <stdio.h>
#include <stdlib.h>
#include <immintrin.h>

#include "stb/stb_ds.h"

#include "fluid/queue/chunk.h"
#include "fluid/queue/islandsolver.h"
#include "fluid/sim/sparse/sparsesimulator.h"
#include "fluid/env/utilities.h"

#define LINEARSOLVERTIMES 10

#define FLUID_SIM_TIMESTEP 0.01

#define DIFFUSE_CONSTANT 0.00001
#define VISCOSITY_CONSTANT 0.00001

/**
 * Simulates a sparse array
 */
LIBRARY_API int fluid_sparse_array_simulate(SparseChunkArray * array, float dt){
    //velocity step
    fluid_sparse_array_vel_step(
        array->u,
        array->v,
        array->w,
        array->u0,
        array->v0,
        array->w0,
        VISCOSITY_CONSTANT,
        FLUID_SIM_TIMESTEP
    );

    //density step
    fluid_sparse_array_dens_step(
        array->d,
        array->d0,
        array->u,
        array->v,
        array->w,
        DIFFUSE_CONSTANT,
        FLUID_SIM_TIMESTEP
    );
}




/**
 * Adds values from a source array to a current frame array (eg more density to the main density array)
 * @param x The array to store into
 * @param s The source array to pull from
 * @param dt The delta time of the simulation
*/
LIBRARY_API void fluid_sparse_array_add_source(float * x, float * s, float dt){
	int i;
    int size=SPARSE_ARRAY_FULL_SIZE;
	for(i=0; i<size; i++){
        x[i] += dt*s[i];
    }
}

/**
 * Diffuses a given array by a diffusion constant
*/
LIBRARY_API void fluid_sparse_array_diffuse(int b, float * x, float * x0, float diff, float dt){
    fluid_sparse_array_lin_solve_diffuse(b, x, x0);
}

/**
 * Advects a given array based on the force vectors in the simulation
*/
LIBRARY_API void fluid_sparse_array_advect(int b, float * d, float * d0, float * u, float * v, float * w, float dt){
    int i, j, k, i0, j0, k0, i1, j1, k1;
    float x, y, z, s0, t0, s1, t1, u1, u0, dtx,dty,dtz;
    
    dtx=dty=dtz=dt*SPARSE_ARRAY_ACTUAL_DATA_DIM;

    for(k=(SPARSE_ARRAY_BORDER_SIZE / 2); k<SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2); k++){
        for(j=(SPARSE_ARRAY_BORDER_SIZE / 2); j<SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2); j++){
            for(i=(SPARSE_ARRAY_BORDER_SIZE / 2); i<SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2); i++){
                x = i-dtx*u[IX(i,j,k)]; y = j-dty*v[IX(i,j,k)]; z = k-dtz*w[IX(i,j,k)];
                if (x<0.5f) x=0.5f; if (x>SPARSE_ARRAY_RAW_DIM+0.5f) x=SPARSE_ARRAY_RAW_DIM+0.5f; i0=(int)x; i1=i0+1;
                if (y<0.5f) y=0.5f; if (y>SPARSE_ARRAY_RAW_DIM+0.5f) y=SPARSE_ARRAY_RAW_DIM+0.5f; j0=(int)y; j1=j0+1;
                if (z<0.5f) z=0.5f; if (z>SPARSE_ARRAY_RAW_DIM+0.5f) z=SPARSE_ARRAY_RAW_DIM+0.5f; k0=(int)z; k1=k0+1;

                s1 = x-i0; s0 = 1-s1; t1 = y-j0; t0 = 1-t1; u1 = z-k0; u0 = 1-u1;
                if(i0 >= SPARSE_ARRAY_RAW_DIM){
                    i0 = SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2);
                }
                if(j0 >= SPARSE_ARRAY_RAW_DIM){
                    j0 = SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2);
                }
                if(k0 >= SPARSE_ARRAY_RAW_DIM){
                    k0 = SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2);
                }
                if(i1 >= SPARSE_ARRAY_RAW_DIM){
                    i1 = SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2);
                }
                if(j1 >= SPARSE_ARRAY_RAW_DIM){
                    j1 = SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2);
                }
                if(k1 >= SPARSE_ARRAY_RAW_DIM){
                    k1 = SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2);
                }
                d[IX(i,j,k)] = s0*(t0*u0*d0[IX(i0,j0,k0)]+t1*u0*d0[IX(i0,j1,k0)]+t0*u1*d0[IX(i0,j0,k1)]+t1*u1*d0[IX(i0,j1,k1)])+
                    s1*(t0*u0*d0[IX(i1,j0,k0)]+t1*u0*d0[IX(i1,j1,k0)]+t0*u1*d0[IX(i1,j0,k1)]+t1*u1*d0[IX(i1,j1,k1)]);
            }
        }
    }
    fluid_sparse_array_set_bnd(b, d);
}

/**
 * Performs the density step
 * @param x The density array
 * @param x0 The delta-density array
 * @param u The x velocity array
 * @param v The y velocity array
 * @param w THe z velocity array
 * @param diff The diffuse constant
 * @param dt The delta time for the simulation
 */
LIBRARY_API void fluid_sparse_array_dens_step(float * x, float * x0, float * u, float * v, float * w, float diff, float dt){
    fluid_sparse_array_add_source(x, x0, dt);
    SWAP(x0, x);
    fluid_sparse_array_diffuse(0, x, x0, diff, dt);
    SWAP(x0, x);
    fluid_sparse_array_advect(0, x, x0, u, v, w, dt);
}

/**
 * The main velocity step function
*/
LIBRARY_API void fluid_sparse_array_vel_step(float * u, float * v, float * w, float * u0, float * v0, float * w0, float visc, float dt){
    fluid_sparse_array_add_source(u, u0, dt);
    fluid_sparse_array_add_source(v, v0, dt);
    fluid_sparse_array_add_source(w, w0, dt);
    SWAP(u0, u);
    fluid_sparse_array_diffuse(1, u, u0, visc, dt);
    SWAP(v0, v);
    fluid_sparse_array_diffuse(2, v, v0, visc, dt);
    SWAP(w0, w);
    fluid_sparse_array_diffuse(3, w, w0, visc, dt);
    fluid_sparse_array_project(u, v, w, u0, v0);
    SWAP(u0, u);
    SWAP(v0, v);
    SWAP(w0, w);
    fluid_sparse_array_advect(1, u, u0, u0, v0, w0, dt);
    fluid_sparse_array_advect(2, v, v0, u0, v0, w0, dt);
    fluid_sparse_array_advect(3, w, w0, u0, v0, w0, dt);
    fluid_sparse_array_project(u, v, w, u0, v0);
}

/**
 * Projects a given array based on force vectors
*/
LIBRARY_API void fluid_sparse_array_project(float * u, float * v, float * w, float * p, float * div){
    int i, j, k;

    __m256 nVector = _mm256_set1_ps(SPARSE_ARRAY_ACTUAL_DATA_DIM);
    __m256 constScalar = _mm256_set1_ps(-1.0/2.0);
    __m256 zeroVec = _mm256_set1_ps(0);
    __m256 vector, vector2, vector3;

    //compute central difference approximation to populate for gauss-seidel relaxation
    for(k=1; k<SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2); k++){
        for(j=1; j<SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2); j++){
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
    
    fluid_sparse_array_set_bnd(0, div);
    fluid_sparse_array_set_bnd(0, p);

    //solve system of equations
    fluid_sparse_array_lin_solve_project(0, p, div);

    //remove divergence from vector field
    constScalar = _mm256_set1_ps(0.5f*SPARSE_ARRAY_ACTUAL_DATA_DIM);
    for ( k=1 ; k<SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2) ; k++ ) {
        for ( j=1 ; j<SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2) ; j++ ) {
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
    
    fluid_sparse_array_set_bnd(1, u);
    fluid_sparse_array_set_bnd(2, v);
    fluid_sparse_array_set_bnd(3, w);
}

/**
 * Solves a linear system of equations in a vectorized manner
 * @param b The axis to set the bounds along
 * @param x The array that will contain the solved equations
 * @param x0 The array containing the first order derivatives
 * @param a 
*/
LIBRARY_API void fluid_sparse_array_lin_solve_diffuse(int b, float * x, float * x0){
    int i, j, k, l, m;
    int a = FLUID_SIM_TIMESTEP * DIFFUSE_CONSTANT * SPARSE_ARRAY_ACTUAL_DATA_DIM * SPARSE_ARRAY_ACTUAL_DATA_DIM * SPARSE_ARRAY_ACTUAL_DATA_DIM;
    int c = 1+6*a;
    
    __m256 aScalar = _mm256_set1_ps(a);
    __m256 cScalar = _mm256_set1_ps(c);
    int vectorSize = 8;
    // iterate the solver
    for(l = 0; l < LINEARSOLVERTIMES; l++){
        // update for each cell
        for(k = (SPARSE_ARRAY_BORDER_SIZE / 2); k < SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2); k++){
            for(j = (SPARSE_ARRAY_BORDER_SIZE / 2); j < SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2); j++){
                int n = 0;
                //solve as much as possible vectorized
                for(i = (SPARSE_ARRAY_BORDER_SIZE / 2); i < SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2); i = i + vectorSize){
                    __m256 vector = _mm256_loadu_ps(&x[GVI(i-1,j,k)]);
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[GVI(i+1,j,k)]));
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[GVI(i,j-1,k)]));
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[GVI(i,j+1,k)]));
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[GVI(i,j,k-1)]));
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[GVI(i,j,k+1)]));
                    vector = _mm256_mul_ps(vector,aScalar);
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x0[GVI(i,j,k)]));
                    vector = _mm256_div_ps(vector,cScalar);
                    _mm256_storeu_ps(&x[GVI(i,j,k)],vector);
                }
                //If there is any leftover, perform manual solving
                if(i > SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2)){
                    for(i = i - vectorSize; i < (SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2)); i++){
                        /*
                        want to solve the second derivative of the grid
                        x0 stores the first derivative
                        start with x being 0 in all locations
                        then take the difference along x0 and store it in x
                        this is approximating the derivative

                        equation looks something like

                        2nd deriv =   x0    +    
                        
                        */
                        //phi is the scalar potential
                        //want to solve the laplacian
                        //laplacian is derivative of phi over the gid spacing squared along each dimension
                        //the derivative of phi is just phi(x+1) - 2 * phi(x) + phi(x-1) along each axis "x"
                        //we are grabbing phi from the x array
                        x[GVI(i,j,k)] = (x0[GVI(i,j,k)] + a*(x[GVI(i-1,j,k)]+x[GVI(i+1,j,k)]+x[GVI(i,j-1,k)]+x[GVI(i,j+1,k)]+x[GVI(i,j,k-1)]+x[GVI(i,j,k+1)]))/c;
                    }
                }
            }
        }
        fluid_sparse_array_set_bnd(b, x);
    }
}

/**
 * Solves a linear system of equations in a vectorized manner
 * @param b The axis to set the bounds along
 * @param x The array that will contain the solved equations
 * @param x0 The array containing the first order derivatives
*/
LIBRARY_API void fluid_sparse_array_lin_solve_project(int b, float * x, float * x0){
    int i, j, k, l, m;
    int a = 1;
    int c = 6;
    
    __m256 aScalar = _mm256_set1_ps(a);
    __m256 cScalar = _mm256_set1_ps(c);
    int vectorSize = 8;
    // iterate the solver
    for(l = 0; l < LINEARSOLVERTIMES; l++){
        // update for each cell
        for(k = (SPARSE_ARRAY_BORDER_SIZE / 2); k < SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2); k++){
            for(j = (SPARSE_ARRAY_BORDER_SIZE / 2); j < SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2); j++){
                int n = 0;
                //solve as much as possible vectorized
                for(i = (SPARSE_ARRAY_BORDER_SIZE / 2); i < SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2); i = i + vectorSize){
                    __m256 vector = _mm256_loadu_ps(&x[GVI(i-1,j,k)]);
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[GVI(i+1,j,k)]));
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[GVI(i,j-1,k)]));
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[GVI(i,j+1,k)]));
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[GVI(i,j,k-1)]));
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x[GVI(i,j,k+1)]));
                    vector = _mm256_mul_ps(vector,aScalar);
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&x0[GVI(i,j,k)]));
                    vector = _mm256_div_ps(vector,cScalar);
                    _mm256_storeu_ps(&x[GVI(i,j,k)],vector);
                }
                //If there is any leftover, perform manual solving
                if(i > SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2)){
                    for(i = i - vectorSize; i < (SPARSE_ARRAY_RAW_DIM - (SPARSE_ARRAY_BORDER_SIZE / 2)); i++){
                        /*
                        want to solve the second derivative of the grid
                        x0 stores the first derivative
                        start with x being 0 in all locations
                        then take the difference along x0 and store it in x
                        this is approximating the derivative

                        equation looks something like

                        2nd deriv =   x0    +    
                        
                        */
                        //phi is the scalar potential
                        //want to solve the laplacian
                        //laplacian is derivative of phi over the gid spacing squared along each dimension
                        //the derivative of phi is just phi(x+1) - 2 * phi(x) + phi(x-1) along each axis "x"
                        //we are grabbing phi from the x array
                        x[GVI(i,j,k)] = (x0[GVI(i,j,k)] + a*(x[GVI(i-1,j,k)]+x[GVI(i+1,j,k)]+x[GVI(i,j-1,k)]+x[GVI(i,j+1,k)]+x[GVI(i,j,k-1)]+x[GVI(i,j,k+1)]))/c;
                    }
                }
            }
        }
        fluid_sparse_array_set_bnd(b, x);
    }
}

/**
 * Sets the bounds of the simulation
 * @param b The axis to set bounds along
 * @param target The array to set the bounds of
*/
LIBRARY_API void fluid_sparse_array_set_bnd(int b, float * target){
    for(int x=1; x < SPARSE_ARRAY_RAW_DIM-1; x++){
        for(int y = 1; y < SPARSE_ARRAY_RAW_DIM-1; y++){
            //((x)+(DIM)*(y) + (DIM)*(DIM)*(z))
            target[0 + SPARSE_ARRAY_RAW_DIM * x + SPARSE_ARRAY_RAW_DIM * SPARSE_ARRAY_RAW_DIM * y] = 
                b==1 ? 
                    -target[1 + SPARSE_ARRAY_RAW_DIM * x + SPARSE_ARRAY_RAW_DIM * SPARSE_ARRAY_RAW_DIM * y] 
                : 
                    target[1 + SPARSE_ARRAY_RAW_DIM * x + SPARSE_ARRAY_RAW_DIM * SPARSE_ARRAY_RAW_DIM * y]
            ;
            target[IX(SPARSE_ARRAY_RAW_DIM-1,x,y)] =     b==1 ? -target[IX(SPARSE_ARRAY_RAW_DIM-2,x,y)] : target[IX(SPARSE_ARRAY_RAW_DIM-2,x,y)];
            target[IX(x,0,y)] =         b==2 ? -target[IX(x,1,y)] : target[IX(x,1,y)];
            target[IX(x,SPARSE_ARRAY_RAW_DIM-1,y)] =     b==2 ? -target[IX(x,SPARSE_ARRAY_RAW_DIM-2,y)] : target[IX(x,SPARSE_ARRAY_RAW_DIM-2,y)];
            target[IX(x,y,0)] =         b==3 ? -target[IX(x,y,1)] : target[IX(x,y,1)];
            target[IX(x,y,SPARSE_ARRAY_RAW_DIM-1)] =     b==3 ? -target[IX(x,y,SPARSE_ARRAY_RAW_DIM-2)] : target[IX(x,y,SPARSE_ARRAY_RAW_DIM-2)];
        }
    }
    for(int x = 1; x < SPARSE_ARRAY_RAW_DIM-1; x++){
        target[IX(x,0,0)]         = (float)(0.5f * (target[IX(x,1,0)] + target[IX(x,0,1)]));
        target[IX(x,SPARSE_ARRAY_RAW_DIM-1,0)]     = (float)(0.5f * (target[IX(x,SPARSE_ARRAY_RAW_DIM-2,0)] + target[IX(x,SPARSE_ARRAY_RAW_DIM-1,1)]));
        target[IX(x,0,SPARSE_ARRAY_RAW_DIM-1)]     = (float)(0.5f * (target[IX(x,1,SPARSE_ARRAY_RAW_DIM-1)] + target[IX(x,0,SPARSE_ARRAY_RAW_DIM-2)]));
        target[IX(x,SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-1)] = (float)(0.5f * (target[IX(x,SPARSE_ARRAY_RAW_DIM-2,SPARSE_ARRAY_RAW_DIM-1)] + target[IX(x,SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-2)]));

        target[IX(0,x,0)] = (float)(0.5f * (target[IX(1,x,0)] + target[IX(0,x,1)]));
        target[IX(SPARSE_ARRAY_RAW_DIM-1,x,0)] = (float)(0.5f * (target[IX(SPARSE_ARRAY_RAW_DIM-2,x,0)] + target[IX(SPARSE_ARRAY_RAW_DIM-1,x,1)]));
        target[IX(0,x,SPARSE_ARRAY_RAW_DIM-1)] = (float)(0.5f * (target[IX(1,x,SPARSE_ARRAY_RAW_DIM-1)] + target[IX(0,x,SPARSE_ARRAY_RAW_DIM-2)]));
        target[IX(SPARSE_ARRAY_RAW_DIM-1,x,SPARSE_ARRAY_RAW_DIM-1)] = (float)(0.5f * (target[IX(SPARSE_ARRAY_RAW_DIM-2,x,SPARSE_ARRAY_RAW_DIM-1)] + target[IX(SPARSE_ARRAY_RAW_DIM-1,x,SPARSE_ARRAY_RAW_DIM-2)]));


        target[IX(0,0,x)] = (float)(0.5f * (target[IX(1,0,x)] + target[IX(0,1,x)]));
        target[IX(SPARSE_ARRAY_RAW_DIM-1,0,x)] = (float)(0.5f * (target[IX(SPARSE_ARRAY_RAW_DIM-2,0,x)] + target[IX(SPARSE_ARRAY_RAW_DIM-1,1,x)]));
        target[IX(0,SPARSE_ARRAY_RAW_DIM-1,x)] = (float)(0.5f * (target[IX(1,SPARSE_ARRAY_RAW_DIM-1,x)] + target[IX(0,SPARSE_ARRAY_RAW_DIM-2,x)]));
        target[IX(SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-1,x)] = (float)(0.5f * (target[IX(SPARSE_ARRAY_RAW_DIM-2,SPARSE_ARRAY_RAW_DIM-1,x)] + target[IX(SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-2,x)]));

    }
    target[IX(0,0,0)]             = (float)((target[IX(1,0,0)]+target[IX(0,1,0)]+target[IX(0,0,1)])/3.0);
    target[IX(SPARSE_ARRAY_RAW_DIM-1,0,0)]         = (float)((target[IX(SPARSE_ARRAY_RAW_DIM-2,0,0)]+target[IX(SPARSE_ARRAY_RAW_DIM-1,1,0)]+target[IX(SPARSE_ARRAY_RAW_DIM-1,0,1)])/3.0);
    target[IX(0,SPARSE_ARRAY_RAW_DIM-1,0)]         = (float)((target[IX(1,SPARSE_ARRAY_RAW_DIM-1,0)]+target[IX(0,SPARSE_ARRAY_RAW_DIM-2,0)]+target[IX(0,SPARSE_ARRAY_RAW_DIM-1,1)])/3.0);
    target[IX(0,0,SPARSE_ARRAY_RAW_DIM-1)]         = (float)((target[IX(0,0,SPARSE_ARRAY_RAW_DIM-2)]+target[IX(1,0,SPARSE_ARRAY_RAW_DIM-1)]+target[IX(0,1,SPARSE_ARRAY_RAW_DIM-1)])/3.0);
    target[IX(SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-1,0)]     = (float)((target[IX(SPARSE_ARRAY_RAW_DIM-2,SPARSE_ARRAY_RAW_DIM-1,0)]+target[IX(SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-2,0)]+target[IX(SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-1,1)])/3.0);
    target[IX(0,SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-1)]     = (float)((target[IX(1,SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-1)]+target[IX(0,SPARSE_ARRAY_RAW_DIM-2,SPARSE_ARRAY_RAW_DIM-1)]+target[IX(0,SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-2)])/3.0);
    target[IX(SPARSE_ARRAY_RAW_DIM-1,0,SPARSE_ARRAY_RAW_DIM-1)]     = (float)((target[IX(SPARSE_ARRAY_RAW_DIM-1,0,SPARSE_ARRAY_RAW_DIM-2)]+target[IX(SPARSE_ARRAY_RAW_DIM-2,0,SPARSE_ARRAY_RAW_DIM-1)]+target[IX(SPARSE_ARRAY_RAW_DIM-1,1,SPARSE_ARRAY_RAW_DIM-1)])/3.0);
    target[IX(SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-1)] = (float)((target[IX(SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-2)]+target[IX(SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-2,SPARSE_ARRAY_RAW_DIM-1)]+target[IX(SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-1,SPARSE_ARRAY_RAW_DIM-2)])/3.0);
}