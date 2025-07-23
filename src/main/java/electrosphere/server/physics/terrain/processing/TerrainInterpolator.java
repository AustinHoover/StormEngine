package electrosphere.server.physics.terrain.processing;

public class TerrainInterpolator {
    
    
    
    /*
    !!!ASSUMPTIONS!!!
    square matricies of data
    object has already been initialized
    dynamicInterpolationRatio is set to some valid power of 2
    */
    
//    /**
//     * Dynamically interpolates a chunk of a specific size from the pre-existing elevation map
//     * Includes the boundary of the chunk as well
//     * @param x The x position on the elevation map to get a chunk from
//     * @param y The y position on the elevation map to get a chunk from
//     * @return Dynamically interpolated float array of elevations of chunk
//     */
//    public static float[][] getAugmentedElevationForChunk(float[][] macroValues, long[][] randomizerValues, int dynamicInterpolationRatio, float randomDampener){
//        
//        //this is what we intend to return from the function
//        float[][] rVal = new float[dynamicInterpolationRatio+1][dynamicInterpolationRatio+1];
//        
//        /*
//        So we're looking at chunk x,y
//        
//        if this is our grid:
//        
//            4  0.1    0.2     0.3     0.4
//        ^
//        |   3  0.1    0.2     0.3     0.4
//        |
//        |   2  0.1    0.2     0.3     0.4
//        
//        x   1  0.1    0.2     0.3     0.4
//        
//              0      1       2       3
//        
//              y  ---- >
//        
//        say we're looking at x=2,y=1
//        
//        "macroValues" should contain the values for x = [1,3] and y = [0,2]
//        
//        the goal is to have the "center" of the output chunk have the value the
//        elevation grid at x=2,y=1
//        
//        */
//        
//        
//        
//        /*
//        
//        Our "macro values" at the edges of this chunk need to be haldway between this chunk's center height value and the next
//        Why? think about it for a second
//        
//        
//        If we have some grid with chunks a and b
//        
//        0.1  0.2  0.3  0.8
//        0.4    a    b  0.9
//        0.5  0.6  0.7  1.0
//        
//        
//        to generate a chunk at point a that smoothly merges into chunk b, you need
//        the chunk at a to know the value of b
//        
//        but you don't want the right edge of the chunk at a to be the VALUE of b
//        because then it would work
//        
//        so if we have two chunks
//        
//             _____________________  _____________________
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |__________a__________**__________b__________|
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |__________|__________||__________|__________|
//        
//        you want the value at ** to be half way between a and b
//        
//        the corner cases are even worse, you have to average all four
//             _____________________  _____________________
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |__________a__________||__________b__________|
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |__________|__________**__________|__________|
//             _____________________**_____________________
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |__________c__________||__________d__________|
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |          |          ||          |          |
//            |__________|__________||__________|__________|
//        
//        
//        
//        */
//        
//        float[][] newMacroValues = new float[3][3];
//        newMacroValues[0][0] = (macroValues[0][0] + macroValues[0][1] + macroValues[1][0] + macroValues[1][1]) / 4.0f;
//        newMacroValues[2][0] = (macroValues[2][0] + macroValues[2][1] + macroValues[1][0] + macroValues[1][1]) / 4.0f;
//        newMacroValues[0][2] = (macroValues[0][2] + macroValues[0][1] + macroValues[1][2] + macroValues[1][1]) / 4.0f;
//        newMacroValues[2][2] = (macroValues[2][2] + macroValues[2][1] + macroValues[1][2] + macroValues[1][1]) / 4.0f;
//        newMacroValues[1][0] = (macroValues[1][0] + macroValues[1][1]) / 2.0f;
//        newMacroValues[0][1] = (macroValues[0][1] + macroValues[1][1]) / 2.0f;
//        newMacroValues[1][2] = (macroValues[1][2] + macroValues[1][1]) / 2.0f;
//        newMacroValues[2][1] = (macroValues[2][1] + macroValues[1][1]) / 2.0f;
//        newMacroValues[1][1] = macroValues[1][1];
//        
//        macroValues = newMacroValues;
//        
//        
//        
//        
//        
//        
////        System.out.println("discreteArrayDimension: " + discreteArrayDimension);
//
//
//
//        //The following values + random seed are what are primarily used for calculating elevation map
//        //If you want to transmit the output of this model without transmitting the entire field
//        //transmit these numbers + seeds
//        
////        System.out.println("Macro values for " + x + "," + y + "\n");
////        System.out.println(macroValues[0][0] + " " + macroValues[1][0] + " " + macroValues[2][0]);
////        System.out.println(macroValues[0][1] + " " + macroValues[1][1] + " " + macroValues[2][1]);
////        System.out.println(macroValues[0][2] + " " + macroValues[1][2] + " " + macroValues[2][2]);
////        System.out.println();
//        
//        
//        int halfLength = dynamicInterpolationRatio/2;
//        
//        /*
//        
//        Four quadrants we're generating
//        
//             _____________________
//            |1         |2         |
//            |          |          |
//            |          |          |
//            |          |          |
//            |__________|__________|
//            |3         |4         |
//            |          |          |
//            |          |          |
//            |__________|__________|
//            
//        First set of loops is quadrant 1
//        then quadrant 2
//        then quadrant 3
//        then quadrant 4
//        
//        
//           0,0        1,0        2,0
//             _____________________
//            |1         |2         |
//            |          |          |
//            |          |          |
//            |          |          |
//        0,1 |__________|__________| 2,1
//            |3         |4         |
//            |          |          |
//            |          |          |
//            |__________|__________|
//           0,2        1,2          2,2
//        
//        Where the "macro values" correspond to on our array
//        
//        */
//        
//        int outXOffset = 0;
//        int outYOffset = 0;
//        
//        long quadrantRandom = 
//                randomizerValues[0][0] + 
//                randomizerValues[1][0] + 
//                randomizerValues[0][1] + 
//                randomizerValues[1][1];
//        
//        Random quadRand = new Random(quadrantRandom);
//        
//        for(int i = 0; i < halfLength; i++){
//            for(int j = 0; j < halfLength; j++){
//                rVal[i+outXOffset][j+outYOffset] = 
//                        (1.0f * (halfLength - i) * (halfLength - j))/(halfLength * halfLength) * macroValues[0][0] +
//                        (1.0f * (i          - 0) * (halfLength - j))/(halfLength * halfLength) * macroValues[1][0] +
//                        (1.0f * (halfLength - i) * (j          - 0))/(halfLength * halfLength) * macroValues[0][1] +
//                        (1.0f * (i          - 0) * (j          - 0))/(halfLength * halfLength) * macroValues[1][1] + 
//                        quadRand.nextFloat() * randomDampener
//                        ;
//            }
//        }
//        
//        
//        quadrantRandom = 
//                randomizerValues[1][0] + 
//                randomizerValues[2][0] + 
//                randomizerValues[1][1] + 
//                randomizerValues[2][1];
//        
//        quadRand = new Random(quadrantRandom);
//        
//        outXOffset = halfLength;
//        for(int i = 0; i < halfLength; i++){
//            for(int j = 0; j < halfLength; j++){
//                rVal[i+outXOffset][j+outYOffset] = 
//                        (1.0f * (halfLength - i) * (halfLength - j))/(halfLength * halfLength) * macroValues[1][0] +
//                        (1.0f * (i          - 0) * (halfLength - j))/(halfLength * halfLength) * macroValues[2][0] +
//                        (1.0f * (halfLength - i) * (j          - 0))/(halfLength * halfLength) * macroValues[1][1] +
//                        (1.0f * (i          - 0) * (j          - 0))/(halfLength * halfLength) * macroValues[2][1] +
//                        quadRand.nextFloat() * randomDampener
//                        ;
//            }
//        }
//        
//        
//        
//        quadrantRandom = 
//                randomizerValues[0][1] + 
//                randomizerValues[1][1] + 
//                randomizerValues[0][2] + 
//                randomizerValues[1][2];
//        
//        quadRand = new Random(quadrantRandom);
//        
//        outXOffset = 0;
//        outYOffset = halfLength;
//        for(int i = 0; i < halfLength; i++){
//            for(int j = 0; j < halfLength; j++){
//                rVal[i+outXOffset][j+outYOffset] = 
//                        (1.0f * (halfLength - i) * (halfLength - j))/(halfLength * halfLength) * macroValues[0][1] +
//                        (1.0f * (i          - 0) * (halfLength - j))/(halfLength * halfLength) * macroValues[1][1] +
//                        (1.0f * (halfLength - i) * (j          - 0))/(halfLength * halfLength) * macroValues[0][2] +
//                        (1.0f * (i          - 0) * (j          - 0))/(halfLength * halfLength) * macroValues[1][2] +
//                        quadRand.nextFloat() * randomDampener
//                        ;
//            }
//        }
//        
//        
//        quadrantRandom = 
//                randomizerValues[1][1] + 
//                randomizerValues[2][1] + 
//                randomizerValues[1][2] + 
//                randomizerValues[2][2];
//        
//        quadRand = new Random(quadrantRandom);
//        
//        outXOffset = halfLength;
//        for(int i = 0; i < halfLength; i++){
//            for(int j = 0; j < halfLength; j++){
//                rVal[i+outXOffset][j+outYOffset] = 
//                        (1.0f * (halfLength - i) * (halfLength - j))/(halfLength * halfLength) * macroValues[1][1] +
//                        (1.0f * (i          - 0) * (halfLength - j))/(halfLength * halfLength) * macroValues[2][1] +
//                        (1.0f * (halfLength - i) * (j          - 0))/(halfLength * halfLength) * macroValues[1][2] +
//                        (1.0f * (i          - 0) * (j          - 0))/(halfLength * halfLength) * macroValues[2][2] +
//                        quadRand.nextFloat() * randomDampener
//                        ;
//            }
//        }
//        
//        
//        /*
//        
//        Four quadrants we're generating
//        
//             ------------>
//              _____________________
//         |   |1         |2         |
//         |   |          |          | |
//         |   |          |          | |
//         |   |          |          | |
//         |   |__________|__________| |
//         |   |3         |4         | |
//         |   |          |          | |
//         V   |          |          | V
//             |__________|__________|
//            
//                 ---------->
//        
//        filling in along these arrows now
//        
//        
//        
//        this is the "order" of the edges:
//        
//        
//                   1         2
//        
//                3               5
//        
//        
//                4               6
//        
//                   7         8
//        
//        
//        the corresponding randoms for each edge are
//        
//                1
//            2       3
//                4
//        
//        */
//        
//        Random edge1Rand = new Random(randomizerValues[1][1] + randomizerValues[1][0]);
//        Random edge2Rand = new Random(randomizerValues[1][1] + randomizerValues[0][1]);
//        Random edge3Rand = new Random(randomizerValues[1][1] + randomizerValues[2][1]);
//        Random edge4Rand = new Random(randomizerValues[1][1] + randomizerValues[1][2]);
//        
//        
//        
//        for(int i = 0; i < halfLength; i++){
//            //edge 1
//            rVal[i][0] = 
//                    (1.0f * (halfLength - i) / halfLength) * macroValues[0][0] + 
//                    (1.0f * (i          - 0) / halfLength) * macroValues[1][0] +
//                    edge1Rand.nextFloat() * randomDampener
//                    ;
//            
//            
//            //edge 2
//            rVal[i+halfLength][0] = 
//                    (1.0f * (halfLength - i) / halfLength) * macroValues[1][0] + 
//                    (1.0f * (i          - 0) / halfLength) * macroValues[2][0] +
//                    edge1Rand.nextFloat() * randomDampener
//                    ;
//            
//            
//            //edge 3
//            rVal[0][i] = 
//                    (1.0f * (halfLength - i) / halfLength) * macroValues[0][0] + 
//                    (1.0f * (i          - 0) / halfLength) * macroValues[0][1] +
//                    edge2Rand.nextFloat() * randomDampener
//                    ;
//            
//            
//            //edge 4
//            rVal[0][i+halfLength] = 
//                    (1.0f * (halfLength - i) / halfLength) * macroValues[0][1] + 
//                    (1.0f * (i          - 0) / halfLength) * macroValues[0][2] +
//                    edge2Rand.nextFloat() * randomDampener
//                    ;
//            
//            //edge 5
//            rVal[dynamicInterpolationRatio][i] = 
//                    (1.0f * (halfLength - i) / halfLength) * macroValues[2][0] + 
//                    (1.0f * (i          - 0) / halfLength) * macroValues[2][1] +
//                    edge3Rand.nextFloat() * randomDampener
//                    ;
//            
//            
//            //edge 6
//            rVal[dynamicInterpolationRatio][i+halfLength] = 
//                    (1.0f * (halfLength - i) / halfLength) * macroValues[2][1] + 
//                    (1.0f * (i          - 0) / halfLength) * macroValues[2][2] +
//                    edge3Rand.nextFloat() * randomDampener
//                    ;
//            
//            
//            //edge 7
//            rVal[i][dynamicInterpolationRatio] = 
//                    (1.0f * (halfLength - i) / halfLength) * macroValues[0][2] + 
//                    (1.0f * (i          - 0) / halfLength) * macroValues[1][2] +
//                    edge4Rand.nextFloat() * randomDampener
//                    ;
//            
//            
//            //edge 8
//            rVal[i+halfLength][dynamicInterpolationRatio] = 
//                    (1.0f * (halfLength - i) / halfLength) * macroValues[1][2] + 
//                    (1.0f * (i          - 0) / halfLength) * macroValues[2][2] +
//                    edge4Rand.nextFloat() * randomDampener
//                    ;
//        }
//        
//        //"Because it is passed over twice" this doesn't get the right value after the loops
//        //so we do it manually to correct
////        rVal[0][dynamicInterpolationRatio] = 
////                macroValues[0][2] + 
////                new Random(randomizerValues[0][2] + randomizerValues[1][2]).nextFloat()
////                ;
//        
//        
//        //"Because array dimensions" this doesn't end up getting filled in
//        //so we have to do it manually
//        //set final corner at bottom right
//        rVal[dynamicInterpolationRatio][dynamicInterpolationRatio] = 
//                macroValues[2][2] +
//                new Random(randomizerValues[1][2] + randomizerValues[2][2]).nextFloat() * randomDampener
//                ;
//        
//        
//        return rVal;
//    }
//    
//    /*
//        So we're looking at chunk x,y
//        
//        if this is our grid:
//        
//            4  0.1    0.2     0.3     0.4
//        ^
//        |   3  0.1    0.2     0.3     0.4
//        |
//        |   2  0.1    0.2     0.3     0.4
//        
//        x   1  0.1    0.2     0.3     0.4
//        
//              0      1       2       3
//        
//              y  ---- >
//        
//        say we're looking at x=2,y=1
//        
//        "macroValues" should contain the values for x = [1,3] and y = [0,2]
//        
//        the goal is to have the "center" of the output chunk have the value the
//        elevation grid at x=2,y=1
//        
//        */
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
//    public static float[][] getBicubicInterpolatedChunk(float[][] macroValues, long[][] randomizerValues, int dynamicInterpolationRatio, float randomDampener){
//        
//        //this is what we intend to return from the function
//        float[][] rVal = new float[dynamicInterpolationRatio+1][dynamicInterpolationRatio+1];
//        
//        float[][] firstPhase = new float[5][dynamicInterpolationRatio+1];
//        
//        
//        int halfLength = dynamicInterpolationRatio/2;
//        
//        
//        /*
//        
//        We start with a 5x5 array:
//        
//        * -- * -- * -- * -- *
//        |    |    |    |    |
//        * -- * -- * -- * -- *
//        |    |    |    |    |
//        * -- * -- * -- * -- *
//        |    |    |    |    |
//        * -- * -- * -- * -- *
//        |    |    |    |    |
//        * -- * -- * -- * -- *
//        
//        we cubic interpolate along the y axis first, ie along these points:
//        
//        
//        
//        * -- * -- * -- * -- *     |
//        |    |    |    |    |     |
//        O -- O -- O -- O -- O     |
//        O    O    O    O    O     |
//        O -- O -- O -- O -- O     |
//        O    O    O    O    O     |
//        O -- O -- O -- O -- O     V
//        |    |    |    |    |
//        * -- * -- * -- * -- *
//        
//        
//        then we use the values along that OOOOO line to create the actual heightfield
//        
//        * -- * -- * -- * -- *
//        |    |    |    |    |
//        O -- O XX O XX O -- O
//        O    O XX O XX O -- O
//        O -- O XX O XX O -- O
//        O    O XX O XX O -- O
//        O -- O XX O XX O -- O
//        |    |    |    |    |
//        * -- * -- * -- * -- *
//        
//        This plane of Xs is what we're after
//        
//        We'll label these processes "phase 1" and "phase 2"
//        
//        phase1 = getting the Os
//        phase2 = getting the Xs
//        
//        
//        */
//        
//        
//        /*
//        
//        PHASE 1
//        
//        we're going to prioritize going down, then left, so
//        
//        |  |  |
//        V /V /V
//        |/ |/ |
//        V  V  V   etc..
//        
//        
//        lets label each substep
//        
//        0   2   4   6   8
//        
//        1   3   5   7   9
//        
//        */
//        
//        //
//        //part 0
//        //
//        
//        long phase1Randomizer = 
//                randomizerValues[0][0] + 
//                randomizerValues[0][1] + 
//                randomizerValues[0][2] + 
//                randomizerValues[0][3];
//        
//        Random randomizer = new Random(phase1Randomizer);
//        
//        float a0 = macroValues[0][3] - macroValues[0][2] - macroValues[0][0] + macroValues[0][1];
//        float a1 = macroValues[0][0] - macroValues[0][1] - a0;
//        float a2 = macroValues[0][2] - macroValues[0][0];
//        float a3 = macroValues[0][1];
//        
//        for(int i = 0; i < halfLength; i++){
//            float x = (float)i/(float)halfLength;
//            float x2 = x * x;
//            firstPhase[0][i] = a0 * x * x2 + a1 * x2 + a2 * x + a3 + randomizer.nextFloat();
//        }
//        
//        //inbetween
//        firstPhase[0][halfLength] = macroValues[0][2];
//        
//        
//        //
//        //part 1
//        //
//        
//        phase1Randomizer = 
//                randomizerValues[0][1] + 
//                randomizerValues[0][2] + 
//                randomizerValues[0][3] + 
//                randomizerValues[0][4];
//        
//        randomizer = new Random(phase1Randomizer);
//        
//        a0 = macroValues[0][4] - macroValues[0][3] - macroValues[0][1] + macroValues[0][2];
//        a1 = macroValues[0][1] - macroValues[0][2] - a0;
//        a2 = macroValues[0][3] - macroValues[0][1];
//        a3 = macroValues[0][2];
//        
//        for(int i = 0; i < halfLength; i++){
//            float x = (float)i/(float)halfLength;
//            float x2 = x * x;
//            firstPhase[0][i + halfLength + 1] = a0 * x * x2 + a1 * x2 + a2 * x + a3 + randomizer.nextFloat() * randomDampener;
//        }
//        
//        
//        
//        
//        
//        
//        //
//        //part 2
//        //
//        
//        phase1Randomizer = 
//                randomizerValues[1][0] + 
//                randomizerValues[1][1] + 
//                randomizerValues[1][2] + 
//                randomizerValues[1][3];
//        
//        randomizer = new Random(phase1Randomizer);
//        
//        a0 = macroValues[1][3] - macroValues[1][2] - macroValues[1][0] + macroValues[1][1];
//        a1 = macroValues[1][0] - macroValues[1][1] - a0;
//        a2 = macroValues[1][2] - macroValues[1][0];
//        a3 = macroValues[1][1];
//        
//        for(int i = 0; i < halfLength; i++){
//            float x = (float)i/(float)halfLength;
//            float x2 = x * x;
//            firstPhase[1][i] = a0 * x * x2 + a1 * x2 + a2 * x + a3 + randomizer.nextFloat() * randomDampener;
//        }
//        
//        
//        //inbetween
//        firstPhase[1][halfLength] = macroValues[1][2];
//        
//        
//        //
//        //part 3
//        //
//        
//        phase1Randomizer = 
//                randomizerValues[1][1] + 
//                randomizerValues[1][2] + 
//                randomizerValues[1][3] + 
//                randomizerValues[1][4];
//        
//        randomizer = new Random(phase1Randomizer);
//        
//        a0 = macroValues[1][4] - macroValues[1][3] - macroValues[1][1] + macroValues[1][2];
//        a1 = macroValues[1][1] - macroValues[1][2] - a0;
//        a2 = macroValues[1][3] - macroValues[1][1];
//        a3 = macroValues[1][2];
//        
//        for(int i = 0; i < halfLength; i++){
//            float x = (float)i/(float)halfLength;
//            float x2 = x * x;
//            firstPhase[1][i + halfLength + 1] = a0 * x * x2 + a1 * x2 + a2 * x + a3 + randomizer.nextFloat() * randomDampener;
//        }
//        
//        
//        
//        //
//        //part 4
//        //
//        
//        phase1Randomizer = 
//                randomizerValues[2][0] + 
//                randomizerValues[2][1] + 
//                randomizerValues[2][2] + 
//                randomizerValues[2][3];
//        
//        randomizer = new Random(phase1Randomizer);
//        
//        a0 = macroValues[2][3] - macroValues[2][2] - macroValues[2][0] + macroValues[2][1];
//        a1 = macroValues[2][0] - macroValues[2][1] - a0;
//        a2 = macroValues[2][2] - macroValues[2][0];
//        a3 = macroValues[2][1];
//        
//        for(int i = 0; i < halfLength; i++){
//            float x = (float)i/(float)halfLength;
//            float x2 = x * x;
//            firstPhase[2][i] = a0 * x * x2 + a1 * x2 + a2 * x + a3 + randomizer.nextFloat() * randomDampener;
//        }
//        
//        
//        //inbetween
//        firstPhase[2][halfLength] = macroValues[2][2];
//        
//        
//        //
//        //part 5
//        //
//        
//        phase1Randomizer = 
//                randomizerValues[2][1] + 
//                randomizerValues[2][2] + 
//                randomizerValues[2][3] + 
//                randomizerValues[2][4];
//        
//        randomizer = new Random(phase1Randomizer);
//        
//        a0 = macroValues[2][4] - macroValues[2][3] - macroValues[2][1] + macroValues[2][2];
//        a1 = macroValues[2][1] - macroValues[2][2] - a0;
//        a2 = macroValues[2][3] - macroValues[2][1];
//        a3 = macroValues[2][2];
//        
//        for(int i = 0; i < halfLength; i++){
//            float x = (float)i/(float)halfLength;
//            float x2 = x * x;
//            firstPhase[2][i + halfLength + 1] = a0 * x * x2 + a1 * x2 + a2 * x + a3 + randomizer.nextFloat() * randomDampener;
//        }
//        
//        
//        
//        
//        
//        //
//        //part 6
//        //
//        
//        phase1Randomizer = 
//                randomizerValues[3][0] + 
//                randomizerValues[3][1] + 
//                randomizerValues[3][2] + 
//                randomizerValues[3][3];
//        
//        randomizer = new Random(phase1Randomizer);
//        
//        a0 = macroValues[3][3] - macroValues[3][2] - macroValues[3][0] + macroValues[3][1];
//        a1 = macroValues[3][0] - macroValues[3][1] - a0;
//        a2 = macroValues[3][2] - macroValues[3][0];
//        a3 = macroValues[3][1];
//        
//        for(int i = 0; i < halfLength; i++){
//            float x = (float)i/(float)halfLength;
//            float x2 = x * x;
//            firstPhase[3][i] = a0 * x * x2 + a1 * x2 + a2 * x + a3 + randomizer.nextFloat() * randomDampener;
//        }
//        
//        
//        //inbetween
//        firstPhase[3][halfLength] = macroValues[3][2];
//        
//        
//        //
//        //part 7
//        //
//        
//        phase1Randomizer = 
//                randomizerValues[3][1] + 
//                randomizerValues[3][2] + 
//                randomizerValues[3][3] + 
//                randomizerValues[3][4];
//        
//        randomizer = new Random(phase1Randomizer);
//        
//        a0 = macroValues[3][4] - macroValues[3][3] - macroValues[3][1] + macroValues[3][2];
//        a1 = macroValues[3][1] - macroValues[3][2] - a0;
//        a2 = macroValues[3][3] - macroValues[3][1];
//        a3 = macroValues[3][2];
//        
//        for(int i = 0; i < halfLength; i++){
//            float x = (float)i/(float)halfLength;
//            float x2 = x * x;
//            firstPhase[3][i + halfLength + 1] = a0 * x * x2 + a1 * x2 + a2 * x + a3 + randomizer.nextFloat() * randomDampener;
//        }
//        
//        
//        
//        
//        
//        
//        //
//        //part 8
//        //
//        
//        phase1Randomizer = 
//                randomizerValues[4][0] + 
//                randomizerValues[4][1] + 
//                randomizerValues[4][2] + 
//                randomizerValues[4][3];
//        
//        randomizer = new Random(phase1Randomizer);
//        
//        a0 = macroValues[4][3] - macroValues[4][2] - macroValues[4][0] + macroValues[4][1];
//        a1 = macroValues[4][0] - macroValues[4][1] - a0;
//        a2 = macroValues[4][2] - macroValues[4][0];
//        a3 = macroValues[4][1];
//        
//        for(int i = 0; i < halfLength; i++){
//            float x = (float)i/(float)halfLength;
//            float x2 = x * x;
//            firstPhase[4][i] = a0 * x * x2 + a1 * x2 + a2 * x + a3 + randomizer.nextFloat() * randomDampener;
//        }
//        
//        
//        //inbetween
//        firstPhase[4][halfLength] = macroValues[4][2];
//        
//        
//        //
//        //part 9
//        //
//        
//        phase1Randomizer = 
//                randomizerValues[4][1] + 
//                randomizerValues[4][2] + 
//                randomizerValues[4][3] + 
//                randomizerValues[4][4];
//        
//        randomizer = new Random(phase1Randomizer);
//        
//        a0 = macroValues[4][4] - macroValues[4][3] - macroValues[4][1] + macroValues[4][2];
//        a1 = macroValues[4][1] - macroValues[4][2] - a0;
//        a2 = macroValues[4][3] - macroValues[4][1];
//        a3 = macroValues[4][2];
//        
//        for(int i = 0; i < halfLength; i++){
//            float x = (float)i/(float)halfLength;
//            float x2 = x * x;
//            firstPhase[4][i + halfLength + 1] = a0 * x * x2 + a1 * x2 + a2 * x + a3 + randomizer.nextFloat() * randomDampener;
//        }
//        
//        
//        
//        
//        //
//        //
//        //
//        //     E N D      P H A S E      1
//        //
//        //
//        //
//        
//        
//        for(int x = 0; x < dynamicInterpolationRatio+1; x++){
//            phase1Randomizer = 
//                    (long)(firstPhase[0][x] + 
//                    firstPhase[1][x] + 
//                    firstPhase[2][x] + 
//                    firstPhase[3][x]);
//        
//            randomizer = new Random(phase1Randomizer);
//
//            a0 = firstPhase[3][x] - firstPhase[2][x] - firstPhase[0][x] + firstPhase[1][x];
//            a1 = firstPhase[0][x] - firstPhase[1][x] - a0;
//            a2 = firstPhase[2][x] - firstPhase[0][x];
//            a3 = firstPhase[1][x];
//
//            for(int y = 0; y < halfLength + 1; y++){
//                float i = (float)y/(float)halfLength;
//                float i2 = i * i;
//                rVal[x][y] = a0 * i * i2 + a1 * i2 + a2 * i + a3 + randomizer.nextFloat() * randomDampener;
//            }
//
//            //
//            //part 9
//            //
//
//            phase1Randomizer = 
//                    (long)(firstPhase[1][x] + 
//                    firstPhase[2][x] + 
//                    firstPhase[3][x] + 
//                    firstPhase[4][x]);
//        
//            randomizer = new Random(phase1Randomizer);
//
//            a0 = firstPhase[4][x] - firstPhase[3][x] - firstPhase[1][x] + firstPhase[2][x];
//            a1 = firstPhase[1][x] - firstPhase[2][x] - a0;
//            a2 = firstPhase[3][x] - firstPhase[1][x];
//            a3 = firstPhase[2][x];
//
//            for(int y = 0; y < halfLength; y++){
//                float i = (float)y/(float)halfLength;
//                float i2 = i * i;
//                rVal[x][y + halfLength + 1] = a0 * i * i2 + a1 * i2 + a2 * i + a3 + randomizer.nextFloat() * randomDampener;
//            }
//        }
//        
//        
//        
//        
//        
//        
//        
//        
//        return rVal;
//        
//    }
    
