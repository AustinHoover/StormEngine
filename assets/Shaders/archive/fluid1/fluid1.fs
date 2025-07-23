#version 330 core

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
in vec2 texPlane1;
in vec2 texPlane2;
in vec2 texPlane3;
in vec4 FragPosLightSpace;


uniform vec3 viewPos;
// uniform DirLight dirLight;
// uniform PointLight pointLights[NR_POINT_LIGHTS];
// uniform SpotLight spotLight;
uniform Material material;

//texture stuff
// uniform sampler2D ourTexture;
uniform int hasTransparency;
// uniform sampler2D specularTexture;

//light depth map
uniform sampler2D shadowMap;


// function prototypes
// vec3 CalcDirLight(vec3 normal, vec3 viewDir);
// vec3 CalcPointLight(int i, vec3 normal, vec3 fragPos, vec3 viewDir);
// vec3 CalcSpotLight(vec3 normal, vec3 fragPos, vec3 viewDir);
float calcLightIntensityTotal(vec3 normal);
float ShadowCalculation(vec4 fragPosLightSpace, vec3 lightDir, vec3 normal);
vec3 getColor(vec2 texPlane1, vec2 texPlane2, vec2 texPlane3, vec3 normal, Material material);

void main(){
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(viewPos - FragPos);
    
    //grab light intensity
    float lightIntensity = calcLightIntensityTotal(norm);

    //get color of base texture
    vec3 textureColor = getColor(texPlane1, texPlane2, texPlane3, norm, material);

    //shadow
    float shadow = ShadowCalculation(FragPosLightSpace, normalize(-dLDirection), norm);

    //calculate final color
    vec3 finalColor = textureColor * lightIntensity * max(shadow,0.4);
    // vec3 lightAmount = CalcDirLight(norm, viewDir);
    // for(int i = 0; i < NR_POINT_LIGHTS; i++){
    //    lightAmount += CalcPointLight(i, norm, FragPos, viewDir);
    // }

    //this final calculation is for transparency
    FragColor = vec4(finalColor, 1);
}


vec3 getColor(vec2 texPlane1, vec2 texPlane2, vec2 texPlane3, vec3 normal, Material material){

    vec3 weights = abs(normal);

    vec3 albedoX = texture(material.diffuse, texPlane1).rgb;
    vec3 albedoY = texture(material.diffuse, texPlane2).rgb;
    vec3 albedoZ = texture(material.diffuse, texPlane3).rgb;


    return (albedoX * weights.x + albedoY * weights.y + albedoZ * weights.z);
}

//
float calcLightIntensityAmbient(){
    //calculate average of ambient light
    float avg = (dLAmbient.x + dLAmbient.y + dLAmbient.z)/3.0;
    return avg;
}

//
float calcLightIntensityDir(vec3 normal){
    vec3 lightDir = normalize(-dLDirection);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    
    return diff;
}

//
float calcLightIntensityTotal(vec3 normal){
    //ambient intensity
    float ambientLightIntensity = calcLightIntensityAmbient();

    //get direct intensity
    float directLightIntensity = calcLightIntensityDir(normal);

    //sum
    float total = ambientLightIntensity + directLightIntensity;
    return total;
}

//
vec3 getTotalLightColor(vec3 normal){
    //get the direct light color adjusted for intensity
    vec3 diffuseLightColor = dLDiffuse * calcLightIntensityDir(normal);

    //sum light colors
    vec3 totalLightColor = diffuseLightColor;
    return totalLightColor;
}

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
    vec3 ambient = pLambient[i];
    vec3 diffuse = pLdiffuse[i] * diff;
    ambient *= attenuation;
    diffuse *= attenuation;
    // specular *= attenuation;
    vec3 specular = vec3(0,0,0);

    vec3 finalValue = (ambient + diffuse + specular);
    finalValue = vec3(max(finalValue.x,0),max(finalValue.y,0),max(finalValue.z,0));

    return finalValue;
}


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
    float bias = max(0.05 * (1.0 - dot(normal, lightDir)), 0.005);

    //calculate shadow value
    float shadow = currentDepth - bias > closestDepth  ? 1.0 : 0.0;

    if(projCoords.z > 1.0){
        shadow = 0.0;
    }

    //calculate dot product, if it is >0 we know they're parallel-ish therefore should disregard the shadow mapping
    //ie the fragment is already facing away from the light source
    float dotprod = dot(normalize(lightDir),normalize(normal));

    if(dotprod > 0.0){
        shadow = 0.0;
    }

    // shadow = currentDepth;

    return shadow;
}