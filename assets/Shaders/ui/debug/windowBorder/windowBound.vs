#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 4) in vec2 aTexCoords;

out vec2 texCoord;

uniform vec3 mPosition;
uniform vec3 mDimension;

void main(){
    //0,0
    vec2 finalPos = vec2(
        ((((aPos.x + 1)/2) * mDimension.x + mPosition.x) * 2 - 1),
        ((((aPos.y + 1)/2) * mDimension.y + (1 - mDimension.y) - mPosition.y) * 2 - 1)
        // aPos.y * mDimension.y + (mPosition.y) + (1 - mDimension.y)
    );
    gl_Position = vec4(finalPos.x, finalPos.y, 0.0, 1.0); 
    texCoord = aTexCoords;
}
