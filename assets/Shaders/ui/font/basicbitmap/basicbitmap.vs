
#version 330 core
layout (location = 0) in vec2 aPos;
layout (location = 4) in vec2 aTexCoords;

out vec2 TexCoords;

uniform vec3 mPosition;
uniform vec3 mDimension;
uniform vec3 tPosition;
uniform vec3 tDimension;

void main(){


    vec2 finalPos = vec2(
                        aPos.x * mDimension.x + mPosition.x,
                        aPos.y * mDimension.y + mPosition.y
                        );
    gl_Position = vec4(finalPos.x, finalPos.y, 0.0, 1.0); 


    vec2 finalTex = vec2(
                        aTexCoords.x * tDimension.x + tPosition.x,
                        aTexCoords.y * tDimension.y + tPosition.y
                        );
    TexCoords = finalTex;
}