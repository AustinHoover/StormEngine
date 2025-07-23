//uncomment if working on this library file:
//#version 450 core
#extension GL_ARB_shading_language_include : require
#include "./material.fs"

/**
  * Maximum number of point lights
  */
#define MAX_POINT_LIGHTS 512

/**
  * Maximum number of lights per cluster
  */
#define MAX_LIGHTS_PER_CLUSTER 100

/**
  * Bind points for different SSBOs
  */
#define CLUSTER_SSBO_BIND_POINT 1
#define POINT_LIGHT_SSBO_BIND_POINT 2
#define DIRECT_LIGHT_SSBO_BIND_POINT 3

/**
  * The direct global light
  */
struct DirectLight {
    vec4 direction;
    vec4 color;
    vec4 ambientColor;
};

/**
  * A point light
  */
struct PointLight {
    vec4 position;
    vec4 color;
    float constant;
    float linear;
    float quadratic;
    float radius;
};

/**
  * A light cluster
  */
struct Cluster {
    vec4 minPoint;
    vec4 maxPoint;
    uint count;
    uint lightIndices[MAX_LIGHTS_PER_CLUSTER];
};

/**
  * Cutoff for fragment alpha
  */
#define FRAGMENT_ALPHA_CUTOFF 0.001

layout(std430, binding = CLUSTER_SSBO_BIND_POINT) restrict buffer clusterGridSSBO {
    Cluster clusters[];
};

layout(std430, binding = POINT_LIGHT_SSBO_BIND_POINT) restrict buffer pointLightSSBO {
    PointLight pointLight[];
};

layout(std430, binding = DIRECT_LIGHT_SSBO_BIND_POINT) restrict buffer dirLightSSBO {
    DirectLight directLight;
};


/**
  * The minimum multiplier that shadow can apply to the fragment (ie how dark can it make the fragment)
  */
#define SHADOW_MIN_MULTIPLIER 0.4f




/**
  * Used for light cluster calculation
  */
uniform float zNear;
uniform float zFar;
uniform uvec3 gridSize;
uniform uvec2 screenDimensions;

/**
  * The light depth map texture
  */
uniform sampler2D shadowMap;


/**
  * Finds the light cluster for this fragment
  */
uint findCluster(vec3 viewspaceFragPos, float zNear, float zFar);

/**
  * Calculates the point light's value applied to this fragment
  */
vec3 CalcPointLight(PointLight pointLight, vec3 normal, vec3 fragPos, vec3 viewDir);

/**
  * calculates the total light intensity on this fragment
  */
float calcLightIntensityTotal(vec3 normal);

/**
  * Calculates the shadow applied to this fragment
  */
float ShadowCalculation(vec4 fragPosLightSpace, vec3 lightDir, vec3 normal);

/**
  * Gets the total light to apply to the fragment
  */
vec3 getTotalLight(Material mat, vec3 norm, vec3 viewDir);

/**
  * Calculates the ambient light applied to this fragment
  */
float calcLightIntensityAmbient(){
    //calculate average of ambient light
    float avg = (directLight.color.x + directLight.color.y + directLight.color.z)/3.0;
    return avg;
}

/**
  * Calculates the ambient light
  */
vec3 calcAmbientLight(vec3 diffuseVal){
    return directLight.ambientColor.rgb * diffuseVal;
}

//
/**
  * Calculates the direct light applied to this fragment
  */
float calcLightIntensityDir(vec3 normal){
    vec3 lightDir = normalize(-directLight.direction.xyz);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    
    return diff;
}

/**
  * Calculates the direct light
  */
vec3 calcDiffuseLight(vec3 normal, vec3 diffuseVal){
    vec3 lightDir = normalize(-directLight.direction.xyz);
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 fullColor = directLight.color.rgb * (diff * diffuseVal);
    vec3 colorClamp = vec3(max(fullColor.x,0),max(fullColor.y,0),max(fullColor.z,0));
    return colorClamp;
}

/**
  * Calculates the direct light applied to this fragment
  */
vec3 calcSpecLight(
    vec3 viewPos,
    vec3 fragPos,
    vec3 norm,
    float shininess,
    vec3 specularVal
){
    vec3 viewDir = normalize(viewPos - fragPos);
    vec3 reflectDir = reflect(-directLight.direction.xyz, norm);  
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);
    vec3 specular = directLight.color.rgb * (spec * specularVal);
    vec3 specClamp = vec3(max(specular.x,0),max(specular.y,0),max(specular.z,0));
    return specClamp;
}

/**
  * Calculates the total light intensity applied to this fragment
  */
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
    vec3 diffuseLightColor = directLight.color.rgb * calcLightIntensityDir(normal);

    //sum light colors
    vec3 totalLightColor = diffuseLightColor;
    return totalLightColor;
}

/**
  * Calculates the point light applied to this fragment
  */
