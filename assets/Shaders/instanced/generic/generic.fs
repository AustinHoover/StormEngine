#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/lights.fs"
#include "../../lib/material.fs"

//generic.fs

#define SMALL_EPSILON 0.0001

out vec4 FragColor;



in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoord;
in vec4 FragPosLightSpace;


uniform vec3 viewPos;
// uniform DirLight dirLight;
// uniform PointLight pointLights[NR_POINT_LIGHTS];
// uniform SpotLight spotLight;
uniform Material material;


void main(){
    
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(viewPos - FragPos);
    
    //grab light intensity
    float lightIntensity = calcLightIntensityTotal(norm);

    if(texture(material.diffuse, TexCoord).a < SMALL_EPSILON){
        discard;
    }

    //get color of base texture
    vec3 textureColor = texture(material.diffuse, TexCoord).rgb;

    //shadow
    float shadow = ShadowCalculation(FragPosLightSpace, normalize(-directLight.direction), norm);

    //calculate final color
    vec3 finalColor = textureColor * lightIntensity * max(shadow,0.4);
    // vec3 lightAmount = CalcDirLight(norm, viewDir);
    // for(int i = 0; i < NR_POINT_LIGHTS; i++){
    //    lightAmount += CalcPointLight(i, norm, FragPos, viewDir);
    // }

    //this final calculation is for transparency
    FragColor = vec4(finalColor, 1);//texture(ourTexture, TexCoord);//vec4(result, 1.0);
}
