

#version 330 core
out vec4 FragColor;

struct Material {
    sampler2D diffuse;
    sampler2D specular;
    float shininess;
}; 

struct DirLight {
    vec3 direction;
	
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct PointLight {
    vec3 position;
    
    float constant;
    float linear;
    float quadratic;
    
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct SpotLight {
    vec3 position;
    vec3 direction;
    float cutOff;
    float outerCutOff;
  
    float constant;
    float linear;
    float quadratic;
  
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;       
};

#define NR_POINT_LIGHTS 10

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoord;
in vec4 FragPosLightSpace;

uniform vec3 viewPos;
uniform DirLight dirLight;
uniform PointLight pointLights[NR_POINT_LIGHTS];
uniform SpotLight spotLight;
uniform Material material;

//texture stuff
// uniform sampler2D ourTexture;
uniform int hasTransparency;
// uniform sampler2D specularTexture;

//light depth map
uniform sampler2D shadowMap;


// function prototypes
vec3 CalcDirLight(DirLight light, vec3 normal, vec3 viewDir);
vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir);
vec3 CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir);
float ShadowCalculation(vec4 fragPosLightSpace, vec3 lightDir, vec3 normal);

void main(){
    if(hasTransparency == 1){
        if(texture(material.diffuse, TexCoord).a < 0.1){
            discard;
        }
    }
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(viewPos - FragPos);
    
    

    vec3 result = CalcDirLight(dirLight, norm, viewDir);
    //for(int i = 0; i < NR_POINT_LIGHTS; i++){
    //    result += CalcPointLight(pointLights[i], norm, FragPos, viewDir);
    //}
    //result += CalcSpotLight(spotLight, norm, FragPos, viewDir);

    FragColor = vec4(result, texture(material.diffuse, TexCoord).a);//texture(ourTexture, TexCoord);//vec4(result, 1.0);
}

// calculates the color when using a directional light.
vec3 CalcDirLight(DirLight light, vec3 normal, vec3 viewDir){
    vec3 lightDir = normalize(-light.direction);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    // combine results
    vec3 texColor = texture(material.diffuse, TexCoord).rgb;
    vec3 ambient = light.ambient;
    vec3 diffuse = light.diffuse * diff;
    //vec3 specular = light.specular * spec * vec3(texture(material.specular, TexCoord).rgb);


    float shadow = ShadowCalculation(FragPosLightSpace, lightDir, normal);

    return (  ambient + (1.0-shadow) * 1.0  ) * texColor;// + specular);
}


// calculates the color when using a point light.
vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir){
    vec3 lightDir = normalize(light.position - fragPos);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    // attenuation
    float distance = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));    
    // combine results
    vec3 ambient = light.ambient * vec4(texture(material.diffuse, TexCoord)).xyz;
    vec3 diffuse = light.diffuse * diff * vec4(texture(material.diffuse, TexCoord)).xyz;
    vec3 specular = light.specular * spec * vec4(texture(material.specular, TexCoord)).xyz;
    ambient *= attenuation;
    diffuse *= attenuation;
    specular *= attenuation;
    return (ambient + diffuse + specular);
}

// calculates the color when using a spot light.
vec3 CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir)
{
    vec3 lightDir = normalize(light.position - fragPos);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    // attenuation
    float distance = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));    
    // spotlight intensity
    float theta = dot(lightDir, normalize(-light.direction)); 
    float epsilon = light.cutOff - light.outerCutOff;
    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
    // combine results
    vec3 ambient = light.ambient * vec3(texture(material.diffuse, TexCoord));
    vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, TexCoord));
    vec3 specular = light.specular * spec * vec3(texture(material.specular, TexCoord));
    ambient *= attenuation * intensity;
    diffuse *= attenuation * intensity;
    specular *= attenuation * intensity;
    return (ambient + diffuse + specular);
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

    // shadow = currentDepth;

    return shadow;
}