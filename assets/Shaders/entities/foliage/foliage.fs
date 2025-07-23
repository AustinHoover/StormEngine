#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/lights.fs"
#include "../../lib/material.fs"
#include "../../lib/standarduniform.fs"

//foliage.fs


/**
  * Transparency of the foliage
  */
#define FOLIAGE_TRANSPARENCY 1.0


in vec3 FragPos;
in vec3 ViewFragPos;
in vec3 Normal;
in vec2 TexCoord;
in vec4 FragPosLightSpace;
in vec3 normalRot1;
in vec3 normalRot2;


uniform dvec3 viewPos;
uniform Material material;

//texture stuff
uniform vec3 baseColor;
uniform vec3 tipColor;


/**
The output
*/
out vec4 FragColor;

// function prototypes
float easeIn(float interpolator);
float easeOut(float interpolator);

void main(){

    //basic vars
    float heightPercent = TexCoord.y;

    //calculate color
    vec3 textureColor = mix(baseColor,tipColor,easeIn(heightPercent));

    //mix normals
    float normalMix = TexCoord.x;
    float normalMultiplier = -(1.0 + -2.0 * int(gl_FrontFacing));
    vec3 norm = normalize(mix(normalRot1,normalRot2,normalMix) * normalMultiplier);
    // vec3 norm = normalize(Normal * normalMultiplier);
    vec3 viewDir = normalize(vec3(viewPos) - FragPos);
    
    //grab light intensity
    vec3 lightIntensity = vec3(calcLightIntensityTotal(norm));

    // Calculate the light to apply
    vec3 light = getTotalLight(
        material,
        textureColor,
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
    FragColor = vec4(finalColor, FOLIAGE_TRANSPARENCY);
}

float easeIn(float interpolator){
    return interpolator * interpolator;
}

float easeOut(float interpolator){
    return 1 - easeIn(1 - interpolator);
}