#version 330 core
out vec4 FragColor;
  
in vec2 TexCoords;

uniform sampler2D screenTexture;

const float offset = 1.0 / 500.0;  

void main(){

    vec2 offsets[9] = vec2[](
        vec2(-offset,  offset), // top-left
        vec2( 0.0f,    offset), // top-center
        vec2( offset,  offset), // top-right
        vec2(-offset,  0.0f),   // center-left
        vec2( 0.0f,    0.0f),   // center-center
        vec2( offset,  0.0f),   // center-right
        vec2(-offset, -offset), // bottom-left
        vec2( 0.0f,   -offset), // bottom-center
        vec2( offset, -offset)  // bottom-right    
    );

    // float kernel[9] = float[](
    //     -1, -1, -1,
    //     -1,  9, -1,
    //     -1, -1, -1
    // );
    
    vec3 sT[9];
    for(int i = 0; i < 9; i++)
    {
        sT[i] = vec3(texture(screenTexture, TexCoords.st + offsets[i]));
    }
    vec3 Gx = (-sT[0] + sT[2]) + 2 * (-sT[3] + sT[5]) + (-sT[6] + sT[8]);
    vec3 Gy = (sT[0] + 2 * sT[1] + sT[2]) - (sT[6] + 2 * sT[7] + sT[8]);
    vec3 G = sqrt(Gx * Gx + Gy * Gy);
    // vec3 col = vec3(0.0);
    // for(int i = 0; i < 9; i++){
    //     col += sampleTex[i] * kernel[i];
    // }
    

    FragColor = vec4(G,1);
}