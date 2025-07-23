#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/lights.fs"
#include "../../lib/material.fs"

#define FLUID_TRANSPARENCY 0.2f


in vec3 FragPos;
in vec3 ViewFragPos;
in vec3 Normal;
in vec2 texPlane1;
in vec2 texPlane2;
in vec2 texPlane3;
in vec4 FragPosLightSpace;


uniform dvec3 viewPos;
uniform Material material;


out vec4 FragColor;

// function prototypes
vec3 getColor(vec2 texPlane1, vec2 texPlane2, vec2 texPlane3, vec3 normal, Material material);

void main(){
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(vec3(viewPos) - FragPos);
    
    // Calculate the light to apply
    vec3 light = getTotalLight(
        material,
        vec2(0),
        vec3(viewPos.xyz),
        FragPosLightSpace,
        ViewFragPos,
        FragPos,
        norm,
        viewDir
    );

    //calculate final color
    vec3 finalColor = light;

    //this final calculation is for transparency
    FragColor = vec4(finalColor, FLUID_TRANSPARENCY);
}


vec3 getColor(vec2 texPlane1, vec2 texPlane2, vec2 texPlane3, vec3 normal, Material material){

    vec3 weights = abs(normal);

    vec3 albedoX = texture(material.diffuse, texPlane1).rgb;
    vec3 albedoY = texture(material.diffuse, texPlane2).rgb;
    vec3 albedoZ = texture(material.diffuse, texPlane3).rgb;


    return (albedoX * weights.x + albedoY * weights.y + albedoZ * weights.z);
}