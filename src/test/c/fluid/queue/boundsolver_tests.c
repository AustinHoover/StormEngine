
#include "stb/stb_ds.h"

#include "fluid/dispatch/dispatcher.h"
#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"

#include "../../util/chunk_test_utils.h"
#include "../../util/test.h"


/**
 * Checks if the bounds were set correctly for a given chunk
 * @param chunk The chunk
 * @param x the world x
 * @param y the world y
 * @param z the world z
 * @param invert Inverts the check (ie for validating data prior to call)
 */
int checkBounds(Chunk * chunk, int x, int y, int z, int invert){
    int rVal = 0;
    int i, j;
    int neighborIndex;
    //
    //check planes
    //

    //x+ plane
    neighborIndex = CK(2,1,1);
    if(x > 1){
        for(i = 1; i < DIM - 1; i++){
            for(j = 1; j < DIM - 1; j++){
                if(invert == 1){
                    rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(DIM-1,i,j)],0,"chunk should NOT have (x=2) bound set to 0 -- %d %d \n");
                } else {
                    rVal += assertEquals(chunk->d[CENTER_LOC][IX(DIM-1,i,j)],0,"chunk should have (x=2) bound set to 0-- %d %d \n");
                }
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x<2\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            for(j = 1; j < DIM - 1; j++){
                if(invert == 1){
                    pass = chunk->d[CENTER_LOC][IX(DIM-1,i,j)] != chunk->d[neighborIndex][IX(1,i,j)];
                } else {
                    pass = chunk->d[CENTER_LOC][IX(DIM-1,i,j)] == chunk->d[neighborIndex][IX(1,i,j)];
                }
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x!=2) bound set to neighbor\n");
        }
    }

    //x- plane
    neighborIndex = CK(0,1,1);
    if(x < 1){
        for(i = 1; i < DIM - 1; i++){
            for(j = 1; j < DIM - 1; j++){
                if(invert == 1){
                    rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(0,i,j)],0,"chunk should NOT have (x=0) bound set to 0 -- %d %d \n");
                } else {
                    rVal += assertEquals(chunk->d[CENTER_LOC][IX(0,i,j)],0,"chunk should have (x=0) bound set to 0-- %d %d \n");
                }
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x>0\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            for(j = 1; j < DIM - 1; j++){
                if(invert == 1){
                    pass = chunk->d[CENTER_LOC][IX(0,i,j)] != chunk->d[neighborIndex][IX(DIM-2,i,j)];
                } else {
                    pass = chunk->d[CENTER_LOC][IX(0,i,j)] == chunk->d[neighborIndex][IX(DIM-2,i,j)];
                }
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x!=0) bound set to neighbor\n");
        }
    }

    //y+ plane
    neighborIndex = CK(1,2,1);
    if(y > 1){
        for(i = 1; i < DIM - 1; i++){
            for(j = 1; j < DIM - 1; j++){
                if(invert == 1){
                    rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(i,DIM-1,j)],0,"chunk should NOT have (y=2) bound set to 0 -- %d %d \n");
                } else {
                    rVal += assertEquals(chunk->d[CENTER_LOC][IX(i,DIM-1,j)],0,"chunk should have (y=2) bound set to 0-- %d %d \n");
                }
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly y<2\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            for(j = 1; j < DIM - 1; j++){
                if(invert == 1){
                    pass = chunk->d[CENTER_LOC][IX(i,DIM-1,j)] != chunk->d[neighborIndex][IX(i,1,j)];
                } else {
                    pass = chunk->d[CENTER_LOC][IX(i,DIM-1,j)] == chunk->d[neighborIndex][IX(i,1,j)];
                }
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (y!=2) bound set to neighbor\n");
        }
    }

    //y- plane
    neighborIndex = CK(1,0,1);
    if(y < 1){
        for(i = 1; i < DIM - 1; i++){
            for(j = 1; j < DIM - 1; j++){
                if(invert == 1){
                    rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(i,0,j)],0,"chunk should NOT have (y=0) bound set to 0 -- %d %d \n");
                } else {
                    rVal += assertEquals(chunk->d[CENTER_LOC][IX(i,0,j)],0,"chunk should have (y=0) bound set to 0-- %d %d \n");
                }
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly y>0\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            for(j = 1; j < DIM - 1; j++){
                if(invert == 1){
                    pass = chunk->d[CENTER_LOC][IX(i,0,j)] != chunk->d[neighborIndex][IX(i,DIM-2,j)];
                } else {
                    pass = chunk->d[CENTER_LOC][IX(i,0,j)] == chunk->d[neighborIndex][IX(i,DIM-2,j)];
                }
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (y!=0) bound set to neighbor\n");
        }
    }

    //z+ plane
    neighborIndex = CK(1,1,2);
    if(z > 1){
        for(i = 1; i < DIM - 1; i++){
            for(j = 1; j < DIM - 1; j++){
                if(invert == 1){
                    rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(i,j,DIM-1)],0,"chunk should NOT have (z=2) bound set to 0 -- %d %d \n");
                } else {
                    rVal += assertEquals(chunk->d[CENTER_LOC][IX(i,j,DIM-1)],0,"chunk should have (z=2) bound set to 0-- %d %d \n");
                }
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly z<2\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            for(j = 1; j < DIM - 1; j++){
                if(invert == 1){
                    pass = chunk->d[CENTER_LOC][IX(i,j,DIM-1)] != chunk->d[neighborIndex][IX(i,j,1)];
                } else {
                    pass = chunk->d[CENTER_LOC][IX(i,j,DIM-1)] == chunk->d[neighborIndex][IX(i,j,1)];
                }
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (z!=2) bound set to neighbor\n");
        }
    }

    //z- plane
    neighborIndex = CK(1,1,0);
    if(z < 1){
        for(i = 1; i < DIM - 1; i++){
            for(j = 1; j < DIM - 1; j++){
                if(invert == 1){
                    rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(i,j,0)],0,"chunk should NOT have (z=0) bound set to 0 -- %d %d \n");
                } else {
                    rVal += assertEquals(chunk->d[CENTER_LOC][IX(i,j,0)],0,"chunk should have (z=0) bound set to 0-- %d %d \n");
                }
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly z>0\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            for(j = 1; j < DIM - 1; j++){
                if(invert == 1){
                    pass = chunk->d[CENTER_LOC][IX(i,j,0)] != chunk->d[neighborIndex][IX(i,j,DIM-2)];
                } else {
                    pass = chunk->d[CENTER_LOC][IX(i,j,0)] == chunk->d[neighborIndex][IX(i,j,DIM-2)];
                }
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (z!=0) bound set to neighbor\n");
        }
    }

    //
    // edges
    //

    //x+ y+ edge
    neighborIndex = CK(2,2,1);
    if(x == 2 || y == 2){
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(DIM-1,DIM-1,i)],0,"chunk should NOT have (x=2 y=2) bound set to 0 -- %d %d \n");
            } else {
                rVal += assertEquals(chunk->d[CENTER_LOC][IX(DIM-1,DIM-1,i)],0,"chunk should have (x=2 y=2) bound set to 0-- %d %d \n");
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x<2 y<2\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                pass = chunk->d[CENTER_LOC][IX(DIM-1,DIM-1,i)] != chunk->d[neighborIndex][IX(1,1,i)];
            } else {
                pass = chunk->d[CENTER_LOC][IX(DIM-1,DIM-1,i)] == chunk->d[neighborIndex][IX(1,1,i)];
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=2 y=2) bound set to neighbor\n");
        }
    }

    //x+ y- edge
    neighborIndex = CK(2,0,1);
    if(x == 2 || y == 0){
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(DIM-1,0,i)],0,"chunk should NOT have (x=2 y=0) bound set to 0 -- %d %d \n");
            } else {
                rVal += assertEquals(chunk->d[CENTER_LOC][IX(DIM-1,0,i)],0,"chunk should have (x=2 y=0) bound set to 0-- %d %d \n");
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x<2 y>0\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                pass = chunk->d[CENTER_LOC][IX(DIM-1,0,i)] != chunk->d[neighborIndex][IX(1,DIM-2,i)];
            } else {
                pass = chunk->d[CENTER_LOC][IX(DIM-1,0,i)] == chunk->d[neighborIndex][IX(1,DIM-2,i)];
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=2 y=0) bound set to neighbor\n");
        }
    }

    //x- y+ edge
    neighborIndex = CK(0,2,1);
    if(x == 0 || y == 2){
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(0,DIM-1,i)],0,"chunk should NOT have (x=0 y=2) bound set to 0 -- %d %d \n");
            } else {
                rVal += assertEquals(chunk->d[CENTER_LOC][IX(0,DIM-1,i)],0,"chunk should have (x=0 y=2) bound set to 0-- %d %d \n");
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x>0 y<2\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                pass = chunk->d[CENTER_LOC][IX(0,DIM-1,i)] != chunk->d[neighborIndex][IX(1,1,i)];
            } else {
                pass = chunk->d[CENTER_LOC][IX(0,DIM-1,i)] == chunk->d[neighborIndex][IX(1,1,i)];
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=0 y=2) bound set to neighbor\n");
        }
    }

    //x- y- edge
    neighborIndex = CK(0,0,1);
    if(x == 0 || y == 0){
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(0,0,i)],0,"chunk should NOT have (x=0 y=0) bound set to 0 -- %d %d \n");
            } else {
                rVal += assertEquals(chunk->d[CENTER_LOC][IX(0,0,i)],0,"chunk should have (x=0 y=0) bound set to 0-- %d %d \n");
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x>0 y>0\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                pass = chunk->d[CENTER_LOC][IX(0,0,i)] != chunk->d[neighborIndex][IX(DIM-2,DIM-2,i)];
            } else {
                pass = chunk->d[CENTER_LOC][IX(0,0,i)] == chunk->d[neighborIndex][IX(DIM-2,DIM-2,i)];
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=0 y=0) bound set to neighbor\n");
        }
    }











    //x+ z+ edge
    neighborIndex = CK(2,1,2);
    if(x == 2 || z == 2){
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(DIM-1,i,DIM-1)],0,"chunk should NOT have (x=2 z=2) bound set to 0 -- %d %d \n");
            } else {
                rVal += assertEquals(chunk->d[CENTER_LOC][IX(DIM-1,i,DIM-1)],0,"chunk should have (x=2 z=2) bound set to 0-- %d %d \n");
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x<2 z<2\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                pass = chunk->d[CENTER_LOC][IX(DIM-1,i,DIM-1)] != chunk->d[neighborIndex][IX(1,i,1)];
            } else {
                pass = chunk->d[CENTER_LOC][IX(DIM-1,i,DIM-1)] == chunk->d[neighborIndex][IX(1,i,1)];
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=2 z=2) bound set to neighbor\n");
        }
    }

    //x+ z- edge
    neighborIndex = CK(2,1,0);
    if(x == 2 || z == 0){
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(DIM-1,i,0)],0,"chunk should NOT have (x=2 z=0) bound set to 0 -- %d %d \n");
            } else {
                rVal += assertEquals(chunk->d[CENTER_LOC][IX(DIM-1,i,0)],0,"chunk should have (x=2 z=0) bound set to 0-- %d %d \n");
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x<2 z>0\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                pass = chunk->d[CENTER_LOC][IX(DIM-1,i,0)] != chunk->d[neighborIndex][IX(1,i,DIM-2)];
            } else {
                pass = chunk->d[CENTER_LOC][IX(DIM-1,i,0)] == chunk->d[neighborIndex][IX(1,i,DIM-2)];
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=2 z=0) bound set to neighbor\n");
        }
    }

    //x- z+ edge
    neighborIndex = CK(0,1,2);
    if(x == 0 || z == 2){
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(0,i,DIM-1)],0,"chunk should NOT have (x=0 z=2) bound set to 0 -- %d %d \n");
            } else {
                rVal += assertEquals(chunk->d[CENTER_LOC][IX(0,i,DIM-1)],0,"chunk should have (x=0 z=2) bound set to 0-- %d %d \n");
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x>0 z<2\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                pass = chunk->d[CENTER_LOC][IX(0,i,DIM-1)] != chunk->d[neighborIndex][IX(1,i,1)];
            } else {
                pass = chunk->d[CENTER_LOC][IX(0,i,DIM-1)] == chunk->d[neighborIndex][IX(1,i,1)];
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=0 z=2) bound set to neighbor\n");
        }
    }

    //x- z- edge
    neighborIndex = CK(0,1,0);
    if(x == 0 || z == 0){
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(0,i,0)],0,"chunk should NOT have (x=0 z=0) bound set to 0 -- %d %d \n");
            } else {
                rVal += assertEquals(chunk->d[CENTER_LOC][IX(0,i,0)],0,"chunk should have (x=0 z=0) bound set to 0-- %d %d \n");
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x>0 z>0\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                pass = chunk->d[CENTER_LOC][IX(0,i,0)] != chunk->d[neighborIndex][IX(DIM-2,i,DIM-2)];
            } else {
                pass = chunk->d[CENTER_LOC][IX(0,i,0)] == chunk->d[neighborIndex][IX(DIM-2,i,DIM-2)];
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=0 z=0) bound set to neighbor\n");
        }
    }












































    //y+ z+ edge
    neighborIndex = CK(1,2,2);
    if(y == 2 || z == 2){
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(i,DIM-1,DIM-1)],0,"chunk should NOT have (y=2 z=2) bound set to 0 -- %d %d \n");
            } else {
                rVal += assertEquals(chunk->d[CENTER_LOC][IX(i,DIM-1,DIM-1)],0,"chunk should have (y=2 z=2) bound set to 0-- %d %d \n");
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly y<2 z<2\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                pass = chunk->d[CENTER_LOC][IX(i,DIM-1,DIM-1)] != chunk->d[neighborIndex][IX(i,1,1)];
            } else {
                pass = chunk->d[CENTER_LOC][IX(i,DIM-1,DIM-1)] == chunk->d[neighborIndex][IX(i,1,1)];
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (y=2 z=2) bound set to neighbor\n");
        }
    }

    //y+ z- edge
    neighborIndex = CK(1,2,0);
    if(y == 2 || z == 0){
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(i,DIM-1,0)],0,"chunk should NOT have (y=2 z=0) bound set to 0 -- %d %d \n");
            } else {
                rVal += assertEquals(chunk->d[CENTER_LOC][IX(i,DIM-1,0)],0,"chunk should have (y=2 z=0) bound set to 0-- %d %d \n");
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly y<2 z>0\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                pass = chunk->d[CENTER_LOC][IX(i,DIM-1,0)] != chunk->d[neighborIndex][IX(i,1,DIM-2)];
            } else {
                pass = chunk->d[CENTER_LOC][IX(i,DIM-1,0)] == chunk->d[neighborIndex][IX(i,1,DIM-2)];
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (y=2 z=0) bound set to neighbor\n");
        }
    }

    //y- z+ edge
    neighborIndex = CK(1,0,2);
    if(y == 0 || z == 2){
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(i,0,DIM-1)],0,"chunk should NOT have (y=0 z=2) bound set to 0 -- %d %d \n");
            } else {
                rVal += assertEquals(chunk->d[CENTER_LOC][IX(i,0,DIM-1)],0,"chunk should have (y=0 z=2) bound set to 0-- %d %d \n");
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly y>0 z<2\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                pass = chunk->d[CENTER_LOC][IX(i,0,DIM-1)] != chunk->d[neighborIndex][IX(i,1,1)];
            } else {
                pass = chunk->d[CENTER_LOC][IX(i,0,DIM-1)] == chunk->d[neighborIndex][IX(i,1,1)];
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (y=0 z=2) bound set to neighbor\n");
        }
    }

    //y- z- edge
    neighborIndex = CK(1,0,0);
    if(y == 0 || z == 0){
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                rVal += assertNotEquals(chunk->d[CENTER_LOC][IX(i,0,0)],0,"chunk should NOT have (y=0 z=0) bound set to 0 -- %d %d \n");
            } else {
                rVal += assertEquals(chunk->d[CENTER_LOC][IX(i,0,0)],0,"chunk should have (y=0 z=0) bound set to 0-- %d %d \n");
            }
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly y>0 z>0\n");
        }
        int pass = 0;
        for(i = 1; i < DIM - 1; i++){
            if(invert == 1){
                pass = chunk->d[CENTER_LOC][IX(i,0,0)] != chunk->d[neighborIndex][IX(i,DIM-2,DIM-2)];
            } else {
                pass = chunk->d[CENTER_LOC][IX(i,0,0)] == chunk->d[neighborIndex][IX(i,DIM-2,DIM-2)];
            }
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (y=0 z=0) bound set to neighbor\n");
        }
    }






    //
    // CORNERS
    //

    //x+ y+ z+
    neighborIndex = CK(2,2,2);
    if(x == 2 || y == 2 || z == 2){
        if(invert == 1){
            rVal += assertNotEquals(chunk->d[CENTER_LOC][IX( DIM-1, DIM-1, DIM-1 )],0,"chunk should NOT have (x=2 y=2 z=2) bound set to 0 -- %d %d \n");
        } else {
            rVal += assertEquals(chunk->d[CENTER_LOC][IX( DIM-1, DIM-1, DIM-1 )],0,"chunk should have (x=2 y=2 z=2) bound set to 0-- %d %d \n");
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x<2 y<2 z<2\n");
        }
        int pass = 0;
        if(invert == 1){
            pass = chunk->d[CENTER_LOC][IX( DIM-1, DIM-1, DIM-1 )] != chunk->d[neighborIndex][IX(    1,    1,    1)];
        } else {
            pass = chunk->d[CENTER_LOC][IX( DIM-1, DIM-1, DIM-1 )] == chunk->d[neighborIndex][IX(    1,    1,    1)];
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=2 y=2 z=2) bound set to neighbor\n");
        }
    }

    //x+ y+ z-
    neighborIndex = CK(2,2,0);
    if(x == 2 || y == 2 || z == 0){
        if(invert == 1){
            rVal += assertNotEquals(chunk->d[CENTER_LOC][IX( DIM-1, DIM-1, 0 )],0,"chunk should NOT have (x=2 y=2 z=0) bound set to 0 -- %d %d \n");
        } else {
            rVal += assertEquals(chunk->d[CENTER_LOC][IX( DIM-1, DIM-1, 0 )],0,"chunk should have (x=2 y=2 z=0) bound set to 0-- %d %d \n");
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x<2 y<2 z>0\n");
        }
        int pass = 0;
        if(invert == 1){
            pass = chunk->d[CENTER_LOC][IX( DIM-1, DIM-1, 0 )] != chunk->d[neighborIndex][IX(    1,    1, DIM-2)];
        } else {
            pass = chunk->d[CENTER_LOC][IX( DIM-1, DIM-1, 0 )] == chunk->d[neighborIndex][IX(    1,    1, DIM-2)];
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=2 y=2 z=0) bound set to neighbor\n");
        }
    }




    //x+ y- z+
    neighborIndex = CK(2,0,2);
    if(x == 2 || y == 0 || z == 2){
        if(invert == 1){
            rVal += assertNotEquals(chunk->d[CENTER_LOC][IX( DIM-1, 0, DIM-1 )],0,"chunk should NOT have (x=2 y=0 z=2) bound set to 0 -- %d %d \n");
        } else {
            rVal += assertEquals(chunk->d[CENTER_LOC][IX( DIM-1, 0, DIM-1 )],0,"chunk should have (x=2 y=0 z=2) bound set to 0-- %d %d \n");
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x<2 y>0 z<2\n");
        }
        int pass = 0;
        if(invert == 1){
            pass = chunk->d[CENTER_LOC][IX( DIM-1, 0, DIM-1 )] != chunk->d[neighborIndex][IX(    1,    1,    1)];
        } else {
            pass = chunk->d[CENTER_LOC][IX( DIM-1, 0, DIM-1 )] == chunk->d[neighborIndex][IX(    1,    1,    1)];
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=2 y=0 z=2) bound set to neighbor\n");
        }
    }

    //x+ y- z-
    neighborIndex = CK(2,0,0);
    if(x == 2 || y == 0 || z == 0){
        if(invert == 1){
            rVal += assertNotEquals(chunk->d[CENTER_LOC][IX( DIM-1, 0, 0 )],0,"chunk should NOT have (x=2 y=0 z=0) bound set to 0 -- %d %d \n");
        } else {
            rVal += assertEquals(chunk->d[CENTER_LOC][IX( DIM-1, 0, 0 )],0,"chunk should have (x=2 y=0 z=0) bound set to 0-- %d %d \n");
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x<2 y>0 z>0\n");
        }
        int pass = 0;
        if(invert == 1){
            pass = chunk->d[CENTER_LOC][IX( DIM-1, 0, 0 )] != chunk->d[neighborIndex][IX(    1, DIM-2, DIM-2)];
        } else {
            pass = chunk->d[CENTER_LOC][IX( DIM-1, 0, 0 )] == chunk->d[neighborIndex][IX(    1, DIM-2, DIM-2)];
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=2 y=0 z=0) bound set to neighbor\n");
        }
    }














    //x- y+ z+
    neighborIndex = CK(0,2,2);
    if(x == 0 || y == 2 || z == 2){
        if(invert == 1){
            rVal += assertNotEquals(chunk->d[CENTER_LOC][IX( 0, DIM-1, DIM-1 )],0,"chunk should NOT have (x=0 y=2 z=2) bound set to 0 -- %d %d \n");
        } else {
            rVal += assertEquals(chunk->d[CENTER_LOC][IX( 0, DIM-1, DIM-1 )],0,"chunk should have (x=0 y=2 z=2) bound set to 0-- %d %d \n");
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x>0 y<2 z<2\n");
        }
        int pass = 0;
        if(invert == 1){
            pass = chunk->d[CENTER_LOC][IX( 0, DIM-1, DIM-1 )] != chunk->d[neighborIndex][IX( DIM-2,    1,    1)];
        } else {
            pass = chunk->d[CENTER_LOC][IX( 0, DIM-1, DIM-1 )] == chunk->d[neighborIndex][IX( DIM-2,    1,    1)];
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=0 y=2 z=2) bound set to neighbor\n");
        }
    }

    //x- y+ z-
    neighborIndex = CK(0,2,0);
    if(x == 0 || y == 2 || z == 0){
        if(invert == 1){
            rVal += assertNotEquals(chunk->d[CENTER_LOC][IX( 0, DIM-1, 0 )],0,"chunk should NOT have (x=0 y=2 z=0) bound set to 0 -- %d %d \n");
        } else {
            rVal += assertEquals(chunk->d[CENTER_LOC][IX( 0, DIM-1, 0 )],0,"chunk should have (x=0 y=2 z=0) bound set to 0-- %d %d \n");
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x>0 y<2 z>0\n");
        }
        int pass = 0;
        if(invert == 1){
            pass = chunk->d[CENTER_LOC][IX( 0, DIM-1, 0 )] != chunk->d[neighborIndex][IX( DIM-2,    1, DIM-2)];
        } else {
            pass = chunk->d[CENTER_LOC][IX( 0, DIM-1, 0 )] == chunk->d[neighborIndex][IX( DIM-2,    1, DIM-2)];
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=0 y=2 z=0) bound set to neighbor\n");
        }
    }




    //x- y- z+
    neighborIndex = CK(0,0,2);
    if(x == 0 || y == 0 || z == 2){
        if(invert == 1){
            rVal += assertNotEquals(chunk->d[CENTER_LOC][IX( 0, 0, DIM-1 )],0,"chunk should NOT have (x=0 y=0 z=2) bound set to 0 -- %d %d \n");
        } else {
            rVal += assertEquals(chunk->d[CENTER_LOC][IX( 0, 0, DIM-1 )],0,"chunk should have (x=0 y=0 z=2) bound set to 0-- %d %d \n");
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x>0 y>0 z<2\n");
        }
        int pass = 0;
        if(invert == 1){
            pass = chunk->d[CENTER_LOC][IX( 0, 0, DIM-1 )] != chunk->d[neighborIndex][IX( DIM-2,    1,    1)];
        } else {
            pass = chunk->d[CENTER_LOC][IX( 0, 0, DIM-1 )] == chunk->d[neighborIndex][IX( DIM-2,    1,    1)];
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=0 y=0 z=2) bound set to neighbor\n");
        }
    }

    //x- y- z-
    neighborIndex = CK(0,0,0);
    if(x == 0 || y == 0 || z == 0){
        if(invert == 1){
            rVal += assertNotEquals(chunk->d[CENTER_LOC][IX( 0, 0, 0 )],0,"chunk should NOT have (x=0 y=0 z=0) bound set to 0 -- %d %d \n");
        } else {
            rVal += assertEquals(chunk->d[CENTER_LOC][IX( 0, 0, 0 )],0,"chunk should have (x=0 y=0 z=0) bound set to 0-- %d %d \n");
        }
    } else {
        if(chunk->d[neighborIndex] == NULL){
            rVal += assertEquals(0,1,"Failed to assign neighbors properly x>0 y>0 z>0\n");
        }
        int pass = 0;
        if(invert == 1){
            pass = chunk->d[CENTER_LOC][IX( 0, 0, 0 )] != chunk->d[neighborIndex][IX( DIM-2, DIM-2, DIM-2)];
        } else {
            pass = chunk->d[CENTER_LOC][IX( 0, 0, 0 )] == chunk->d[neighborIndex][IX( DIM-2, DIM-2, DIM-2)];
        }
        if(!pass){
            rVal += assertEquals(0,1,"chunk should have (x=0 y=0 z=0) bound set to neighbor\n");
        }
    }


    
    return rVal;
}


