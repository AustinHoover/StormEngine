#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../../lib/lights.fs"
#include "../../../lib/material.fs"
#include "../../../lib/standarduniform.fs"


layout (location = 0) out vec4 accum;
layout (location = 1) out float reveal;


//inputs
in vec3 FragPos;
in vec3 ViewFragPos;
in vec3 Normal;
in vec2 TexCoord;
in vec4 FragPosLightSpace;


//view position
uniform dvec3 viewPos;

//material
uniform Material material;


uniform mat4 view;


//function prototypes
float linearizeDepth(float d,float zNear,float zFar);
float weightCalcOrigin(float finalAlpha, float zLoc, float linearizedLoc);
float weightCalcFlat(float finalAlpha, float zLoc, float linearizedLoc);
float weightCalcNew(float finalAlpha, float zLoc, float linearizedLoc);


void main(){

    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(vec3(viewPos) - FragPos);

    //get color of base texture
    vec4 textureColor = texture(material.diffuse, TexCoord);

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
    vec4 finalColor = vec4(material.albedo * light,textureColor.a);

    //calculate weight function
    float weight = clamp(pow(min(1.0, finalColor.a * 10.0) + 0.01, 3.0) * 1e8 * 
                        pow(1.0 - gl_FragCoord.z * 0.9, 3.0), 1e-2, 3e3);

    //emit colors
    accum = vec4(finalColor.rgb * finalColor.a, finalColor.a) * weight;
    reveal = finalColor.a;
}

//a weight calculation
float weightCalcOrigin(float finalAlpha, float zLoc, float linearizedLoc){
    float weight = clamp(pow(min(1.0, finalAlpha * 10.0) + 0.01, 3.0) * 1e8 * 
                         pow(1.0 - zLoc * 0.9, 3.0), 1e-2, 3e3);
    return weight;
}

//a weight calculation
float weightCalcFlat(float finalAlpha, float zLoc, float linearizedLoc){
    return 0.1f;
}

//a weight calculation
float weightCalcNew(float finalAlpha, float zLoc, float linearizedLoc){
    float alphaComponent = pow(min(1.0, finalAlpha * 10.0) + 0.01, 3.0);
    float zComponent = pow(1.0 - zLoc * 0.9, 3.0);
    
    float weightRaw = alphaComponent * 1e8 * zComponent;
    float weight = clamp(weightRaw, 1e-2, 3e3);
    return weight;
}

//Linearizes the depth
float linearizeDepth(float d,float zNear,float zFar){
    return zNear * zFar / (zFar + d * (zNear - zFar));
}
