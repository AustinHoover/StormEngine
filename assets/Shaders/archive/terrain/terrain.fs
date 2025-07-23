#version 410 core
#extension GL_ARB_explicit_uniform_location : enable

#define NR_POINT_LIGHTS 10


out vec4 FragColor;


layout (std140) uniform Lights {
    //                                        this is how many      because we have to align
    //                                        bytes it SHOULD       in multiples of 16, this
    //                                        take                  it where it ACTUALLY is
    //
    //refer: https://learnopengl.com/Advanced-OpenGL/Advanced-GLSL
    //
    //                                        base alignment        aligned offset
    //direct light
    vec3 dLDirection;                      // 16                    0
    vec3 dLAmbient;                        // 16                    16
    vec3 dLDiffuse;                        // 16                    32
    vec3 dLSpecular;                       // 16                    48

    //point light
    vec3 pLposition[NR_POINT_LIGHTS];      // 16*10                 64
    float pLconstant[NR_POINT_LIGHTS];     // 16*10                 224
    float pLlinear[NR_POINT_LIGHTS];       // 16*10                 384
    float pLquadratic[NR_POINT_LIGHTS];    // 16*10                 544
    vec3 pLambient[NR_POINT_LIGHTS];       // 16*10                 704
    vec3 pLdiffuse[NR_POINT_LIGHTS];       // 16*10                 864
    vec3 pLspecular[NR_POINT_LIGHTS];      // 16*10                 1024

    //for a total size of   1184

};

struct Material {
    sampler2D diffuse;
    sampler2D specular;
    float shininess;
}; 

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoord;
in vec4 FragPosLightSpace;
flat in ivec4 groundTexIndices;
uniform vec3 viewPos;


uniform Material material;

//texture stuff
// uniform sampler2D ourTexture;
uniform int hasTransparency;
// uniform sampler2D specularTexture;

//light depth map
layout (location = 3) uniform sampler2D shadowMap;

//textures
//
// Goal is to have a texture for the current chunk and one for each nearnby chunk
//
//
//
// uniform sampler2D groundTextures1;
// uniform sampler2D groundTextures2;
// uniform sampler2D groundTextures3;
// uniform sampler2D groundTextures4;
// //fifth texture unit is for shadow map
// uniform sampler2D groundTextures5;
//this is for bindable ground textures
layout (location = 5) uniform sampler2D groundTextures[10];

// function prototypes
vec3 CalcDirLight(vec3 normal, vec3 viewDir, vec3 texColor);
vec3 CalcPointLight(int i, vec3 normal, vec3 fragPos, vec3 viewDir);
// vec3 CalcSpotLight(vec3 normal, vec3 fragPos, vec3 viewDir);

float ShadowCalculation(vec4 fragPosLightSpace, vec3 lightDir, vec3 normal);

vec3 blendedTextureColor(vec2 texPos, vec4 tex1, vec4 tex2, vec4 tex3, vec4 tex4);



vec4 getTextureColor(int index, vec2 coord){
    if(index == 0){
        return texture(groundTextures[0], coord);
    }
    if(index == 1){
        return texture(groundTextures[1], coord);
    }
    if(index == 2){
        return texture(groundTextures[2], coord);
    }
    if(index == 3){
        return texture(groundTextures[3], coord);
    }
    if(index == 4){
        return texture(groundTextures[4], coord);
    }
    // return texture(shadowMap, coord);
    // return vec3(1,1,1);
    return vec4(0,0,0,1);
}

void main(){
    if(hasTransparency == 1){
        if(texture(material.diffuse, TexCoord).a < 0.1){
            discard;
        }
    }
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(viewPos - FragPos);
    


    // sampler2DArray text = groundTextures;
    // sampler2D test = groundTextures1;

    vec4 texColor1 = getTextureColor(groundTexIndices.x, TexCoord);
    vec4 texColor2 = getTextureColor(groundTexIndices.y, TexCoord);
    vec4 texColor3 = getTextureColor(groundTexIndices.z, TexCoord);
    vec4 texColor4 = getTextureColor(groundTexIndices.w, TexCoord);
    // vec4 texColor1 = texture(groundTextures[groundTexIndices.x], TexCoord);
    // vec4 texColor2 = texture(groundTextures[groundTexIndices.y], TexCoord);
    // vec4 texColor3 = texture(groundTextures[groundTexIndices.z], TexCoord);
    // vec4 texColor4 = texture(groundTextures[groundTexIndices.w], TexCoord);
    // vec4 texColor1 = texture(groundTextures[0], TexCoord);
    // vec4 texColor2 = texture(groundTextures[1], TexCoord);
    // vec4 texColor3 = texture(groundTextures[1], TexCoord);
    // vec4 texColor4 = texture(groundTextures[1], TexCoord);
    vec3 finalTexColor = blendedTextureColor(TexCoord, texColor1, texColor2, texColor3, texColor4);
    // vec3 finalTexColor = vec3(0,0,0);
    // vec3 finalTexColor = mix(mix(texColor1,texColor2,TexCoord.x),mix(texColor3,texColor4,TexCoord.x),TexCoord.y).xyz;//blendedTextureColor(TexCoord, texColor1, texColor2, texColor3, texColor4);
    // if(groundTexIndices.x != 1 || groundTexIndices.y != 0 || groundTexIndices.z != 0 || groundTexIndices.w != 0){
    //     finalTexColor = vec3(1,0,0);
    // }
    // vec3 finalTexColor = vec3(groundTexIndices.x,groundTexIndices.y,groundTexIndices.z);
    // vec3 finalTexColor = vec3(1.0,0,0);

    // vec4 tex2 = texture(groundTextures[int(groundTexIndices.y)], TexCoord);
    // vec4 tex3 = texture2D(groundTextures[int(groundTexIndex.z * 2)], texPos);
    // vec4 tex4 = texture2D(groundTextures[int(groundTexIndex.w * 2)], texPos);

    //get texture color
    // vec3 texColor = vec3(0,0,1);//blendedTextureColor(texPos, groundTexIndices);
    

    vec3 result = CalcDirLight(norm, viewDir, finalTexColor);
    for(int i = 0; i < NR_POINT_LIGHTS; i++){
       result += CalcPointLight(i, norm, FragPos, viewDir);
    }
    // result += CalcSpotLight(spotLight, norm, FragPos, viewDir);

    FragColor = vec4(result, 1);//texture(ourTexture, TexCoord);//vec4(result, 1.0);
}

