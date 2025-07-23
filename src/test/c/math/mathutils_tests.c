
#include "math/mathutils.h"
#include "../util/test.h"


int math_mathutils_tests(){
    int rVal = 0;

    //test fract()
    rVal += assertEqualsFloat(0.5,fract(1.5),"Fract failed to calculate correctly! %f %f \n");
    rVal += assertEqualsFloat(0.0,fract(1.0),"Fract failed to calculate correctly! %f %f \n");

    //test dot2()
    rVal += assertEqualsFloat(1,dot2(1,0,1,0),"dot2 should be 1! %f %f \n");
    rVal += assertEqualsFloat(-1,dot2(1,0,-1,0),"dot2 should be -1! %f %f \n");
    rVal += assertEqualsFloat(0,dot2(1,0,0,1),"dot2 should be 0! %f %f \n");


    //test dot3()
    rVal += assertEqualsFloat(1,dot3(1,0,0,1,0,0),"dot3 should be 1! %f %f \n");
    rVal += assertEqualsFloat(-1,dot3(1,0,0,-1,0,0),"dot3 should be -1! %f %f \n");
    rVal += assertEqualsFloat(0,dot3(1,0,0,0,1,0),"dot3 should be 0! %f %f \n");
    

    return rVal;
}