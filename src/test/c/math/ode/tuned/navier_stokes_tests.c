#include <math.h>
#include <stdlib.h>

#include "math/ode/tuned/navier_stokes.h"
#include "math/ode/gauss_seidel.h"
#include "../../../util/test.h"

#define NAVIER_STOKES_TESTS_ERROR_MARGIN 0.00001f
#define NAVIER_STOKES_TESTS_MAX_ITERATIONS 500

int math_ode_tuned_navier_stokes_test_residual(){
    printf("math_ode_tuned_navier_stokes_test_residual \n");
    int rVal = 0;
    int GRIDDIM = 4;

    float * phi = (float *)calloc(1,GRIDDIM*GRIDDIM*GRIDDIM*sizeof(float));
    float * phi0 = (float *)calloc(1,GRIDDIM*GRIDDIM*GRIDDIM*sizeof(float));
    float expected, actual;

    //
    //
    //
    phi0[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 1.0f;
    expected = 1.0f;
    actual = solver_navier_stokes_get_residual(phi,phi0,1,1,1,GRIDDIM);
    if(fabs(expected - actual) > NAVIER_STOKES_TESTS_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Calculated incorrect residual!  expected: %f    actual: %f  \n");
    }


    //
    //
    //
    phi[solver_gauss_seidel_get_index(2,1,1,GRIDDIM)] = 1.0f;
    phi[solver_gauss_seidel_get_index(0,1,1,GRIDDIM)] = 1.0f;
    phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 2.0f;
    phi0[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 0.0f;
    expected = -10.0f;
    actual = solver_navier_stokes_get_residual(phi,phi0,1,1,1,GRIDDIM);
    if(fabs(expected - actual) > NAVIER_STOKES_TESTS_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Calculated incorrect residual!  expected: %f    actual: %f  \n");
    }

    //
    //
    //
    phi[solver_gauss_seidel_get_index(2,1,1,GRIDDIM)] = 1.0f;
    phi[solver_gauss_seidel_get_index(0,1,1,GRIDDIM)] = 1.0f;
    phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = -2.0f;
    phi0[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 0.0f;
    expected = 14.0f;
    actual = solver_navier_stokes_get_residual(phi,phi0,1,1,1,GRIDDIM);
    if(fabs(expected - actual) > NAVIER_STOKES_TESTS_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Calculated incorrect residual!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

int math_ode_tuned_navier_stokes_test_approximation(){
    printf("math_ode_tuned_navier_stokes_test_approximation \n");
    int rVal = 0;
    int GRIDDIM = 4;

    float * phi = (float *)calloc(1,GRIDDIM*GRIDDIM*GRIDDIM*sizeof(float));
    float * phi0 = (float *)calloc(1,GRIDDIM*GRIDDIM*GRIDDIM*sizeof(float));
    float expected, actual;

    //
    //
    //
    phi0[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 1.0f;
    expected = 0.166666f;
    solver_navier_stokes_approximate(phi,phi0,1,1,1,GRIDDIM);
    actual = phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)];
    if(fabs(expected - actual) > NAVIER_STOKES_TESTS_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Calculated incorrect approximation!  expected: %f    actual: %f  \n");
    }

    //
    //
    //
    phi0[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 1.0f;
    phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 0.0f;
    expected = 0.166666f;
    solver_navier_stokes_approximate(phi,phi0,1,1,1,GRIDDIM);
    solver_navier_stokes_approximate(phi,phi0,1,1,1,GRIDDIM);
    solver_navier_stokes_approximate(phi,phi0,1,1,1,GRIDDIM);
    solver_navier_stokes_approximate(phi,phi0,1,1,1,GRIDDIM);
    actual = phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)];
    if(fabs(expected - actual) > NAVIER_STOKES_TESTS_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Calculated incorrect approximation!  expected: %f    actual: %f  \n");
    }

    //
    //
    //
    phi0[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 1.0f;
    phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 0.0f;
    phi[solver_gauss_seidel_get_index(0,1,1,GRIDDIM)] = 1.0f;
    expected = 0.3333333f;
    solver_navier_stokes_approximate(phi,phi0,1,1,1,GRIDDIM);
    actual = phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)];
    if(fabs(expected - actual) > NAVIER_STOKES_TESTS_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Calculated incorrect approximation!  expected: %f    actual: %f  \n");
    }

    //
    //
    //
    phi0[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 1.0f;
    phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 1.0f;
    phi[solver_gauss_seidel_get_index(0,1,1,GRIDDIM)] = 1.0f;
    expected = 0.333333f;
    solver_navier_stokes_approximate(phi,phi0,1,1,1,GRIDDIM);
    actual = phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)];
    if(fabs(expected - actual) > NAVIER_STOKES_TESTS_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Calculated incorrect approximation!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

int math_ode_tuned_navier_stokes_test_approximation_and_residual(){
    printf("math_ode_tuned_navier_stokes_test_approximation_and_residual \n");
    int rVal = 0;

    int GRIDDIM = 4;
    float * phi = (float *)calloc(1,GRIDDIM*GRIDDIM*GRIDDIM*sizeof(float));
    float * phi0 = (float *)calloc(1,GRIDDIM*GRIDDIM*GRIDDIM*sizeof(float));
    float expected, actual;

    //
    //
    //
    phi0[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 1.0f;
    solver_navier_stokes_approximate(phi,phi0,1,1,1,GRIDDIM);
    expected = 0.0f;
    actual = solver_navier_stokes_get_residual(phi,phi0,1,1,1,GRIDDIM);
    if(fabs(expected - actual) > NAVIER_STOKES_TESTS_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Calculated incorrect residual!  expected: %f    actual: %f  \n");
    }

    //
    //
    //
    phi0[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 1.0f;
    phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 1.0f;
    phi[solver_gauss_seidel_get_index(0,1,1,GRIDDIM)] = 1.0f;
    expected = 0.0f;
    solver_navier_stokes_approximate(phi,phi0,1,1,1,GRIDDIM);
    actual = solver_navier_stokes_get_residual(phi,phi0,1,1,1,GRIDDIM);
    if(fabs(expected - actual) > NAVIER_STOKES_TESTS_ERROR_MARGIN){
        rVal += assertEqualsFloat(expected,actual,"Calculated incorrect approximation!  expected: %f    actual: %f  \n");
    }

    return rVal;
}

int math_ode_tuned_navier_stokes_test_convergence_4(){
    printf("math_ode_tuned_navier_stokes_test_convergence_4 \n");
    int rVal = 0;

    int GRIDDIM = 4;
    float * phi = (float *)calloc(1,GRIDDIM*GRIDDIM*GRIDDIM*sizeof(float));
    float * phi0 = (float *)calloc(1,GRIDDIM*GRIDDIM*GRIDDIM*sizeof(float));
    float expected, actual;
    int iteration = 0;
    float residual = 1.0f;

    //
    //
    //
    phi0[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] = 1.0f;
    residual = 1.0f;
    iteration = 0;
    while(residual > NAVIER_STOKES_TESTS_ERROR_MARGIN && iteration < NAVIER_STOKES_TESTS_MAX_ITERATIONS){
        residual = solver_navier_stokes_iterate(phi,phi0,GRIDDIM);
        iteration++;
    }
    if(residual > NAVIER_STOKES_TESTS_ERROR_MARGIN){
        rVal++;
        printf("Failed to converge! \n");
        printf("Residual: %f \n", residual);
        printf("iterations: %d \n",iteration);
        printf("%f %f  \n", phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)], phi[solver_gauss_seidel_get_index(1,1,2,GRIDDIM)]);
        printf("%f %f  \n", phi[solver_gauss_seidel_get_index(2,1,1,GRIDDIM)], phi[solver_gauss_seidel_get_index(2,1,2,GRIDDIM)]);
        solver_navier_stokes_approximate(phi, phi0, 1, 1, 1, GRIDDIM);
        printf("%f \n",phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)]);
        printf("\n");
    }

    return rVal;
}


int math_ode_tuned_navier_stokes_test_convergence_6(){
    printf("math_ode_tuned_navier_stokes_test_convergence_6 \n");
    int rVal = 0;

    int GRIDDIM = 6;
    float * phi = (float *)calloc(1,GRIDDIM*GRIDDIM*GRIDDIM*sizeof(float));
    float * phi0 = (float *)calloc(1,GRIDDIM*GRIDDIM*GRIDDIM*sizeof(float));
    float expected, actual;
    int iteration = 0;
    float residual = 1.0f;

    //
    //
    //
    phi0[solver_gauss_seidel_get_index(3,1,1,GRIDDIM)] = 1.0f;
    phi0[solver_gauss_seidel_get_index(2,1,1,GRIDDIM)] = -1.0f;
    for(int x = 0; x < GRIDDIM; x++){
        for(int y = 0; y < GRIDDIM; y++){
            for(int z = 0; z < GRIDDIM; z++){
                phi[solver_gauss_seidel_get_index(x,y,z,GRIDDIM)] = 0;
            }
        }
    }
    residual = 1.0f;
    iteration = 0;
    while(residual > NAVIER_STOKES_TESTS_ERROR_MARGIN && iteration < NAVIER_STOKES_TESTS_MAX_ITERATIONS){
        residual = solver_navier_stokes_iterate(phi,phi0,GRIDDIM);
        //have to set borders for the iterator to work
        for(int x = 0; x < GRIDDIM; x++){
            for(int y = 0; y < GRIDDIM; y++){
                phi[solver_gauss_seidel_get_index(0,x,y,GRIDDIM)] = phi[solver_gauss_seidel_get_index(1,x,y,GRIDDIM)];
                phi[solver_gauss_seidel_get_index(x,0,y,GRIDDIM)] = phi[solver_gauss_seidel_get_index(x,1,y,GRIDDIM)];
                phi[solver_gauss_seidel_get_index(x,y,0,GRIDDIM)] = phi[solver_gauss_seidel_get_index(x,y,1,GRIDDIM)];
                phi[solver_gauss_seidel_get_index(GRIDDIM-1,x,y,GRIDDIM)] = phi[solver_gauss_seidel_get_index(GRIDDIM-2,x,y,GRIDDIM)];
                phi[solver_gauss_seidel_get_index(x,GRIDDIM-1,y,GRIDDIM)] = phi[solver_gauss_seidel_get_index(x,GRIDDIM-2,y,GRIDDIM)];
                phi[solver_gauss_seidel_get_index(x,y,GRIDDIM-1,GRIDDIM)] = phi[solver_gauss_seidel_get_index(x,y,GRIDDIM-2,GRIDDIM)];
            }
        }
        iteration++;
    }
    if(residual > NAVIER_STOKES_TESTS_ERROR_MARGIN){
        rVal++;
        printf("Failed to converge! \n");
        printf("Residual: %f \n", residual);
        printf("iterations: %d \n",iteration);
        printf("%f %f %f %f  \n", phi[solver_gauss_seidel_get_index(0,1,0,GRIDDIM)], phi[solver_gauss_seidel_get_index(1,1,0,GRIDDIM)], phi[solver_gauss_seidel_get_index(2,1,0,GRIDDIM)], phi[solver_gauss_seidel_get_index(3,1,0,GRIDDIM)]);
        printf("%f %f %f %f  \n", phi[solver_gauss_seidel_get_index(0,1,1,GRIDDIM)], phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)], phi[solver_gauss_seidel_get_index(2,1,1,GRIDDIM)], phi[solver_gauss_seidel_get_index(3,1,1,GRIDDIM)]);
        printf("%f %f %f %f  \n", phi[solver_gauss_seidel_get_index(0,1,2,GRIDDIM)], phi[solver_gauss_seidel_get_index(1,1,2,GRIDDIM)], phi[solver_gauss_seidel_get_index(2,1,2,GRIDDIM)], phi[solver_gauss_seidel_get_index(3,1,2,GRIDDIM)]);
        printf("%f %f %f %f  \n", phi[solver_gauss_seidel_get_index(0,1,3,GRIDDIM)], phi[solver_gauss_seidel_get_index(1,1,3,GRIDDIM)], phi[solver_gauss_seidel_get_index(2,1,3,GRIDDIM)], phi[solver_gauss_seidel_get_index(3,1,3,GRIDDIM)]);
        solver_navier_stokes_approximate(phi, phi0, 1, 1, 1, GRIDDIM);
        printf("%f \n",phi[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)]);
        printf("\n");
    }

    return rVal;
}


int math_ode_tuned_navier_stokes_tests(){
    int rVal = 0;

    rVal += math_ode_tuned_navier_stokes_test_residual();
    rVal += math_ode_tuned_navier_stokes_test_approximation();
    rVal += math_ode_tuned_navier_stokes_test_approximation_and_residual();
    rVal += math_ode_tuned_navier_stokes_test_convergence_4();
    rVal += math_ode_tuned_navier_stokes_test_convergence_6();

    return rVal;
}