vec3 CalcPointLight(PointLight pointLight, vec3 normal, vec3 fragPos, vec3 viewDir){
    vec3 lightDir = normalize(pointLight.position.xyz - fragPos);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    // vec3 reflectDir = reflect(-lightDir, normal);
    // float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    // attenuation
    float distance = length(pointLight.position.xyz - fragPos);
    float attenuation = 1.0 / (pointLight.constant + pointLight.linear * distance + pointLight.quadratic * (distance * distance));
    if(distance > pointLight.radius){
        attenuation = 0;
    }
    // combine results
    vec3 ambient = pointLight.color.xyz;// * vec4(texture(material.diffuse, TexCoord)).xyz;
    vec3 diffuse = pointLight.color.xyz * diff;// * vec4(texture(material.diffuse, TexCoord)).xyz;
    // vec3 specular = pLspecular[i] * spec;// * vec4(texture(material.specular, TexCoord)).xyz;
    ambient = ambient * attenuation;
    diffuse = diffuse * attenuation;
    // specular *= attenuation;
    vec3 specular = vec3(0,0,0);

    vec3 finalValue = vec3(0);
    if(distance < pointLight.radius){
        finalValue = (ambient + diffuse + specular);
        finalValue = vec3(max(finalValue.x,0),max(finalValue.y,0),max(finalValue.z,0));
    }

    return finalValue;
}

/**
  * Finds the light cluster this fragment belongs to
  */
uint findCluster(vec3 viewspaceFragPos, float zNear, float zFar){
    uint zTile = uint((log(abs(viewspaceFragPos.z) / zNear) * gridSize.z) / log(zFar / zNear));
    vec2 tileSize = screenDimensions / gridSize.xy;
    uvec3 tile = uvec3(gl_FragCoord.xy / tileSize, zTile);
    return tile.x + (tile.y * gridSize.x) + (tile.z * gridSize.x * gridSize.y);
}

/**
  * Calculates the shadow applied to this fragment
  */
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

    return clamp(1.0 - shadow, 0.0, 0.7);
}

/**
  * Gets the total light to apply to the fragment
  */
vec3 getTotalLight(
    Material mat,
    vec2 texCoord,
    vec3 viewPos,
    vec4 fragPosLightSpace,
    vec3 fragPosView,
    vec3 fragPos,
    vec3 norm,
    vec3 viewDir
){
    vec3 diffuseVal = texture(mat.diffuse, texCoord).rgb;
    vec3 specularVal = vec3(0);

    //
    //Global light calculations
    vec3 ambientLight = calcAmbientLight(diffuseVal);
    vec3 diffuseLight = calcDiffuseLight(norm, diffuseVal);
    vec3 specLight = calcSpecLight(viewPos,fragPos,norm,mat.shininess,specularVal);
    vec3 lightSum = ambientLight + diffuseLight + specLight;

    //
    //point light calculations
    uint clusterIndex = findCluster(fragPosView, zNear, zFar);
    uint pointLightCount = clusters[clusterIndex].count;
    for(int i = 0; i < pointLightCount; i++){
        uint pointLightIndex = clusters[clusterIndex].lightIndices[i];
        PointLight pointLight = pointLight[pointLightIndex];
        lightSum = lightSum + CalcPointLight(pointLight, norm, fragPos, viewDir);
    }
    //error checking on light clusters
    if(pointLightCount > MAX_LIGHTS_PER_CLUSTER){
        return vec3(1.0,0.0,0.0);
    }

    //
    //shadow calculations
    float shadow = ShadowCalculation(fragPosLightSpace, normalize(-directLight.direction.xyz), -norm);
    float shadowMultiplier = max(shadow,SHADOW_MIN_MULTIPLIER);
    lightSum = lightSum * shadowMultiplier;

    //clamp
    vec3 lightClamp = vec3(min(lightSum.x,1),min(lightSum.y,1),min(lightSum.z,1));

    return lightClamp;
}

/**
  * Gets the total light to apply to the fragment
  */
vec3 getTotalLight(
    Material mat,
    vec3 albedo,
    vec3 viewPos,
    vec4 fragPosLightSpace,
    vec3 fragPosView,
    vec3 fragPos,
    vec3 norm,
    vec3 viewDir
){
    vec3 diffuseVal = albedo;
    vec3 specularVal = vec3(0);

    //
    //Global light calculations
    vec3 ambientLight = calcAmbientLight(diffuseVal);
    vec3 diffuseLight = calcDiffuseLight(norm, diffuseVal);
    vec3 specLight = calcSpecLight(viewPos,fragPos,norm,mat.shininess,specularVal);
    vec3 lightSum = ambientLight + diffuseLight + specLight;

    //
    //point light calculations
    uint clusterIndex = findCluster(fragPosView, zNear, zFar);
    uint pointLightCount = clusters[clusterIndex].count;
    for(int i = 0; i < pointLightCount; i++){
        uint pointLightIndex = clusters[clusterIndex].lightIndices[i];
        PointLight pointLight = pointLight[pointLightIndex];
        lightSum = lightSum + CalcPointLight(pointLight, norm, fragPos, viewDir);
    }
    //error checking on light clusters
    if(pointLightCount > MAX_LIGHTS_PER_CLUSTER){
        return vec3(1.0,0.0,0.0);
    }

    //
    //shadow calculations
    float shadow = ShadowCalculation(fragPosLightSpace, normalize(-directLight.direction.xyz), -norm);
    float shadowMultiplier = max(shadow,SHADOW_MIN_MULTIPLIER);
    lightSum = lightSum * shadowMultiplier;

    //clamp
    vec3 lightClamp = vec3(min(lightSum.x,1),min(lightSum.y,1),min(lightSum.z,1));

    return lightClamp;
}