    /**
     * Gets the bicubic interpolation of an array of 5 x 5 values to an array of (dynamicInterpolationRatio + 1) x (dynamicInterpolationRatio + 1)
     * @param macroValues The array of values to sample from
     * @param dynamicInterpolationRatio The interpolation ratio
     * @return The interpolated array
     */
    public static float[][] getBicubicInterpolatedChunk(float[][] macroValues, int dynamicInterpolationRatio){
        float[][] rVal = new float[dynamicInterpolationRatio + 1][dynamicInterpolationRatio + 1];
        
        float[][] subValues = new float[4][4];
        
        for(int x = 0; x < 4; x++){
            for(int y = 0; y < 4; y++){
                subValues[x][y] = (macroValues[x][y] + macroValues[x+1][y] + macroValues[x][y+1] + macroValues[x+1][y+1])/4.0f;
            }
        }
        
        float[][] inbetweenStage = new float[4][dynamicInterpolationRatio + 1];
        
        
        
        
        //
        //Inbetween phase 1
        //
        float a0 = subValues[0][3] - subValues[0][2] - subValues[0][0] + subValues[0][1];
        float a1 = subValues[0][0] - subValues[0][1] - a0;
        float a2 = subValues[0][2] - subValues[0][0];
        float a3 = subValues[0][1];
        for(int i = 0; i < dynamicInterpolationRatio + 1; i++){
            float x = (float)i/(float)dynamicInterpolationRatio;
            float x2 = x * x;
            inbetweenStage[0][i] = a0 * x * x2 + a1 * x2 + a2 * x + a3;
        }
        
        
        //
        //Inbetween phase 2
        //
        
        a0 = subValues[1][3] - subValues[1][2] - subValues[1][0] + subValues[1][1];
        a1 = subValues[1][0] - subValues[1][1] - a0;
        a2 = subValues[1][2] - subValues[1][0];
        a3 = subValues[1][1];
        for(int i = 0; i < dynamicInterpolationRatio + 1; i++){
            float x = (float)i/(float)dynamicInterpolationRatio;
            float x2 = x * x;
            inbetweenStage[1][i] = a0 * x * x2 + a1 * x2 + a2 * x + a3;
        }
        
        
        //
        //Inbetween phase 3
        //
        
        a0 = subValues[2][3] - subValues[2][2] - subValues[2][0] + subValues[2][1];
        a1 = subValues[2][0] - subValues[2][1] - a0;
        a2 = subValues[2][2] - subValues[2][0];
        a3 = subValues[2][1];
        for(int i = 0; i < dynamicInterpolationRatio + 1; i++){
            float x = (float)i/(float)dynamicInterpolationRatio;
            float x2 = x * x;
            inbetweenStage[2][i] = a0 * x * x2 + a1 * x2 + a2 * x + a3;
        }
        
        
        
        //
        //Inbetween phase 4
        //
        
        a0 = subValues[3][3] - subValues[3][2] - subValues[3][0] + subValues[3][1];
        a1 = subValues[3][0] - subValues[3][1] - a0;
        a2 = subValues[3][2] - subValues[3][0];
        a3 = subValues[3][1];
        for(int i = 0; i < dynamicInterpolationRatio + 1; i++){
            float x = (float)i/(float)dynamicInterpolationRatio;
            float x2 = x * x;
            inbetweenStage[3][i] = a0 * x * x2 + a1 * x2 + a2 * x + a3;
        }
        
        
        //
        //final phase
        //
        
        
        for(int x = 0; x < dynamicInterpolationRatio + 1; x++){
            a0 = inbetweenStage[3][x] - inbetweenStage[2][x] - inbetweenStage[0][x] + inbetweenStage[1][x];
            a1 = inbetweenStage[0][x] - inbetweenStage[1][x] - a0;
            a2 = inbetweenStage[2][x] - inbetweenStage[0][x];
            a3 = inbetweenStage[1][x];
            for(int y = 0; y < dynamicInterpolationRatio + 1; y++){
                float i = (float)y/(float)dynamicInterpolationRatio;
                float i2 = i * i;
                rVal[y][x] = a0 * i * i2 + a1 * i2 + a2 * i + a3;
            }
        }
        
        
        return rVal;
    }
    
}
