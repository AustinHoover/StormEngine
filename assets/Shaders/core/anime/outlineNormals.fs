#version 330 core
out vec4 FragColor;
  
in vec2 TexCoords;

uniform sampler2D screenTexture;

const float offset = 1.0 / 2000.0;  

int posToIndex(int x, int y);
float calculateAveragedEdge(int x, int y, vec3 sT[25]);

void main(){
    vec2 offsets[25] = vec2[25](
        vec2(-2 * offset,  2 * offset),
        vec2(-2 * offset,  1 * offset),
        vec2(-2 * offset,  0 * offset),
        vec2(-2 * offset, -1 * offset),
        vec2(-2 * offset, -2 * offset),
        vec2(-1 * offset,  2 * offset),
        vec2(-1 * offset,  1 * offset),
        vec2(-1 * offset,  0 * offset),
        vec2(-1 * offset, -1 * offset),
        vec2(-1 * offset, -2 * offset),
        vec2( 0 * offset,  2 * offset),
        vec2( 0 * offset,  1 * offset),
        vec2( 0 * offset,  0 * offset),
        vec2( 0 * offset, -1 * offset),
        vec2( 0 * offset, -2 * offset),
        vec2( 1 * offset,  2 * offset),
        vec2( 1 * offset,  1 * offset),
        vec2( 1 * offset,  0 * offset),
        vec2( 1 * offset, -1 * offset),
        vec2( 1 * offset, -2 * offset),
        vec2( 2 * offset,  2 * offset),
        vec2( 2 * offset,  1 * offset),
        vec2( 2 * offset,  0 * offset),
        vec2( 2 * offset, -1 * offset),
        vec2( 2 * offset, -2 * offset)
    );
    // vec2 offsets[9] = vec2[](
    //     vec2(-offset,  offset), // top-left
    //     vec2( 0.0f,    offset), // top-center
    //     vec2( offset,  offset), // top-right
    //     vec2(-offset,  0.0f),   // center-left
    //     vec2( 0.0f,    0.0f),   // center-center
    //     vec2( offset,  0.0f),   // center-right
    //     vec2(-offset, -offset), // bottom-left
    //     vec2( 0.0f,   -offset), // bottom-center
    //     vec2( offset, -offset)  // bottom-right    
    // );

    // float kernel[9] = float[](
    //     -1, -1, -1,
    //     -1,  9, -1,
    //     -1, -1, -1
    // );
    
    vec3 sT[25];
    for(int x = 0; x < 5; x++){
        for(int y = 0; y < 5; y++){
            sT[x * 5 + y] = texture(screenTexture, TexCoords.st + offsets[x * 5 + y]).xyz;
        }
    }
    // vec3 Gx = (-sT[0] + sT[2]) + 2 * (-sT[3] + sT[5]) + (-sT[6] + sT[8]);
    // vec3 Gy = (sT[0] + 2 * sT[1] + sT[2]) - (sT[6] + 2 * sT[7] + sT[8]);
    // vec3 G = sqrt(Gx * Gx + Gy * Gy);
    // float averaged = (G.x + G.y + G.z)/3.0;
    float vals[9];
    for(int x = 0; x < 3; x++){
        for(int y = 0; y < 3; y++){
            vals[x * 3 + y] = calculateAveragedEdge(x,y,sT);
        }
    }

    float rVal = 0;
    float cutoff1 = 0.6;
    float cutoff2 = 0.1;
    float cutoff3 = 0.5;

    float surroundAvg = (vals[0] + vals[2] + vals[6] + vals[8])/4.0;

    if(
        //center
        vals[4] > cutoff1 && 
        surroundAvg > cutoff3
        // //plus
        // vals[1] > cutoff2 && 
        // vals[3] > cutoff2 && 
        // vals[5] > cutoff2 && 
        // vals[7] > cutoff2 //&&
        // // //diag
        // vals[0] < cutoff3 && 
        // vals[2] < cutoff3 && 
        // vals[6] < cutoff3 && 
        // vals[8] < cutoff3
    ){
        rVal = min(vals[4],1.0);
    }

    // rVal = calculateAveragedEdge(1,1,sT);
    // if(rVal < 0.8){
    //     rVal = 0;
    // }
    // vec3 col = vec3(0.0);
    // for(int i = 0; i < 9; i++){
    //     col += sampleTex[i] * kernel[i];
    // }
    

    FragColor = vec4(rVal,rVal,rVal,1);
}

float calculateAveragedEdge(int x, int y, vec3 sT[25]){
    //compute sobel kernel
    vec3 Gx = 
    (-sT[posToIndex(x,y) + 0] + sT[posToIndex(x,y) + 2]) + 
    2 * (-sT[posToIndex(x,y) + 5] + sT[posToIndex(x,y) + 7]) + 
    (-sT[posToIndex(x,y) + 10] + sT[posToIndex(x,y)+ 12]);
    vec3 Gy = 
    (sT[posToIndex(x,y) + 0] + 2 * sT[posToIndex(x,y) + 1] + sT[posToIndex(x,y) + 2]) - 
    (sT[posToIndex(x,y) + 10] + 2 * sT[posToIndex(x,y)+ 11] + sT[posToIndex(x,y) + 12]);
    vec3 G = sqrt(Gx * Gx + Gy * Gy);

    //compute laplacian kernel
    vec3 L = sT[posToIndex(x,y) + 1] + sT[posToIndex(x,y) + 5] - 4 * sT[posToIndex(x,y) + 6] + sT[posToIndex(x,y) + 7] + sT[posToIndex(x,y) + 11];

    float averaged = abs(G.x + G.y + G.z)/3.0;
    return averaged;
}

int posToIndex(int x, int y){
    return x * 5 + y;
}
