package electrosphere.entity;

/**
 * Tags used in scenes to delineate groups of entities
 */
public class EntityTags {

    public static final String BONE_ATTACHED = "boneAttached";
    public static final String TRANSFORM_ATTACHED = "transformAttached";
    public static final String COLLIDABLE = "collidable";
    public static final String SPRINTABLE = "sprintable";
    public static final String MOVEABLE = "moveable";
    public static final String ATTACKER = "attacker";
    public static final String TARGETABLE = "targetable";
    public static final String LIFE_STATE = "lifeState";
    public static final String CREATURE = "creature";
    public static final String FOLIAGE = "foliage";
    public static final String TERRAIN = "terrain";
    public static final String UI = "ui"; //is it a ui entity
    public static final String DRAWABLE = "drawable"; //is it drawable
    public static final String DRAW_INSTANCED = "drawInstanced"; //if it's instanced, but not necessarily managed by a service (ie a tree branch)
    public static final String DRAW_INSTANCED_MANAGED = "drawInstancedManaged"; //if it's managed by a service (ie foliage manager)
    public static final String POSEABLE = "poseable"; //is it poseable on server
    public static final String LIGHT = "light";
    public static final String ITEM = "item";
    public static final String OBJECT = "object";
    public static final String GRAVITY = "gravity";
    public static final String PARTICLE = "particle";
    public static final String DRAW_CAST_SHADOW = "drawCastShadow";
    public static final String DRAW_VOLUMETIC_DEPTH_PASS = "drawVolumetricDepthPass"; //draw in the volumetic phase of the volumetric pass
    public static final String DRAW_VOLUMETIC_SOLIDS_PASS = "drawVolumetricSolidsPass"; //draw in the non-volumetic phase of the volumetric pass
    public static final String DRAW_FOLIAGE_PASS = "drawFoliagePass"; //draw in the foliage pass

    /**
     * Entities that occupy block positions
     */
    public static final String BLOCK_OCCUPANT = "blockOccupant";

}
