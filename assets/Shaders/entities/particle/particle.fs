#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/lights.fs"
#include "../../lib/material.fs"
#include "../../lib/standarduniform.fs"

//foliage.fs

layout (location = 0) out vec4 accum;
layout (location = 1) out float reveal;




in vec3 FragPos;
in vec3 ViewFragPos;
in vec3 Normal;
in vec2 TexCoord;
in vec4 FragPosLightSpace;
in vec4 instanceColor;


uniform dvec3 viewPos;
uniform Material material;
uniform mat4 view;


/**
The output
*/
out vec4 FragColor;

// function prototypes
float easeIn(float interpolator);
float easeOut(float interpolator);

void main(){
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(vec3(viewPos) - FragPos);

    //get color of base texture
    vec4 textureColor =  texture(material.diffuse,TexCoord) * instanceColor;

    //the light level
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
    vec4 finalColor = textureColor.rgba * vec4(light,1.0);

    //calculate weight function
    float weight = clamp(pow(min(1.0, finalColor.a * 10.0) + 0.01, 3.0) * 1e3 * 
                        pow(1.0 - gl_FragCoord.z * 0.9, 3.0), 1e-2, 3e3);

    //emit colors
    accum = vec4(finalColor.rgb * finalColor.a, finalColor.a) * weight;
    // accum = finalColor * weight;
    reveal = finalColor.a;
}


float easeIn(float interpolator){
    return interpolator * interpolator;
}

float easeOut(float interpolator){
    return 1 - easeIn(1 - interpolator);
}