// calculates the color when using a directional light.
vec3 CalcDirLight(vec3 normal, vec3 viewDir, vec3 texColor){
    vec3 lightDir = normalize(-dLDirection);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    // combine results
    // vec3 texColor = texture(material.diffuse, TexCoord).rgb;
    vec3 ambient = dLAmbient;
    vec3 diffuse = dLDiffuse * diff;
    //vec3 specular = light.specular * spec * vec3(texture(material.specular, TexCoord).rgb);


    float shadow = ShadowCalculation(FragPosLightSpace, lightDir, normal);
    // return shadow * vec3(1,1,1);
    return (  ambient + (1.0-shadow) * diffuse  ) * texColor;// + specular);
}


// calculates the color when using a point light.
vec3 CalcPointLight(int i, vec3 normal, vec3 fragPos, vec3 viewDir){
    vec3 lightDir = normalize(pLposition[i] - fragPos);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    // vec3 reflectDir = reflect(-lightDir, normal);
    // float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    // attenuation
    float distance = length(pLposition[i] - fragPos);
    float attenuation = 1.0 / (pLconstant[i] + pLlinear[i] * distance + pLquadratic[i] * (distance * distance));    
    // combine results
    vec3 ambient = pLambient[i];// * vec4(texture(material.diffuse, TexCoord)).xyz;
    vec3 diffuse = pLdiffuse[i] * diff;// * vec4(texture(material.diffuse, TexCoord)).xyz;
    // vec3 specular = pLspecular[i] * spec;// * vec4(texture(material.specular, TexCoord)).xyz;
    ambient *= attenuation;
    diffuse *= attenuation;
    // specular *= attenuation;
    vec3 specular = vec3(0,0,0);

    vec3 finalValue = (ambient + diffuse + specular);
    finalValue = vec3(max(finalValue.x,0),max(finalValue.y,0),max(finalValue.z,0));

    return finalValue;
}

// calculates the color when using a spot light.
// vec3 CalcSpotLight(vec3 normal, vec3 fragPos, vec3 viewDir)
// {
//     vec3 lightDir = normalize(light.position - fragPos);
//     // diffuse shading
//     float diff = max(dot(normal, lightDir), 0.0);
//     // specular shading
//     vec3 reflectDir = reflect(-lightDir, normal);
//     float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
//     // attenuation
//     float distance = length(light.position - fragPos);
//     float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));    
//     // spotlight intensity
//     float theta = dot(lightDir, normalize(-light.direction)); 
//     float epsilon = light.cutOff - light.outerCutOff;
//     float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
//     // combine results
//     vec3 ambient = light.ambient * vec3(texture(material.diffuse, TexCoord));
//     vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, TexCoord));
//     vec3 specular = light.specular * spec * vec3(texture(material.specular, TexCoord));
//     ambient *= attenuation * intensity;
//     diffuse *= attenuation * intensity;
//     specular *= attenuation * intensity;
//     return (ambient + diffuse + specular);
// }


float ShadowCalculation(vec4 fragPosLightSpace, vec3 lightDir, vec3 normal){

    // perform perspective divide
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;

    //transform to NDC
    projCoords = projCoords * 0.5 + 0.5;

    //get closest depth from light's POV
    float closestDepth = texture(shadowMap, projCoords.xy).r;

    //get depth of current fragment
    float currentDepth = projCoords.z;
    
    //calculate bias
    float bias = min(0.05 * (1.0 - dot(normal, lightDir)), 0.005);

    //calculate shadow value
    float shadow = currentDepth - bias > closestDepth  ? 1.0 : 0.0;

    if(projCoords.z > 1.0){
        shadow = 0.0;
    }

    // shadow = currentDepth - closestDepth;

    return shadow;
}


vec3 blendedTextureColor(vec2 texPos, vec4 tex1, vec4 tex2, vec4 tex3, vec4 tex4){
    // int texIndex1 = int(groundTexIndex.x * 2);
    // int texIndex2 = int(groundTexIndex.y * 2);
    // int texIndex3 = int(groundTexIndex.z * 2);
    // int texIndex4 = int(groundTexIndex.w * 2);
    // vec4 tex1 = texture2D(groundTextures[int(groundTexIndex.x * 2)], texPos);
    // vec4 tex2 = texture2D(groundTextures[int(groundTexIndex.y * 2)], texPos);
    // vec4 tex3 = texture2D(groundTextures[int(groundTexIndex.z * 2)], texPos);
    // vec4 tex4 = texture2D(groundTextures[int(groundTexIndex.w * 2)], texPos);
    // float percentTex1 = (texPos.x - 1) * (texPos.y - 1);
    // float percentTex2 = (texPos.x - 0) * (texPos.y - 1);
    // float percentTex3 = (texPos.x - 1) * (texPos.y - 0);
    // float percentTex4 = (texPos.x - 0) * (texPos.y - 0);
    return mix(mix(tex1,tex2,texPos.x),mix(tex3,tex4,texPos.x),texPos.y).rgb;
}