#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/lights.fs"
#include "../../lib/material.fs"

//colorshift.fs


#define SMALL_EPSILON 0.001



in vec3 FragPos;
in vec3 ViewFragPos;
in vec3 Normal;
in vec2 TexCoord;
in vec4 FragPosLightSpace;
in vec3 colorShiftValue;


uniform vec3 viewPos;
uniform Material material;

uniform mat4 view;

/**
The output
*/
out vec4 FragColor;


void main(){
    
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(viewPos - FragPos);
    
    //grab light intensity
    vec3 lightIntensity = vec3(calcLightIntensityTotal(norm));

    if(texture(material.diffuse, TexCoord).a < SMALL_EPSILON){
        discard;
    }

    //get color of base texture
    vec3 textureColor = texture(material.diffuse, TexCoord).rgb;
    textureColor = vec3(
        textureColor.r * (colorShiftValue.x),
        textureColor.g * (colorShiftValue.y),
        textureColor.b * (colorShiftValue.z)
    );

    //shadow
    float shadow = ShadowCalculation(FragPosLightSpace, normalize(-directLight.direction), norm);

    //
    //point light calculations
    uint clusterIndex = findCluster(ViewFragPos, zNear, zFar);
    uint pointLightCount = clusters[clusterIndex].count;
    for(int i = 0; i < pointLightCount; i++){
        uint pointLightIndex = clusters[clusterIndex].lightIndices[i];
        PointLight pointLight = pointLight[pointLightIndex];
        lightIntensity = lightIntensity + CalcPointLight(pointLight, norm, FragPos, viewDir);
    }
    //error checking on light clusters
    if(pointLightCount > MAX_LIGHTS_PER_CLUSTER){
        FragColor = vec4(1.0f,0.0f,0.0f,1);
        return;
    }

    //calculate final color
    vec3 finalColor = textureColor * lightIntensity;
    // vec3 lightAmount = CalcDirLight(norm, viewDir);
    // for(int i = 0; i < NR_POINT_LIGHTS; i++){
    //    lightAmount += CalcPointLight(i, norm, FragPos, viewDir);
    // }

    //this final calculation is for transparency
    FragColor = vec4(finalColor, 1);//texture(ourTexture, TexCoord);//vec4(result, 1.0);
}
