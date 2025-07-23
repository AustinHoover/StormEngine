#include <stdlib.h>
#include <stdio.h>

#define STB_DS_IMPLEMENTATION
#include "stb/stb_ds.h"

#include "test.h"

int assertEquals(int a, int b, char * msg){
    int rVal = (a != b);
    if(rVal){
        printf(msg, a, b);
        fflush(stdout);
    }
    return rVal;
}

int assertEqualsPtr(void * a, void * b, char * msg){
    int rVal = (a != b);
    if(rVal){
        printf(msg, a, b);
        fflush(stdout);
    }
    return rVal;
}

int assertEqualsFloat(float a, float b, char * msg){
    int rVal = (a != b);
    if(rVal){
        printf(msg, a, b);
        fflush(stdout);
    }
    return rVal;
}

int assertNotEquals(int a, int b, char * msg){
    int rVal = (a == b);
    if(rVal){
        printf(msg, a, b);
        fflush(stdout);
    }
    return rVal;
}

int assertNotEqualsPtr(void * a, void * b, char * msg){
    int rVal = (a == b);
    if(rVal){
        printf(msg, a, b);
        fflush(stdout);
    }
    return rVal;
}

int assertNotEqualsFloat(float a, float b, char * msg){
    int rVal = (a == b);
    if(rVal){
        printf(msg, a, b);
        fflush(stdout);
    }
    return rVal;
}


int util_test(){
    return 0;
}

