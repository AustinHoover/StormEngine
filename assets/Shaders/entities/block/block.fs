#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/lights.fs"
#include "../../lib/material.fs"

//texture defines
#define ATLAS_ELEMENT_DIM 256.0
#define ATLAS_DIM 8192.0
#define ATLAS_EL_PER_ROW 32
#define ATLAS_NORMALIZED_ELEMENT_WIDTH 0.031 //within the single texture within the atlas, we use this so we never go over the end of the texture
#define ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL 0.03125 //used to properly shift from texture to texture in the atlas


in vec3 FragPos;
in vec3 ViewFragPos;
in vec3 Normal;
in vec2 uv;
in vec4 FragPosLightSpace;
in flat int samplerIndexVec; //the indices in the atlas of textures to sample


uniform vec3 viewPos;
uniform Material material;

/**
Used for light cluster calculation
*/
uniform mat4 view;

/**
The output
*/
out vec4 FragColor;


// function prototypes
vec2 getColor(vec2 uv, vec3 normal, int samplerIndexVec);

void main(){
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(viewPos - FragPos);
    
    //get color of base texture
    vec2 TexCoord = getColor(uv, norm, samplerIndexVec);

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
    vec3 finalColor = light;

    //this final calculation is for transparency
    FragColor = vec4(finalColor, texture(material.diffuse, TexCoord).a);
}


/**
 * The function that gets the texture color based on the triplanar texture mapping and the voxel type at each point along the vert.
 * See the triplanar mapping wiki article for an explanation of math involved.
 */
vec2 getColor(vec2 uv, vec3 normal, int samplerIndexVec){

    //the uv of the texture clamped within the atlas
    vec2 actualUv = vec2(
        (fract(uv.x) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (mod(samplerIndexVec,ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL),
        (fract(uv.y) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (round(samplerIndexVec / ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL)
    );
    

    return actualUv;
}