int kernelx[27] = {
    0, 0, 0, 0, 0, 0, 0, 0, 0,
    1, 1, 1, 1, 1, 1, 1, 1, 1,
    2, 2, 2, 2, 2, 2, 2, 2, 2,
};

int kernely[27] = {
    0, 0, 0, 1, 1, 1, 2, 2, 2,
    0, 0, 0, 1, 1, 1, 2, 2, 2,
    0, 0, 0, 1, 1, 1, 2, 2, 2,
};

int kernelz[27] = {
    0, 1, 2, 0, 1, 2, 0, 1, 2,
    0, 1, 2, 0, 1, 2, 0, 1, 2,
    0, 1, 2, 0, 1, 2, 0, 1, 2,
};

int fluid_queue_boundsolver_tests(){
    int rVal = 0;

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(kernelx[i],kernely[i],kernelz[i]));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],i+1);
    }

    //make sure the data is setup correctly before the call
    for(int i = 0; i < chunkCount; i++){
        rVal += checkBounds(queue[i],kernelx[i],kernely[i],kernelz[i],1);
    }

    //call bounds setter
    fluid_solve_bounds(chunkCount,queue,env);

    //check that the 0 values are 0'd
    for(int i = 0; i < chunkCount; i++){
        rVal += checkBounds(queue[i],kernelx[i],kernely[i],kernelz[i],0);
    }

    {
        int borderVal = queue[0]->d[CENTER_LOC][IX(1,1,DIM-2)];
        int containedVal = queue[1]->d[CENTER_LOC][IX(1,1,0)];
        rVal += assertEquals(borderVal,containedVal,"chunk 0,0,1 should contain border values from 0,0,0 --- %d %d\n");
    }

    {
        int borderVal = queue[0]->d[CENTER_LOC][IX(1,1,DIM-1)];
        int containedVal = queue[1]->d[CENTER_LOC][IX(1,1,1)];
        rVal += assertEquals(borderVal,containedVal,"chunk 0,0,0 should contain border values from 0,0,1 --- %d %d\n");
    }

    {
        int borderVal = queue[0]->d[CENTER_LOC][IX(DIM-2,1,1)];
        int containedVal = queue[9]->d[CENTER_LOC][IX(0,1,1)];
        rVal += assertEquals(borderVal,containedVal,"chunk 1,0,0 should contain border values from 0,0,0 --- %d %d\n");
    }

    {
        int borderVal = queue[0]->d[CENTER_LOC][IX(DIM-1,1,1)];
        int containedVal = queue[9]->d[CENTER_LOC][IX(1,1,1)];
        rVal += assertEquals(borderVal,containedVal,"chunk 0,0,0 should contain border values from 1,0,0 --- %d %d\n");
    }



    //cleanup test
    chunk_free_queue(queue);

    return rVal;
}