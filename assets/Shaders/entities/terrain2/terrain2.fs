#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/lights.fs"
#include "../../lib/material.fs"
#include "../../lib/standarduniform.fs"

//texture defines
#define ATLAS_ELEMENT_DIM 256.0
#define ATLAS_DIM 8192.0
#define ATLAS_EL_PER_ROW 32
#define ATLAS_NORMALIZED_ELEMENT_WIDTH 0.031 //within the single texture within the atlas, we use this so we never go over the end of the texture
#define ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL 0.03125 //used to properly shift from texture to texture in the atlas


/**
  * Transparency of the terrain
  */
#define TERRAIN_TRANSPARENCY 1.0




in vec3 FragPos;
in vec3 ViewFragPos;
in vec3 Normal;
in vec2 texPlane1;
in vec2 texPlane2;
in vec2 texPlane3;
in vec4 FragPosLightSpace;
in vec3 samplerIndexVec; //the indices in the atlas of textures to sample
in vec3 samplerRatioVec; //the vector of HOW MUCH to pull from each texture in the atlas


uniform dvec3 viewPos;
// uniform DirLight dirLight;
// uniform PointLight pointLights[NR_POINT_LIGHTS];
// uniform SpotLight spotLight;
uniform Material material;


/**
The output
*/
out vec4 FragColor;

// function prototypes
vec3 getColor(vec2 texPlane1, vec2 texPlane2, vec2 texPlane3, vec3 normal, vec3 samplerIndexVec, vec3 samplerRatioVec, Material material);

void main(){
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(vec3(viewPos) - FragPos);

    //get color of base texture
    vec3 albedo = getColor(texPlane1, texPlane2, texPlane3, norm, samplerIndexVec, samplerRatioVec, material);

    // Calculate the light to apply
    vec3 light = getTotalLight(
        material,
        albedo,
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
    FragColor = vec4(finalColor, TERRAIN_TRANSPARENCY);
}


/**
 * The function that gets the texture color based on the triplanar texture mapping and the voxel type at each point along the vert.
 * See the triplanar mapping wiki article for an explanation of math involved.
 */
vec3 getColor(vec2 texPlane1, vec2 texPlane2, vec2 texPlane3, vec3 normal, vec3 samplerIndexVec, vec3 samplerRatioVec, Material material){

    vec3 weights = abs(normal);

    //what is the index in the atlas of the texture for a given vertex
    int vert1AtlasIndex = int(samplerIndexVec.x);
    int vert2AtlasIndex = int(samplerIndexVec.y);
    int vert3AtlasIndex = int(samplerIndexVec.z);

    //what is the weight of that texture relative to the fragment
    float vert1Weight = samplerRatioVec.x;
    float vert2Weight = samplerRatioVec.y;
    float vert3Weight = samplerRatioVec.z;

    //the x-wise uv of the texture for vert1
    vec2 vert1_x_uv = vec2(
        (fract(texPlane1.x) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (mod(samplerIndexVec.x,ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL),
        (fract(texPlane1.y) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (round(samplerIndexVec.x / ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL)
    );
    //the x-wise uv of the texture for vert2
    vec2 vert2_x_uv = vec2(
        (fract(texPlane1.x) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (mod(samplerIndexVec.y,ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL),
        (fract(texPlane1.y) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (round(samplerIndexVec.y / ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL)
    );
    //the x-wise uv of the texture for vert3
    vec2 vert3_x_uv = vec2(
        (fract(texPlane1.x) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (mod(samplerIndexVec.z,ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL),
        (fract(texPlane1.y) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (round(samplerIndexVec.z / ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL)
    );
    //albedo for the X texture
    vec3 albedoX = texture(material.diffuse, vert1_x_uv).rgb * vert1Weight + texture(material.diffuse, vert2_x_uv).rgb * vert2Weight + texture(material.diffuse, vert3_x_uv).rgb * vert3Weight;


    //the y-wise uv of the texture for vert1
    vec2 vert1_y_uv = vec2(
        (fract(texPlane2.x) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (mod(samplerIndexVec.x,ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL),
        (fract(texPlane2.y) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (round(samplerIndexVec.x / ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL)
    );
    //the y-wise uv of the texture for vert2
    vec2 vert2_y_uv = vec2(
        (fract(texPlane2.x) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (mod(samplerIndexVec.y,ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL),
        (fract(texPlane2.y) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (round(samplerIndexVec.y / ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL)
    );
    //the y-wise uv of the texture for vert3
    vec2 vert3_y_uv = vec2(
        (fract(texPlane2.x) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (mod(samplerIndexVec.z,ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL),
        (fract(texPlane2.y) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (round(samplerIndexVec.z / ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL)
    );
    //albedo for the X texture
    vec3 albedoY = texture(material.diffuse, vert1_y_uv).rgb * vert1Weight + texture(material.diffuse, vert2_y_uv).rgb * vert2Weight + texture(material.diffuse, vert3_y_uv).rgb * vert3Weight;




    //the z-wise uv of the texture for vert1
    vec2 vert1_z_uv = vec2(
        (fract(texPlane3.x) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (mod(samplerIndexVec.x,ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL),
        (fract(texPlane3.y) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (round(samplerIndexVec.x / ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL)
    );
    //the z-wise uv of the texture for vert2
    vec2 vert2_z_uv = vec2(
        (fract(texPlane3.x) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (mod(samplerIndexVec.y,ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL),
        (fract(texPlane3.y) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (round(samplerIndexVec.y / ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL)
    );
    //the z-wise uv of the texture for vert3
    vec2 vert3_z_uv = vec2(
        (fract(texPlane3.x) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (mod(samplerIndexVec.z,ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL),
        (fract(texPlane3.y) * ATLAS_NORMALIZED_ELEMENT_WIDTH) + (round(samplerIndexVec.z / ATLAS_EL_PER_ROW) * ATLAS_NORMALIZED_ELEMENT_WIDTH_FULL)
    );
    //albedo for the X texture
    vec3 albedoZ = texture(material.diffuse, vert1_z_uv).rgb * vert1Weight + texture(material.diffuse, vert2_z_uv).rgb * vert2Weight + texture(material.diffuse, vert3_z_uv).rgb * vert3Weight;
    

    return (albedoX * weights.x + albedoY * weights.y + albedoZ * weights.z);
}
