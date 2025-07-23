#version 430 core
layout (location = 0) in vec3 aPos;
layout (location = 4) in vec2 aTexCoords;

out vec2 TexCoords;

uniform vec3 mPosition;
uniform vec3 mDimension;
uniform vec3 tPosition;
uniform vec3 tDimension;

void main(){

     vec2 finalPos = vec2(
        ((aPos.x + 1)/2 * mDimension.x + mPosition.x) * 2 - 1,
        ((aPos.y + 1)/2 * mDimension.y + (1 - mDimension.y) - mPosition.y) * 2 - 1

        // ((((aPos.y + 1)/2) * mDimension.y + mPosition.y) * 2 - 1)
        // aPos.y * mDimension.y + (mPosition.y) + (1 - mDimension.y)
    );
    gl_Position = vec4(finalPos.x, finalPos.y, 0.0, 1.0); 

    vec2 finalTex = vec2(
        aTexCoords.x * tDimension.x + tPosition.x,
        aTexCoords.y * tDimension.y + tPosition.y
    );
    // vec2 finalTex = aTexCoords;
    // vec2 finalTex = vec2(
    //     aTexCoords.x + 0.7,
    //     aTexCoords.y
    // );
    TexCoords = finalTex + vec2(0,-1);
}
