
/**
  * A material
  */
struct Material {
    /**
      * The diffuse sampler
      */
    sampler2D diffuse;

    /**
      * The diffuse sampler
      */
    sampler2D specular;

    /**
      * The normal sampler
      */
    sampler2D normal;

    /**
      * The reflection map
      */
    sampler2D parallax;

    /**
      * The shininess value
      */
    float shininess;

    /**
      * The reflectivity value
      */
    float reflectivity;

    /**
      * The albedo of the material
      */
    vec3 albedo;
};
