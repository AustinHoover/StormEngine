#version 450 core

// shader inputs
layout (location = 0) in vec3 position;
layout (location = 1) in vec2 rawTextureCoords;

out vec2 textureCoords;

void main(){
    textureCoords = rawTextureCoords;
	gl_Position = vec4(position, 1.0f);
}