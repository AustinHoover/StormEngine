#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/lights.fs"
#include "../../lib/material.fs"

//proceduraltree.fs



in vec3 FragPos;
in vec3 ViewFragPos;
in vec3 Normal;
in vec2 TexCoord;
in vec4 FragPosLightSpace;


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

    //get color of base texture
    vec3 textureColor = texture(material.diffuse, TexCoord).rgb;

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

    //this final calculation is for transparency
    FragColor = vec4(finalColor, texture(material.diffuse, TexCoord).a);
}
