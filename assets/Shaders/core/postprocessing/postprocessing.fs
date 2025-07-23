#version 450 core

//the texture coordinates of the screen
in vec2 textureCoords;

// shader outputs
layout (location = 0) out vec4 frag;


//
//Uniforms
//

// color accumulation buffer
layout (binding = 0) uniform sampler2D screenTexture;

//Controls whether blur is applied or not
uniform int applyBlur;



//
//Consts
//

//How far to pull pixels from
const float offset = 1.0 / 300.0;

//offsets for kernel sampling
const vec2 kernelOffsets[9] = vec2[](
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

const float blurKernel[9] = float[](
	1.0 / 16, 2.0 / 16, 1.0 / 16,
	2.0 / 16, 4.0 / 16, 2.0 / 16,
	1.0 / 16, 2.0 / 16, 1.0 / 16  
);



//
//main
//
void main(){
	vec3 outputColor = vec3(texture(screenTexture,textureCoords.st));
    
	//kernel samples
    vec3 sampleTex[9];

	//
	//Blur
	if(applyBlur > 0){
		for(int i = 0; i < 9; i++){
			sampleTex[i] = vec3(texture(screenTexture, textureCoords.st + kernelOffsets[i]));
		}
		outputColor = vec3(0.0);
		for(int i = 0; i < 9; i++){
			outputColor = outputColor + sampleTex[i] * blurKernel[i];
		}
	}
    
    frag = vec4(outputColor, 1.0);
} 