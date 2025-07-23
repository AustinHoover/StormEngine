#version 450 core
#extension GL_ARB_shading_language_include : require
#include "./lib/lights.fs"
#include "./lib/material.fs"
#include "./lib/standarduniform.fs"

//Shaders/FragmentShader.fs

in vec3 FragPos;
in vec3 ViewFragPos;
in vec3 Normal;
in vec2 TexCoord;
in vec4 FragPosLightSpace;


uniform dvec3 viewPos;
uniform Material material;

/**
The color to apply to the model
*/
uniform vec4 color;

/**
The output
*/
out vec4 FragColor;

void main(){
    if(texture(material.diffuse, TexCoord).a < FRAGMENT_ALPHA_CUTOFF){
        discard;
    }
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(vec3(viewPos) - FragPos);

    // Calculate the light to apply
    vec3 light = getTotalLight(
        material,
        TexCoord,
        vec3(viewPos.xyz),
        FragPosLightSpace,
        ViewFragPos,
        FragPos,
        norm,
        viewDir
    );

    //calculate final color
    vec3 finalColor = material.albedo * light;

    //this final calculation is for transparency
    FragColor = vec4(finalColor, texture(material.diffuse, TexCoord).a);
}
