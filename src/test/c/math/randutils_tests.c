
#include "math/mathutils.h"
#include "math/randutils.h"
#include "../util/test.h"


int math_randutils_tests(){
    int rVal = 0;

    randutils_rand1(1);
    randutils_rand2(1,2);
    randutils_rand3(1,2,3);

    return rVal;
}